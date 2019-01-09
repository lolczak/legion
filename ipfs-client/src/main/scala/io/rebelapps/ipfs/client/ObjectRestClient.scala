package io.rebelapps.ipfs.client

import java.util.concurrent._

import cats.effect.Effect
import cats.implicits._
import fs2.Stream
import io.circe.parser._
import io.circe.syntax._
import io.rebelapps.ipfs.api.ObjectOps
import io.rebelapps.ipfs.model.{DataEnvelope, ObjectGetResponse, ObjectPutResponse}
import org.http4s.Status.Successful
import org.http4s.client.blaze._
import org.http4s.headers._
import org.http4s.multipart._
import org.http4s.{Charset, EntityEncoder, MediaType, Method, Request, Status, Uri}

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

class ObjectRestClient[F[_]](host: String, port: Int = 5001)
                            (implicit E: Effect[F])
  extends ObjectOps[F] {

  private val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

  private val clientF = Http1Client()

  override def put(data: String): F[Either[ObjectPutError, ObjectPutResponse]] = {
    val url = Uri.unsafeFromString(s"http://$host:$port/api/v0/object/put?inputenc=json&datafieldenc=text")
    val payload = DataEnvelope(data).asJson.noSpaces

    val multipart = Multipart[F](
      Vector(
        Part(
          `Content-Disposition`("form-data", Map("name" -> "file", "filename" -> "json")) +:
            `Content-Type`(MediaType.`application/octet-stream`, Charset.`UTF-8`) +: Seq.empty,
          Stream.emits(payload.getBytes("UTF-8").toSeq).evalMap(E.point))
      ))

    val bodyF = implicitly[EntityEncoder[F, Multipart[F]]].toEntity(multipart)

    for {
      httpClient <- clientF
      body <- bodyF
      req = Request(
        Method.POST,
        url,
        headers = multipart.headers,
        body = body.body
      )
      response <- httpClient.fetchAs[String](req).attempt
      result = response.fold(th => Left(th.toString), parsePutResponse)
    } yield result
  }

  private def parsePutResponse(responseStr: String): Either[ObjectPutError, ObjectPutResponse] =
    decode[ObjectPutResponse](responseStr).leftMap(_.toString)

  override def get(key: String): F[Either[GetError, ObjectGetResponse]] = {
    val url = Uri.unsafeFromString(s"http://$host:$port/api/v0/object/get?arg=$key")
    for {
      httpClient <- clientF
      response   <- httpClient.get[Either[GetError, ObjectGetResponse]](url) {
        case resp if resp.status == Status.Ok => readString(resp.body).map(parseGetResponse)
        case resp                             => readString(resp.body).map(Left(_))
      }
    } yield response
  }

  private def parseGetResponse(responseStr: String): Either[GetError, ObjectGetResponse] =
    decode[ObjectGetResponse](responseStr).leftMap(_.toString)

  private def readString(body: Stream[F, Byte]): F[String] =
    body
      .compile
      .toList
      .map(bytes => new String(Array(bytes: _*)))

}
