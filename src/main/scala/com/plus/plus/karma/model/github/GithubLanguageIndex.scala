package com.plus.plus.karma.model.github

import com.plus.plus.karma.model._

import java.net.URI

case class GithubLanguageIndex(index: Map[String, GithubLanguage]) {
  def asKarmaItems: List[KarmaSuggestItem] = {
    index.toList.map { case (name, language) =>
      val description = s"${language.`type`} language in GitHub. Search for open issues with `help_wanted` tag"
      KarmaSuggestItem(
        name = name,
        description = description,
        source = KarmaFeedItemSources.Github,
        site = GithubLanguageIndex.githubSite,
        subSource = ""
      )
    }
  }
}

object GithubLanguageIndex {
  val githubSite: URI = URI.create("https://github.com")
}
