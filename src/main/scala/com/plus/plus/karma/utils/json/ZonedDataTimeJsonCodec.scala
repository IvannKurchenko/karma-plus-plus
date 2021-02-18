package com.plus.plus.karma.utils.json

import io.circe.{Decoder, Encoder}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.language.implicitConversions
import scala.util.Try

trait ZonedDataTimeJsonCodec {
  val defaultFormat: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  def zonedDataTimeEncoder(format: DateTimeFormatter = defaultFormat): Encoder[ZonedDateTime] = Encoder[String].contramap(format.format)
  def zonedDataTimeDecoder(format: DateTimeFormatter = defaultFormat): Decoder[ZonedDateTime] = Decoder[String].emapTry(value => Try(ZonedDateTime.parse(value, format)))
}