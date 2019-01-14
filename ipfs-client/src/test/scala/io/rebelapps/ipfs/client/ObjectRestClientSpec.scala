package io.rebelapps.ipfs.client

import cats.effect._
import cats.effect.internals.IOContextShift
import cats.implicits._
import io.rebelapps.test.{DockerSetUp, Tcp}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers}

import scala.concurrent.ExecutionContext.global
class ObjectRestClientSpec extends FlatSpec with Matchers with BeforeAndAfterAll with DockerSetUp with Inside {

  "An Ipfs client" should "persist strings" in {
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

  it should "persist json objects" in {
    //given
    import io.circe.generic.auto._
    case class TestData(name: String, count: Long)
    val TestJson = TestData("test", 100)
    //when
    val (putResult, getResult) = {
      for {
        putResult <- objectUnderTest.objectOps.putJson[TestData](TestJson)
        getResult <- putResult.fold(err => IO.pure(err.asLeft), putResp => objectUnderTest.objectOps.getJson[TestData](putResp.Hash))
      } yield (putResult, getResult)
    } unsafeRunSync()
    //then
    inside(putResult) { case Right(response) =>
      response.Hash shouldNot be (empty)
    }
    inside(getResult) { case Right(response) =>
      response shouldBe TestJson
    }
  }

  lazy val dockerComposeFile = "./ipfs-client/src/test/resources/test-docker-compose.yml"

  lazy val localBindAddress = "127.0.0.1"

  lazy val restPort = 5001

  implicit val ioCtxShift = IOContextShift(global)

  val objectUnderTest = new IpfsRestClient[IO](localBindAddress, restPort)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    logger.debug("Waiting for ipfs")
    Tcp.waitForPort(localBindAddress, restPort)
    Thread.sleep(10000)
  }

}
