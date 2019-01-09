package io.rebelapps.ipfs.api

import scala.language.higherKinds

trait IpfsApi[F[_]] {

  def objectOps: ObjectOps[F]

}
