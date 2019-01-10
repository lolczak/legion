package io.rebelapps.ipfs.client

import cats.effect.Effect
import cats.implicits._
import fs2.Stream
import io.circe.{Decoder, Encoder}
import io.circe.parser._
import io.circe.syntax._
import io.rebelapps.ipfs.api.ObjectOps
import io.rebelapps.ipfs.failure.{GenericFailure, InvalidRequest, InvalidResponse}
import io.rebelapps.ipfs.model.{DataEnvelope, ObjectGetResponse, ObjectPutResponse}
import org.http4s.client.blaze._
import org.http4s.headers._
import org.http4s.multipart._
import org.http4s.{Charset, EntityEncoder, MediaType, Method, Request, Response, Status, Uri}
import shapeless._

import scala.language.higherKinds

class ObjectRestClient[F[_]](host: String, port: Int = 5001)
                            (implicit E: Effect[F])
  extends ObjectOps[F] {

  private val clientF = Http1Client[F]()

  override def put(data: String): F[Either[ObjectPutFailure, ObjectPutResponse]] = {
    val url = Uri.unsafeFromString(s"http://$host:$port/api/v0/object/put?inputenc=json&datafieldenc=text")
    val envelop = DataEnvelope(data).asJson.noSpaces
    val payload = Stream.emits(envelop.getBytes("UTF-8").toSeq).evalMap(E.point)

    val multipart = Multipart[F](
      Vector(
        Part(
          `Content-Disposition`("form-data", Map("name" -> "file", "filename" -> "json")) +:
          `Content-Type`(MediaType.`application/octet-stream`, Charset.`UTF-8`) +: Seq.empty,
          payload)
      ))

    val bodyF = implicitly[EntityEncoder[F, Multipart[F]]].toEntity(multipart)

    for {
      httpClient <- clientF
      body       <- bodyF
      request     = Request(Method.POST, url, headers = multipart.headers, body = body.body)
      response   <- httpClient.fetch[Either[InvalidRequest, String]](request)(responseHandler)
      result      = response.fold(err => Left(Coproduct[ObjectPutFailure](err)), parsePutResponse)
    } yield result
  }

  private val responseHandler: Response[F] =|> F[Either[InvalidRequest, String]] = {
    case resp if resp.status == Status.Ok =>
      readString(resp.body).map(_.asRight)

    case resp =>
      readString(resp.body).map(body => InvalidRequest(resp.status.code, body).asLeft)
  }

  private def parsePutResponse(body: String): Either[ObjectPutFailure, ObjectPutResponse] =
    decode[ObjectPutResponse](body)
      .leftMap { err => Coproduct[ObjectPutFailure](InvalidResponse(200, body, err.toString)) }

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

  private def readString(stream: Stream[F, Byte]): F[String] =
    stream
      .compile
      .toList
      .map(bytes => new String(Array(bytes: _*)))

  override def putJson[A: Encoder](data: A): F[Either[ObjectPutFailure, ObjectPutResponse]] = put(data.asJson.noSpaces)

  override def getJson[A: Decoder](key: String): F[Either[GetError, A]] =
    get(key) map {
      case Left(err)       => Left(err)
      case Right(response) => decode[A](response.Data).leftMap(_.toString + " -> " + response.toString)
    }

}
