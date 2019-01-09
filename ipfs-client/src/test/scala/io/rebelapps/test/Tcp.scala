package io.rebelapps.test

import java.net.{InetSocketAddress, Socket}

import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration

import java.net.{ InetSocketAddress, Socket }

import cats.implicits._

import scala.annotation.tailrec
import scala.concurrent.duration.{ FiniteDuration, _ }

object Tcp {

  def waitForPort(host: String, port: Int, timeout: FiniteDuration = 2 minutes): Either[Throwable, Unit] =
    waitForPort(new InetSocketAddress(host, port), timeout)

  def waitForPort(inetSocketAddress: InetSocketAddress, timeout: FiniteDuration): Either[Throwable, Unit] = {
    val start = System.currentTimeMillis()

    @tailrec
    def waitTillTimedOut(maybeLastTry: Option[Either[Throwable, Unit]] = None): Either[Throwable, Unit] = {
      val elapsedTime = System.currentTimeMillis() - start
      val isTimedOut = timeout.toMillis <= elapsedTime
      if (isTimedOut) {
        maybeLastTry.getOrElse(Left(new IllegalArgumentException("The timeout is too small")))
      } else {
        val result = tryConnect(inetSocketAddress)
        if (result.isRight) result
        else waitTillTimedOut(Some(result))
      }
    }

    waitTillTimedOut()
  }

  private def tryConnect(inetSocketAddress: InetSocketAddress, timeout: Int = 100) =
    Either.catchNonFatal {
      val socket = new Socket()
      socket.connect(inetSocketAddress, timeout)
      socket.getChannel
      socket.close()
    }

}
