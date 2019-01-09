package io.rebelapps.ipfs.api

import io.circe.{Decoder, Encoder}
import io.rebelapps.ipfs.model.{ObjectGetResponse, ObjectPutResponse}

import scala.language.higherKinds

trait ObjectOps[F[_]] {

  type ObjectPutError = String

  type GetError = String

  def put(data: String): F[Either[ObjectPutError, ObjectPutResponse]]

  def get(key: String): F[Either[GetError, ObjectGetResponse]]

  def putJson[A: Encoder](data: A): F[Either[ObjectPutError, ObjectPutResponse]]

  def getJson[A: Decoder](key: String): F[Either[GetError, A]]

}
