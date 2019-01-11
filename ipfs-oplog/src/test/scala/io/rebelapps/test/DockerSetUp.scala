package io.rebelapps.test

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.BeforeAndAfterAll

import scala.language.postfixOps
import scala.sys.process._

trait DockerSetUp extends LazyLogging {
  this: BeforeAndAfterAll =>

  val dockerComposeFile: String

  def startContainers(): Unit = {
    val exitCode = s"docker-compose -f $dockerComposeFile up -d" !

    logger.debug(s"Containers started with exit code: $exitCode")
  }

  def stopContainers(): Unit = {
    val exitCode = s"docker-compose -f $dockerComposeFile down" !

    logger.debug(s"Containers started with exit code: $exitCode")
  }

  override protected def afterAll(): Unit = {
    logger.debug("Stopping docker containers")
    stopContainers()
  }

  override protected def beforeAll(): Unit = {
    logger.debug("Starting docker containers")
    startContainers()
  }

}
