package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.all._
import cats.effect._
import com.plus.plus.karma.utils.collection._
import com.plus.plus.karma.model.{KarmaFeedRequest, _}
import com.plus.plus.karma.model.stackexchange._
import com.plus.plus.karma.service.FeedSuggestionsService._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import scalacache.{Cache, Mode}
import scalacache.caffeine.CaffeineCache

import fs2.Stream

class FeedSuggestionsService[F[_] : Mode : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                                        redditService: RedditService[F],
                                                                        stackExchangeService: StackExchangeService[F])
                                                                       (implicit A: Applicative[F]) {

  private implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  private implicit val languageIndexCache: Cache[GithubKarmaSuggestItems] = CaffeineCache[GithubKarmaSuggestItems]
  private implicit val stackExchangeTagsCache: Cache[StackExchangeItems] = CaffeineCache[StackExchangeItems]

  def autocompleteSuggestions(termPrefix: String): F[KarmaSuggest] = {
    val normalizedPrefix = normalize(termPrefix)
    if (normalizedPrefix.nonEmpty) {
      for {
        reddit <- autocompleteRedditSuggestions(normalizedPrefix)
        github <- autocompleteGithubSuggestions(normalizedPrefix)
        stackExchange <- autocompleteStackExchangeSuggestions(normalizedPrefix)
      } yield KarmaSuggest(List(github, reddit, stackExchange).merge)
    } else {
      KarmaSuggest.empty.pure
    }
  }

  def exactSuggestions(request: KarmaFeedRequest): F[KarmaSuggest] = {
    for {
      reddit <- exactRedditSuggestions(request.source(KarmaFeedItemSources.Reddit))
      github <- exactGithubSuggestions(request.source(KarmaFeedItemSources.Github))
      stackExchange <- exactStackExchangeSuggestions(request.source(KarmaFeedItemSources.StackExchange))
    } yield KarmaSuggest(reddit ++ github ++ stackExchange)
  }

  private def autocompleteRedditSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    redditService.autocomplete(term).map(_.subreddits.filterNot(_.numSubscribers == 0).map(_.toKarmaSuggest))
  }

  private def exactRedditSuggestions(items: List[KarmaFeedItemRequest]): F[List[KarmaSuggestItem]] = {
    items.traverse { item =>
      val normalItem = item.name.toLowerCase
      autocompleteRedditSuggestions(normalItem).map(_.filter(_.name == normalItem))
    }.map(_.flatten)
  }

  private def autocompleteGithubSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    githubAllSuggestions.map(_.items.filter(language => normalize(language.name).startsWith(term)))
  }

  private def exactGithubSuggestions(items: List[KarmaFeedItemRequest]): F[List[KarmaSuggestItem]] = {
    val names = items.map(_.name).map(normalize).toSet
    githubAllSuggestions.map(_.items.filter(language => names.contains(normalize(language.name))))
  }

  private def autocompleteStackExchangeSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    stackExchangeTags.map(_.items.filter(_.name.startsWith(term)))
  }

  private def exactStackExchangeSuggestions(items: List[KarmaFeedItemRequest]): F[List[KarmaSuggestItem]] = {
    val names = items.map(_.name).toSet
    stackExchangeTags.map(_.items.filter(tag => names.contains(tag.name)))
  }

  /*
   * It is totally not functional approach to change service internal state via memorization, but otherwise
   * it will overcomplicate all other DI
   */
//  def prefetchSuggestionData: F[Unit] = {
//    Logger[F].info("Start prefetching internal data") *>
//      githubAllSuggestions *>
//      stackExchangeTags *>
//      Logger[F].info("Finished prefetching internal data")
//  }

  private def githubAllSuggestions: F[GithubKarmaSuggestItems] = {
    scalacache.memoization.memoizeF(None) {
      githubService.languages.map(languages => GithubKarmaSuggestItems(languages.asKarmaItems))
    }
  }

  private def stackExchangeTags: F[StackExchangeItems] = {
    /*scalacache.memoization.memoizeF(None) {
      for {
        exists <- Sync[F].delay(stackExchangeFileCache.exists)
        _ <- if(!exists) fetchAllStackExchangeTagsToFileCache else Sync[F].unit
        tags <- loadAllStackExchangeTagsFromFileCache
      } yield StackExchangeItems(tags.sortBy(_.tag.count).map(_.asKarmaItem))
    }*/
    StackExchangeItems(Nil).pure
  }

  /*private def loadAllStackExchangeTagsFromFileCache: F[List[SiteStackExchangeTag]] = {
    for {
      _ <- Logger[F].info("Start fetching all stack exchange tags from local file cache")
      lines <- Sync[F].delay(stackExchangeFileCache.lines)
      parsedLines <- lines.toList.traverse { line =>
        parser.parse(line).flatMap(_.as[SiteStackExchangeTag]).liftTo[F]
      }
      _ <- Logger[F].info("Finished fetching all stack exchange tags from local file cache")
    } yield parsedLines
  }

  private def fetchAllStackExchangeTagsToFileCache: F[Unit] = {
    for {
      _ <- Logger[F].info("Start fetching all stack exchange tags")
      _ <- Sync[F].delay(stackExchangeFileCache.parent.createDirectories())
      _ <- {
        (stackExchangeSites >>= fetchStackExchangeTags).
          map(_.asJson.noSpaces).
          chunkN(100).
          evalMap(json => Sync[F].delay(stackExchangeFileCache.appendLines(json.toList:_*))).
          compile.
          drain
      }
      _ <- Logger[F].info("Finished fetching all stack exchange tags")
    } yield ()
  }*/

  private def fetchStackExchangeTags(site: StackExchangeSite): Stream[F, SiteStackExchangeTag] = {
    def tags(page: Int): F[StackExchangeTags] = {
      val siteName = site.api_site_parameter
      for {
        _ <- Logger[F].info(s"Start fetching StackExchange tags at page: $page for site $siteName")
        tags <- stackExchangeService.tags(page, 100, siteName)
        _ <- Logger[F].info(s"Finished fetching StackExchange tags at page: $page")
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
