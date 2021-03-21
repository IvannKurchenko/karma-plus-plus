package com.plus.plus.karma.service

import cats.effect._
import cats.implicits._
import com.plus.plus.karma.model.KarmaSuggestItem
import com.plus.plus.karma.model.stackexchange.{SiteStackExchangeTag, StackExchangeTag}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import com.plus.plus.karma.service.StackExchangeTagsLoader._
import com.plus.plus.karma.utils.collection.PrefixTree
import io.circe.parser._
import io.circe.generic.auto._
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._

import java.net.URI
import scala.io.Source
import com.plus.plus.karma.utils.json._
import io.chrisdavenport.log4cats.Logger

/**
 * Loads stack exchange tags from resources
 */
class StackExchangeTagsLoader[F[_]: Sync] {
  private implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  def load: F[PrefixTree[KarmaSuggestItem]] = {
    for {
      _ <- Logger[F].info("Starting loading tags from resources")
      index <- loadIndex
      tags <- index.index.traverse(readCsv)
      _ <- Logger[F].info("Finished loading tags from resources")
    } yield {
      val indexedTags: Seq[(String, KarmaSuggestItem)] = tags.flatten.map(siteTag => siteTag.tag.name -> siteTag.asKarmaItem)
      PrefixTree.create(indexedTags)
    }
  }

  private def readCsv(item: StackExchangeIndexFileItem): F[List[SiteStackExchangeTag]] = {
    implicit val decoder: HeaderDecoder[StackExchangeTag] = {
      HeaderDecoder.decoder[String, Int, StackExchangeTag]("TagName", "Count")(StackExchangeTag.apply)
    }

    for {
      results <- Sync[F].delay {
        val resource = Thread.currentThread().getContextClassLoader.getResource(s"stackexchange/${item.file}")
        resource.readCsv[List, StackExchangeTag](rfc.withHeader)
      }
      tags <- results.traverse(Sync[F].fromEither)
    } yield tags.map(tag => SiteStackExchangeTag(item.site, item.name, item.api, tag))
  }

  private def loadIndex: F[StackExchangeIndex] = {
    Resource.fromAutoCloseable(Sync[F].delay(Source.fromResource("stackexchange/meta/index.json"))).use { index =>
      Sync[F].fromEither(parse(index.mkString).flatMap(_.as[StackExchangeIndex]))
    }
  }
}

object StackExchangeTagsLoader {
  case class StackExchangeIndexFileItem(file: String, site: URI, name: String, api: String)
  case class StackExchangeIndex(index: Seq[StackExchangeIndexFileItem])
}