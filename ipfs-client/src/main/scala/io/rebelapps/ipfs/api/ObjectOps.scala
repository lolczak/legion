package io.rebelapps.ipfs.api

import io.circe.{Decoder, Encoder}
import io.rebelapps.ipfs.failure.{GenericFailure, InvalidRequest, InvalidResponse, NotFound}
import io.rebelapps.ipfs.model.{ObjectGetResponse, ObjectPutResponse}
import shapeless._

import scala.language.higherKinds

trait ObjectOps[F[_]] {

  type ObjectPutFailure = GenericFailure :+: InvalidRequest :+: InvalidResponse :+: CNil

  type ObjectGetFailure = NotFound.type :+: GenericFailure :+: InvalidRequest :+: InvalidResponse :+: CNil

  def put(data: String): F[Either[ObjectPutFailure, ObjectPutResponse]]

  def get(key: String): F[Either[ObjectGetFailure, ObjectGetResponse]]

  def putJson[A: Encoder](data: A): F[Either[ObjectPutFailure, ObjectPutResponse]]

  def getJson[A: Decoder](key: String): F[Either[ObjectGetFailure, A]]

}
