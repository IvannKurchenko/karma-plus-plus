package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.all._
import cats.effect._
import com.plus.plus.karma.model._
import com.plus.plus.karma.model.KarmaFeedItemSources.{KarmaFeedItemSource, _}
import com.plus.plus.karma.model.stackexchange.{SiteStackExchangeTag, StackExchangeSite}
import com.plus.plus.karma.service.FeedService._
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

  private implicit val languageIndexCache: Cache[GithubKarmaSuggestItems] = CaffeineCache[GithubKarmaSuggestItems]
  private implicit val stackExchangeTagsCache: Cache[StackExchangeItems] = CaffeineCache[StackExchangeItems]

  def feed(request: KarmaFeedRequest): F[List[KarmaFeedItem]] = {
    for {
      github <- sourceFeed(request, KarmaFeedItemSources.Github)(githubFeed)
      reddit <- sourceFeed(request, KarmaFeedItemSources.Reddit)(redditFeed)
      stackExchange <- sourceFeed(request, KarmaFeedItemSources.StackExchange)(stackExchangeFeed)
    } yield (github ++ reddit).sortBy(_.created).reverse
  }

  def suggestions(term: String): F[KarmaSuggest] = {
    val normalized = normalize(term)
    if (normalized.nonEmpty) {
      for {
        reddit <- redditSuggestions(normalized)
        github <- githubSuggestions(normalized)
        stackExchange <- stackExchangeSuggestions(normalized)
      } yield {
        KarmaSuggest(github ++ reddit ++ stackExchange)
      }
    } else {
      KarmaSuggest.empty.pure
    }
  }

  private def sourceFeed(request: KarmaFeedRequest, source: KarmaFeedItemSource)
                        (f: (List[String], Int) => F[List[KarmaFeedItem]]): F[List[KarmaFeedItem]] = {
    val items = request.source(source)
    if (items.nonEmpty) f(items, request.page) else List.empty[KarmaFeedItem].pure
  }

  private def githubFeed(items: List[String], page: Int): F[List[KarmaFeedItem]] = {
    githubService.searchIssues(items, page, 10).map(_.items.map(_.asKarmaFeedItem))
  }

  private def redditFeed(items: List[String], page: Int): F[List[KarmaFeedItem]] = {
    val limit = 10
    val count = limit * page
    items.traverse(redditService.subredditsPosts(_, limit, count)).map(_.flatMap(_.data.children.map(_.data.asKarmaFeedItem)))
  }

  private def stackExchangeFeed(items: List[String], page: Int): F[List[KarmaFeedItem]] = {
    val pageSize = 10
    ???///items.traverse(stackExchangeService.questions(_, pageSize, )).
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
        sites <- stackExchangeSites()
        tags <- sites.traverse(fetchStackExchangeTags(_))
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

object FeedService {
  case class GithubKarmaSuggestItems(items: List[KarmaSuggestItem])
  case class StackExchangeItems(items: List[KarmaSuggestItem])
}
