package io.rebelapps.ipfs.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class ObjectGetResponse(Data: String,
                             Links: List[Link])

case class Link(Name: String,
                Hash: String,
                Size: Long)

object ObjectGetResponse {

  implicit val linkDecoder: Decoder[Link] = deriveDecoder

  implicit val ogrDecoder: Decoder[ObjectGetResponse] = deriveDecoder

}