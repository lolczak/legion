package io.rebelapps.ipfs.client

import cats.effect.Effect
import io.rebelapps.ipfs.api.{IpfsApi, ObjectOps}

import scala.language.higherKinds

class IpfsRestClient[F[_]](host: String, port: Int = 5001)
                          (implicit E: Effect[F])
  extends IpfsApi[F] {

  override val objectOps: ObjectOps[F] = new ObjectRestClient[F](host, port)

}
