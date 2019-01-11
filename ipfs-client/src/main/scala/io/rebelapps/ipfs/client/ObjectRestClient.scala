package io.rebelapps.ipfs.client

import java.io.IOException
import java.net.{ConnectException, SocketException, UnknownHostException}
import java.util.concurrent.TimeoutException

import cats.effect.Effect
import cats.implicits._
import fs2.Stream
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import io.rebelapps.ipfs.api.ObjectOps
import io.rebelapps.ipfs.failure._
import io.rebelapps.ipfs.model.{DataEnvelope, ObjectGetResponse, ObjectPutResponse}
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import org.http4s.client.blaze._
import org.http4s.headers._
import org.http4s.multipart._
import org.http4s.{Charset, EntityEncoder, MediaType, Method, Request, Response, Status, Uri}
import shapeless._
import shapeless.ops.coproduct.Inject

import scala.language.higherKinds

class ObjectRestClient[F[_]](host: String, port: Int = 5001)
                            (implicit E: Effect[F])
  extends ObjectOps[F] {

  private val clientF = Http1Client[F]()

  override def putJson[A: Encoder](data: A): F[Either[ObjectPutFailure, ObjectPutResponse]] = put(data.asJson.noSpaces)

  override def getJson[A: Decoder](key: String): F[Either[ObjectGetFailure, A]] =
    get(key) map {
      case Left(err)       => Left(err)
      case Right(response) => decode[A](response.Data).leftMap { err => Coproduct[ObjectGetFailure](InvalidResponse(response.Data, err.toString)) }
    }

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

    val op =
      for {
        httpClient <- clientF
        body       <- bodyF
        request     = Request(Method.POST, url, headers = multipart.headers, body = body.body)
        response   <- httpClient.fetch[Either[ObjectPutFailure, String]](request)(responseHandler[ObjectPutFailure])
        result      = response.flatMap(parsePutResponse)
      } yield result

    op.attempt
      .map {
        case Left(th)     => handleError[ObjectPutFailure](th).asLeft
        case Right(other) => other
      }
  }

  private def parsePutResponse(body: String): Either[ObjectPutFailure, ObjectPutResponse] =
    decode[ObjectPutResponse](body)
      .leftMap { err => Coproduct[ObjectPutFailure](InvalidResponse(body, err.toString)) }

  override def get(key: String): F[Either[ObjectGetFailure, ObjectGetResponse]] = {
    val url = Uri.unsafeFromString(s"http://$host:$port/api/v0/object/get?arg=$key")
    val op =
      for {
        httpClient <- clientF
        response   <- httpClient.get[Either[ObjectGetFailure, String]](url)(notFoundHandler[ObjectGetFailure] orElse responseHandler[ObjectGetFailure])
        result      = response.flatMap(parseGetResponse)
      } yield result

    op.attempt
      .map {
        case Left(th)     => handleError[ObjectGetFailure](th).asLeft
        case Right(other) => other
      }
  }

  private def handleError[A <: Coproduct](th: Throwable)
                                         (implicit inj1: Inject[A, UnrecognizedFailure],
                                          inj2: Inject[A, NetworkFailure]): A =
    th match {
      case _: TimeoutException     => Coproduct[A](NetworkFailure(createThMsg(th)))
      case _: ConnectException     => Coproduct[A](NetworkFailure(createThMsg(th)))
      case _: SocketException      => Coproduct[A](NetworkFailure(createThMsg(th)))
      case _: UnknownHostException => Coproduct[A](NetworkFailure(createThMsg(th)))
      case _: IOException          => Coproduct[A](NetworkFailure(createThMsg(th)))
      case _                       => Coproduct[A](UnrecognizedFailure(createThMsg(th)))
    }

  private def createThMsg(th: Throwable): String = s"${th.toString}, ${getStackTrace(th)}"

  private def parseGetResponse(body: String): Either[ObjectGetFailure, ObjectGetResponse] =
    decode[ObjectGetResponse](body)
      .leftMap { err => Coproduct[ObjectGetFailure](InvalidResponse(body, err.toString)) }

  private def notFoundHandler[A <: Coproduct](implicit inj: Inject[A, NotFound.type]): Response[F] =|> F[Either[A, String]] = {
    case resp if resp.status == Status.NotFound => E.point(Coproduct[A](NotFound).asLeft)
  }

  private def responseHandler[A <: Coproduct](implicit inj: Inject[A, InvalidRequest]): Response[F] =|> F[Either[A, String]] = {
    case resp if resp.status == Status.Ok =>
      readString(resp.body).map(_.asRight)

    case resp =>
      readString(resp.body).map(body => Coproduct[A](InvalidRequest(resp.status.code, body)).asLeft)
  }

  private def readString(stream: Stream[F, Byte]): F[String] =
    stream
      .compile
      .toList
      .map(bytes => new String(Array(bytes: _*)))

}
