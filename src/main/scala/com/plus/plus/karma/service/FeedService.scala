package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.functor._
import cats.effect._
import com.plus.plus.karma.model._
import com.plus.plus.karma.model.KarmaFeedItemSources._

class FeedService[F[_]: Sync: ContextShift: Timer](githubService: GithubService[F])
                                                  (implicit A: Applicative[F]) {
  def feed(request: KarmaFeedRequest): F[List[KarmaFeedItem]] = {
    githubFeed(request)
  }

  def suggestions(term: String): F[KarmaSuggest] = {
    val normalized = normalize(term)
    githubSuggestions(normalized).map(KarmaSuggest.apply)
  }

  private def githubFeed(request: KarmaFeedRequest): F[List[KarmaFeedItem]] = {
    val items = request.source(Github)
    if(items.nonEmpty) {
      githubService.searchIssues(items).map(_.items.map(_.asKarmaFeedItem))
    } else {
      A.pure(Nil)
    }
  }

  private def githubSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    githubService.languages.map { languages =>
      languages.filter {
        case (language, _) => normalize(language).startsWith(term)
      }.toList.map {
        case (name, language) =>
          val description = s"${language.`type`} language in GitHub"
          KarmaSuggestItem(name, KarmaFeedItemSources.Github, description)
      }
    }
  }

  private def normalize(string: String) = string.toLowerCase.trim.split(" ").mkString(" ")
}
