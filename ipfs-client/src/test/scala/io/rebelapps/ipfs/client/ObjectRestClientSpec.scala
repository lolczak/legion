package io.rebelapps.ipfs.client

import cats.effect.IO
import io.rebelapps.test.{DockerSetUp, Tcp}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers}

class ObjectRestClientSpec extends FlatSpec with Matchers with BeforeAndAfterAll with DockerSetUp with Inside {

  "An Ipfs client" should "store byte arrays" in {
    //given
    //when
    val result = objectUnderTest.objectOps.put("test").unsafeRunSync()
    //then
    inside(result) { case Right(response) =>
      response.Hash shouldNot be (empty)
    }
  }

  lazy val dockerComposeFile = "./ipfs-client/src/test/resources/test-docker-compose.yml"

  lazy val localBindAddress = "127.0.0.1"

  lazy val restPort = 5001

  val objectUnderTest = new IpfsRestClient[IO](localBindAddress, restPort)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    logger.debug("Waiting for ipfs")
    Tcp.waitForPort(localBindAddress, restPort)
    Thread.sleep(10000)
  }

}
