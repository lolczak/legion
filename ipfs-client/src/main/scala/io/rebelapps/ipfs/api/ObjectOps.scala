package io.rebelapps.ipfs.api

import io.rebelapps.ipfs.model.{ObjectGetResponse, ObjectPutResponse}

import scala.language.higherKinds

trait ObjectOps[F[_]] {

  type ObjectPutError = String

  type GetError = String

  def put(data: String): F[Either[ObjectPutError, ObjectPutResponse]]

  def get(key: String): F[Either[GetError, ObjectGetResponse]]

}
