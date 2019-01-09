package io.rebelapps.ipfs.client

import cats.effect.IO
import cats.implicits._
import io.rebelapps.test.{DockerSetUp, Tcp}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers}

class ObjectRestClientSpec extends FlatSpec with Matchers with BeforeAndAfterAll with DockerSetUp with Inside {

  "An Ipfs client" should "store string" in {
    //given
    val TestData = "test"
    //when
    val (putResult, getResult) = {
      for {
        putResult <- objectUnderTest.objectOps.put(TestData)
        getResult <- putResult.fold(err => IO.pure(err.asLeft), putResp => objectUnderTest.objectOps.get(putResp.Hash))
      } yield (putResult, getResult)
    } unsafeRunSync()
    //then
    inside(putResult) { case Right(response) =>
      response.Hash shouldNot be (empty)
    }
    inside(getResult) { case Right(response) =>
      response.Data shouldBe TestData
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
