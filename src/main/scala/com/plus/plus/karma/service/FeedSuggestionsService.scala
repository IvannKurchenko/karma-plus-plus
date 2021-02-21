package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.all._
import cats.effect._
import com.plus.plus.karma.model._
import com.plus.plus.karma.model.stackexchange.{SiteStackExchangeTag, StackExchangeSite}
import com.plus.plus.karma.service.FeedSuggestionsService._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import scalacache.{Cache, Mode}
import scalacache.caffeine.CaffeineCache

import scala.concurrent.duration._

class FeedSuggestionsService[F[_] : Mode : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                                        redditService: RedditService[F],
                                                                        stackExchangeService: StackExchangeService[F])
                                                                       (implicit A: Applicative[F]) {

  private implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  private implicit val languageIndexCache: Cache[GithubKarmaSuggestItems] = CaffeineCache[GithubKarmaSuggestItems]
  private implicit val stackExchangeTagsCache: Cache[StackExchangeItems] = CaffeineCache[StackExchangeItems]


  def suggestions(term: String): F[KarmaSuggest] = {
    val normalized = normalize(term)
    if (normalized.nonEmpty) {
      for {
        reddit <- redditSuggestions(normalized)
        github <- githubSuggestions(normalized)
        stackExchange <- stackExchangeSuggestions(normalized)
      } yield KarmaSuggest(github, reddit, stackExchange)
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
    (githubAllSuggestions *> stackExchangeTags).void
  }

  private def githubAllSuggestions: F[GithubKarmaSuggestItems] = {
    scalacache.memoization.memoizeF(None) {
      githubService.languages.map(languages => GithubKarmaSuggestItems(languages.asKarmaItems))
    }
  }

  private def stackExchangeTags: F[StackExchangeItems] = {
    scalacache.memoization.memoizeF(None) {
      for {
        _ <- Logger[F].info("Start fetching all stack exchange tags")
        sites <- stackExchangeSites()
        tags <- sites.traverse(fetchStackExchangeTags(_))
        _ <- Logger[F].info("Finished fetching all stack exchange tags")
      } yield StackExchangeItems(tags.flatten.sortBy(_.tag.count).map(_.asKarmaItem))
    }
  }

  private def fetchStackExchangeTags(site: StackExchangeSite, page: Int = 1): F[List[SiteStackExchangeTag]] = {
    val siteName = site.api_site_parameter
    for {
      _ <- Logger[F].info(s"Start fetching StackExchange tags at page: $page for site $siteName")
      sites <- stackExchangeService.tags(page, 100, siteName)
      _ <- Logger[F].info(s"Finished fetching StackExchange tags at page: $page")
      _ <- Timer[F].sleep(500.millis)
      nextSites <- if(sites.has_more) fetchStackExchangeTags(site, page + 1) else Nil.pure
    } yield sites.items.map(tag => SiteStackExchangeTag(site, tag)) ++ nextSites
  }

  private def stackExchangeSites(page: Int = 1): F[List[StackExchangeSite]] = {
    for {
      _ <- Logger[F].info(s"Start fetching StackExchange sites at page: $page")
      sites <- stackExchangeService.sites(page, 100)
      _ <- Logger[F].info(s"Finished fetching StackExchange sites at page: $page")
      _ <- Timer[F].sleep(200.millis)
      nextSites <- if(sites.has_more) stackExchangeSites(page + 1) else Nil.pure
    } yield sites.items ++ nextSites
  }

  private def normalize(string: String): String = string.toLowerCase.trim.split(" ").mkString(" ")
}

object FeedSuggestionsService {
  case class GithubKarmaSuggestItems(items: List[KarmaSuggestItem])
  case class StackExchangeItems(items: List[KarmaSuggestItem])
}
