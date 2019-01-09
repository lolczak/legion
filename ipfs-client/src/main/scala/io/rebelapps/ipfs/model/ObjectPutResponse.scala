package io.rebelapps.ipfs.model

import io.circe.Decoder
import io.circe.generic.semiauto._

case class ObjectPutResponse(Hash: String)

object ObjectPutResponse {

  implicit val oprDecoder: Decoder[ObjectPutResponse] = deriveDecoder

}