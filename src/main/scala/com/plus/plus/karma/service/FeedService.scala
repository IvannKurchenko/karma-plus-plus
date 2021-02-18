package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.all._
import cats.effect._
import com.plus.plus.karma.model._
import com.plus.plus.karma.model.KarmaFeedItemSources.{KarmaFeedItemSource, _}
import com.plus.plus.karma.model.github.GithubLanguageIndex
import com.plus.plus.karma.model.stackexchange.{StackExchangeSite, StackExchangeTag}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import scalacache.{Cache, Mode}
import scalacache.caffeine.CaffeineCache

import scala.concurrent.duration._

class FeedService[F[_] : Mode : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                             redditService: RedditService[F],
                                                             stackExchangeService: StackExchangeService[F])
                                                            (implicit A: Applicative[F]) {

  private implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  implicit val languageIndexCache: Cache[GithubLanguageIndex] = CaffeineCache[GithubLanguageIndex]
  implicit val stackExchangeTagsCache: Cache[List[StackExchangeTag]] = CaffeineCache[List[StackExchangeTag]]

  def feed(request: KarmaFeedRequest): F[List[KarmaFeedItem]] = {
    for {
      github <- sourceFeed(request, KarmaFeedItemSources.Github)(githubFeed)
      reddit <- sourceFeed(request, KarmaFeedItemSources.Reddit)(redditFeed)
    } yield (github ++ reddit).sortBy(_.created).reverse
  }

  def suggestions(term: String): F[KarmaSuggest] = {
    val normalized = normalize(term)
    if (normalized.nonEmpty) {
      for {
        reddit <- redditSuggestions(normalized)
        github <- githubSuggestions(normalized)
      } yield {
        KarmaSuggest(github ++ reddit)
      }
    } else {
      KarmaSuggest.empty.pure
    }
  }

  private def sourceFeed(request: KarmaFeedRequest, source: KarmaFeedItemSource)
                        (f: List[String] => F[List[KarmaFeedItem]]): F[List[KarmaFeedItem]] = {
    val items = request.source(source)
    if (items.nonEmpty) f(items) else List.empty[KarmaFeedItem].pure
  }

  private def githubFeed(items: List[String]): F[List[KarmaFeedItem]] = {
    githubService.searchIssues(items).map(_.items.map(_.asKarmaFeedItem))
  }

  private def redditFeed(items: List[String]): F[List[KarmaFeedItem]] = {
    items.traverse(redditService.subredditsPosts).map(_.flatMap(_.data.children.map(_.data.asKarmaFeedItem)))
  }

  private def redditSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    redditService.autocomplete(term).map(_.subreddits.filterNot(_.numSubscribers == 0).map(_.toKarmaSuggest))
  }

  private def githubSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    githubLanguages.map { languages =>
      languages.asKarmaItems.filter(language => normalize(language.name).startsWith(term))
    }
  }

  /*
   * It is totally not functional approach to change service internal state via memorization, but otherwise
   * it will overcomplicate all other DI
   */
  def prefetchSuggestionData: F[Unit] = {
    (githubLanguages *> stackExchangeTags).void
  }

  private def githubLanguages: F[GithubLanguageIndex] = {
    scalacache.memoization.memoizeF(None)(githubService.languages)
  }

  private def stackExchangeTags: F[List[StackExchangeTag]] = {
    scalacache.memoization.memoizeF(None) {
      for {
        sites <- stackExchangeSites()
        tags <- sites.traverse(fetchStackExchangeTags(_))
      } yield tags.flatten.sortBy(_.count)
    }
  }

  private def fetchStackExchangeTags(site: StackExchangeSite, page: Int = 1): F[List[StackExchangeTag]] = {
    val siteName = site.api_site_parameter
    for {
      _ <- Logger[F].info(s"Start fetching StackExchange tags at page: $page for site $siteName")
      sites <- stackExchangeService.tags(page, 100, siteName)
      _ <- Logger[F].info(s"Finished fetching StackExchange tags at page: $page")
      _ <- implicitly[Timer[F]].sleep(200.millis)
      nextSites <- if(sites.has_more) fetchStackExchangeTags(site, page + 1) else Nil.pure
    } yield sites.items ++ nextSites
  }

  private def stackExchangeSites(page: Int = 1): F[List[StackExchangeSite]] = {
    for {
      _ <- Logger[F].info(s"Start fetching StackExchange sites at page: $page")
      sites <- stackExchangeService.sites(page, 100)
      _ <- Logger[F].info(s"Finished fetching StackExchange sites at page: $page")
      nextSites <- if(sites.has_more) stackExchangeSites(page + 1) else Nil.pure
    } yield sites.items ++ nextSites
  }

  private def normalize(string: String): String = string.toLowerCase.trim.split(" ").mkString(" ")
}
