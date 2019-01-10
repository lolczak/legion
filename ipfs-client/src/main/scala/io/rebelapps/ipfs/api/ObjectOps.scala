package io.rebelapps.ipfs.api

import io.circe.{Decoder, Encoder}
import io.rebelapps.ipfs.failure.{GenericFailure, InvalidRequest, InvalidResponse}
import io.rebelapps.ipfs.model.{ObjectGetResponse, ObjectPutResponse}
import shapeless._

import scala.language.higherKinds

trait ObjectOps[F[_]] {

  type ObjectPutFailure = GenericFailure :+: InvalidRequest :+: InvalidResponse :+: CNil

  type GetError = String

  def put(data: String): F[Either[ObjectPutFailure, ObjectPutResponse]]

  def get(key: String): F[Either[GetError, ObjectGetResponse]]

  def putJson[A: Encoder](data: A): F[Either[ObjectPutFailure, ObjectPutResponse]]

  def getJson[A: Decoder](key: String): F[Either[GetError, A]]

}
