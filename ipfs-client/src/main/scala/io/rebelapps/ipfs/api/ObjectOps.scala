package io.rebelapps.ipfs.api

import scala.language.higherKinds

trait ObjectOps[F[_]] {

  type AddResponse = String

  type AddError = String

  type GetResponse = String

  type GetError = String

  def put(data: String): F[Either[AddError, AddResponse]]

  def get(key: String): F[Either[GetError, GetResponse]]

}
