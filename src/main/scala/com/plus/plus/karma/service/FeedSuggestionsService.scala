package com.plus.plus.karma.service

import cats.effect._
import cats.syntax.all._
import com.plus.plus.karma.model.{KarmaFeedRequest, _}
import com.plus.plus.karma.service.FeedSuggestionsService._
import com.plus.plus.karma.utils.collection._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import scalacache.caffeine.CaffeineCache
import scalacache.{Cache, Mode}

class FeedSuggestionsService[F[_] : Mode : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                                        redditService: RedditService[F],
                                                                        stackExchangeTags: PrefixTree[KarmaSuggestItem]) {

  private implicit def unsafeLogger = Slf4jLogger.getLogger[F]

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
    stackExchangeTags.prefixSearch(term).toList.pure
  }

  private def exactStackExchangeSuggestions(items: List[KarmaFeedItemRequest]): F[List[KarmaSuggestItem]] = {
    items.flatMap(item => stackExchangeTags.exactSearch(item.name)).pure
  }

  private def githubAllSuggestions: F[GithubKarmaSuggestItems] = {
    scalacache.memoization.memoizeF(None) {
      githubService.languages.map(languages => GithubKarmaSuggestItems(languages.asKarmaItems))
    }
  }

  private def normalize(string: String): String = string.toLowerCase.trim.split(" ").mkString(" ")
}

object FeedSuggestionsService {
  case class GithubKarmaSuggestItems(items: List[KarmaSuggestItem])
  case class StackExchangeItems(items: List[KarmaSuggestItem])
}
