package com.plus.plus.karma.service

import cats.syntax.functor._
import cats.effect._
import com.plus.plus.karma.model.{KarmaFeedItemSources, KarmaSuggest, KarmaSuggestItem}

class FeedService[F[_]: Sync: ContextShift: Timer](githubService: GithubService[F]) {
  def feed = {
    ???//githubService.searchIssues()
  }

  def suggestions(term: String): F[KarmaSuggest] = {
    val normalized = normalize(term)
    githubSuggestions(normalized).map(KarmaSuggest.apply)
  }

  private def githubSuggestions(term: String): F[List[KarmaSuggestItem]] = {
    githubService.languages.map { languages =>
      languages.filter {
        case (language, _) => normalize(language).startsWith(term)
      }.toList.map {
        case (name, language) =>
          val description = s"${language.`type`} programming language in GitHub"
          KarmaSuggestItem(name, KarmaFeedItemSources.Github, description)
      }
    }
  }

  private def normalize(string: String) = string.toLowerCase.trim.split(" ").mkString(" ")
}
