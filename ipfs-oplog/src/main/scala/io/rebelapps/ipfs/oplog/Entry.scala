package io.rebelapps.ipfs.oplog

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder

case class Entry(sequenceNumber: Long,
                 maybeNext: Option[Hash],
                 payload: Payload) {

  lazy val isRoot: Boolean = sequenceNumber == 0

}

object Entry {

  private[oplog] val RootEntry = Payload("ROOT", "ROOT", "ROOT")

  private[oplog] val Root = Entry(0, None, RootEntry)

  implicit val payloadEncoder: Encoder[Payload] = deriveEncoder

  implicit val payloadDecoder: Decoder[Payload] = deriveDecoder

  implicit val entryEncoder: Encoder[Entry] = deriveEncoder

  implicit val entryDecoder: Decoder[Entry] = deriveDecoder

}