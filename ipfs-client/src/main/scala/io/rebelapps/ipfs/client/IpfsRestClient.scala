package io.rebelapps.ipfs.client

import cats.effect.ConcurrentEffect
import io.rebelapps.ipfs.api.{IpfsApi, ObjectOps}

import scala.language.higherKinds

class IpfsRestClient[F[_]](host: String, port: Int = 5001)
                          (implicit F: ConcurrentEffect[F])
  extends IpfsApi[F] {

  override val objectOps: ObjectOps[F] = new ObjectRestClient[F](host, port)

}
