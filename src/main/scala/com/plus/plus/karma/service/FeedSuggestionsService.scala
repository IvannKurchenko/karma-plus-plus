package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.all._
import cats.effect._
import com.plus.plus.karma.model._
import com.plus.plus.karma.model.stackexchange.{SiteStackExchangeTag, StackExchangeSite, StackExchangeSites, StackExchangeTags}
import com.plus.plus.karma.service.FeedSuggestionsService._
import io.circe.syntax._
import io.circe._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import scalacache.{Cache, Mode}
import scalacache.caffeine.CaffeineCache
import better.files._

import scala.concurrent.duration._
import fs2.Stream

class FeedSuggestionsService[F[_] : Mode : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                                        redditService: RedditService[F],
                                                                        stackExchangeService: StackExchangeService[F])
                                                                       (implicit A: Applicative[F]) {

  private implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  private implicit val languageIndexCache: Cache[GithubKarmaSuggestItems] = CaffeineCache[GithubKarmaSuggestItems]
  private implicit val stackExchangeTagsCache: Cache[StackExchangeItems] = CaffeineCache[StackExchangeItems]

  private val stackExchangeFileCache = File("data/stack-exchange-tags.json")

  def suggestions(term: String): F[KarmaSuggest] = {
    val normalized = normalize(term)
    if (normalized.nonEmpty) {
      for {
        reddit <- redditSuggestions(normalized)
        github <- githubSuggestions(normalized)
        stackExchange <- stackExchangeSuggestions(normalized)
      } yield KarmaSuggest(github ++ reddit ++ stackExchange)
    } else {
      KarmaSuggest.empty.pure
    }
  }

  private def redditSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    redditService.autocomplete(term).map(_.subreddits.filterNot(_.numSubscribers == 0).map(_.toKarmaSuggest))
  }

  private def githubSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    githubAllSuggestions.map { languages =>
      languages.items.filter(language => normalize(language.name).startsWith(term))
    }
  }

  private def stackExchangeSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    stackExchangeTags.map(_.items.filter(_.name.startsWith(term)))
  }

  /*
   * It is totally not functional approach to change service internal state via memorization, but otherwise
   * it will overcomplicate all other DI
   */
  def prefetchSuggestionData: F[Unit] = {
    Logger[F].info("Start prefetching internal data") *>
      githubAllSuggestions *>
      stackExchangeTags *>
      Logger[F].info("Finished prefetching internal data")
  }

  private def githubAllSuggestions: F[GithubKarmaSuggestItems] = {
    scalacache.memoization.memoizeF(None) {
      githubService.languages.map(languages => GithubKarmaSuggestItems(languages.asKarmaItems))
    }
  }

  private def stackExchangeTags: F[StackExchangeItems] = {
    scalacache.memoization.memoizeF(None) {
      for {
        exists <- Sync[F].delay(stackExchangeFileCache.exists)
        _ <- if(!exists) fetchAllStackExchangeTagsToFileCache else Sync[F].unit
        tags <- loadAllStackExchangeTagsFromFileCache
      } yield StackExchangeItems(tags.sortBy(_.tag.count).map(_.asKarmaItem))
    }
  }

  private def loadAllStackExchangeTagsFromFileCache: F[List[SiteStackExchangeTag]] = {
    for {
      lines <- Sync[F].delay(stackExchangeFileCache.lines)
      parsedLines <- lines.toList.traverse { line =>
        parser.parse(line).flatMap(_.as[SiteStackExchangeTag]).liftTo[F]
      }
    } yield parsedLines
  }

  private def fetchAllStackExchangeTagsToFileCache: F[Unit] = {
    for {
      _ <- Logger[F].info("Start fetching all stack exchange tags")
      _ <- Sync[F].delay(stackExchangeFileCache.parent.createDirectories())
      _ <- {
        (stackExchangeSites >>= fetchStackExchangeTags).
          map(_.asJson.noSpaces).
          evalMap(json => Sync[F].delay(stackExchangeFileCache.append(json))).
          compile.
          drain
      }
      _ <- Logger[F].info("Finished fetching all stack exchange tags")
    } yield ()
  }

  private def fetchStackExchangeTags(site: StackExchangeSite): Stream[F, SiteStackExchangeTag] = {
    def tags(page: Int): F[StackExchangeTags] = {
      val siteName = site.api_site_parameter
      for {
        _ <- Logger[F].info(s"Start fetching StackExchange tags at page: $page for site $siteName")
        tags <- stackExchangeService.tags(page, 100, siteName)
        _ <- Logger[F].info(s"Finished fetching StackExchange tags at page: $page")
        _ <- Timer[F].sleep(1.second)
      } yield tags
    }

    for {
      tags <- Stream.fromIterator(Iterator.from(1)).evalMap(tags).takeWhile(_.has_more)
      tag <- Stream.fromIterator(tags.items.iterator)
    } yield SiteStackExchangeTag(site, tag)
  }

  private def stackExchangeSites: Stream[F, StackExchangeSite] = {
    def sites(page: Int): F[StackExchangeSites] = {
      for {
        _ <- Logger[F].info(s"Start fetching StackExchange sites at page: $page")
        sites <- stackExchangeService.sites(page, 100)
        _ <- Logger[F].info(s"Finished fetching StackExchange sites at page: $page")
        _ <- Timer[F].sleep(1.second)
      } yield sites
    }

    for {
      sites <- Stream.fromIterator(Iterator.from(1)).evalMap(sites).takeWhile(_.has_more)
      site <- Stream.fromIterator(sites.items.iterator)
    } yield site
  }

  private def normalize(string: String): String = string.toLowerCase.trim.split(" ").mkString(" ")
}

object FeedSuggestionsService {
  case class GithubKarmaSuggestItems(items: List[KarmaSuggestItem])
  case class StackExchangeItems(items: List[KarmaSuggestItem])
}
