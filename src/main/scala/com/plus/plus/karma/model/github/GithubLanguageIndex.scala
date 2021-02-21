package com.plus.plus.karma.model.github

import com.plus.plus.karma.model.KarmaFeedItemSources.KarmaFeedItemSource
import com.plus.plus.karma.model._

case class GithubLanguageIndex(index: Map[String, GithubLanguage]) {
  def asKarmaItems: List[KarmaSuggestItem] = {
    index.toList.map { case (name, language) =>
      val description = s"${language.`type`} language in GitHub"
      KarmaSuggestItem(name, description, KarmaFeedItemSources.Github, "")
    }
  }
}