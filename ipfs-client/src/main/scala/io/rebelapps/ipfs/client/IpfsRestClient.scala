package io.rebelapps.ipfs.client

import cats.effect.{ConcurrentEffect, Resource}
import io.rebelapps.ipfs.api.{IpfsApi, ObjectOps}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

class IpfsRestClient[F[_]](host: String, port: Int = 5001)
                          (implicit F: ConcurrentEffect[F], ec: ExecutionContext)
  extends IpfsApi[F] {

  private val clientResource: Resource[F, Client[F]] = BlazeClientBuilder[F](ec).resource

  override val objectOps: ObjectOps[F] = new ObjectRestClient[F](clientResource, host, port)

}
