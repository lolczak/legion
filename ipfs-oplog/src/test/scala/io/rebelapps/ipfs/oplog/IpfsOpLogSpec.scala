package io.rebelapps.ipfs.oplog

import cats.effect.IO
import cats.implicits._
import io.rebelapps.ipfs.client.IpfsRestClient
import io.rebelapps.test.{DockerSetUp, Tcp}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers}

class IpfsOpLogSpec extends FlatSpec with Matchers with BeforeAndAfterAll with DockerSetUp with Inside {

  "An IPFS op log" should "create new op log" in {
    //given
    val log = IpfsOpLog.createNew[IO](ipfs).unsafeRunSync()
    val entry1 = Entry("test-op1", "test1")
    val entry2 = Entry("test-op2", "test2")
    //when
    val result = (log.append(entry1) >> log.append(entry2) >> log.entries()).unsafeRunSync()
    //then
    result shouldBe List(entry1, entry2)
  }

  lazy val dockerComposeFile = "./ipfs-client/src/test/resources/test-docker-compose.yml"

  lazy val localBindAddress = "127.0.0.1"

  lazy val restPort = 5001

  lazy val ipfs = new IpfsRestClient[IO](localBindAddress, restPort)


  override protected def beforeAll(): Unit = {
    super.beforeAll()
    logger.debug("Waiting for ipfs")
    Tcp.waitForPort(localBindAddress, restPort)
    Thread.sleep(10000)
  }

}
