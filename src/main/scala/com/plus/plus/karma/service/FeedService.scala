package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.all._
import cats.effect._

import com.plus.plus.karma.model._
import com.plus.plus.karma.model.KarmaFeedItemSources.{KarmaFeedItemSource, _}
import com.plus.plus.karma.model.github.GithubLanguageIndex
import com.plus.plus.karma.model.reddit.SubredditSearch
import scalacache.{Cache, Mode}
import scalacache.caffeine.CaffeineCache

import scala.util.Random
import scala.concurrent.duration._

class FeedService[F[_] : Mode : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                             redditService: RedditService[F])
                                                            (implicit A: Applicative[F]) {

  implicit val languageIndexCache: Cache[GithubLanguageIndex] = CaffeineCache[GithubLanguageIndex]
  implicit val subRedditCache: Cache[List[SubredditSearch]] = CaffeineCache[List[SubredditSearch]]

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
      Applicative[F].pure(KarmaSuggest.empty)
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
    val languages: F[GithubLanguageIndex] = scalacache.memoization.memoizeF(None)(githubService.languages)
    languages.map { languages =>
      languages.filter {
        case (language, _) => normalize(language).startsWith(term)
      }.toList.map {
        case (name, language) =>
          val description = s"${language.`type`} language in GitHub"
          KarmaSuggestItem(name, KarmaFeedItemSources.Github, description)
      }
    }
  }

  private def normalize(string: String): String = string.toLowerCase.trim.split(" ").mkString(" ")
}
