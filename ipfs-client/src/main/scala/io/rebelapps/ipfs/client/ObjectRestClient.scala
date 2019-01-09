package io.rebelapps.ipfs.client

import java.util.concurrent._

import cats.effect.Effect
import cats.implicits._
import fs2.Stream
import io.rebelapps.ipfs.api.ObjectOps
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.multipart._
import org.http4s.{Charset, EntityEncoder, Header, Headers, MediaType, Method, Request, Uri}

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

class ObjectRestClient[F[_]](host: String, port: Int = 5001)
                            (implicit E: Effect[F])
  extends ObjectOps[F] {

  private val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

  private val clientF = Http1Client()

  override def put(data: String): F[Either[AddError, AddResponse]] = {
    val payload =
      s"""{
         |  "data": "$data"
         |}
       """.stripMargin
    val multipart = Multipart[F](
      Vector(
        Part(
          `Content-Disposition`("form-data", Map("name" -> "file", "filename" -> "json")) +:
            `Content-Type`(MediaType.`application/octet-stream`, Charset.`UTF-8`) +: Seq.empty,
          Stream.emits(payload.getBytes("UTF-8").toSeq).evalMap(E.point))
      ))

    val bodyF = implicitly[EntityEncoder[F, Multipart[F]]].toEntity(multipart)

    for {
      httpClient  <- clientF
      body <- bodyF
      req = Request(
        Method.POST,
        Uri.unsafeFromString(s"http://$host:$port/api/v0/object/put?inputenc=json&datafieldenc=text"),
        body = body.body
      )
      response <- httpClient.fetchAs[String](req).attempt
      result = response.fold(th => Left(th.toString), Right(_))
    } yield result
  }

  override def get(key: String): F[Either[GetError, GetResponse]] = ???
}
