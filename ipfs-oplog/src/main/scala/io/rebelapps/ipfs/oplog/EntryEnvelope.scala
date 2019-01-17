package io.rebelapps.ipfs.oplog

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder

case class EntryEnvelope(sequenceNumber: Long,
                         maybeNext: Option[Hash],
                         entry: Payload) {

  lazy val isRoot: Boolean = sequenceNumber == 0

}

object EntryEnvelope {

  private[oplog] val RootEntry = Payload("ROOT", "ROOT")

  private[oplog] val Root = EntryEnvelope(0, None, RootEntry)

  implicit val entryEncoder: Encoder[Payload] = deriveEncoder

  implicit val entryDecoder: Decoder[Payload] = deriveDecoder

  implicit val envelopeEncoder: Encoder[EntryEnvelope] = deriveEncoder

  implicit val envelopeDecoder: Decoder[EntryEnvelope] = deriveDecoder

}