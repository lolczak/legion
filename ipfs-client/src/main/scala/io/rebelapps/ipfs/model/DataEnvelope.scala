package io.rebelapps.ipfs.model

import io.circe.Encoder
import io.circe.generic.semiauto._

case class DataEnvelope(data: String)

object DataEnvelope {

  implicit val envelopeEncoder: Encoder[DataEnvelope] = deriveEncoder

}
