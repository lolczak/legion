package io.rebelapps.ipfs.oplog

import cats.effect.IO
import cats.effect.internals.IOContextShift
import cats.implicits._
import io.rebelapps.ipfs.client.IpfsRestClient
import io.rebelapps.test.{DockerSetUp, Tcp}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class IpfsOpLogSpec extends FlatSpec with Matchers with BeforeAndAfterAll with DockerSetUp with Inside {

  "An IPFS op log" should "create new op log" in {
    //given
    val log = IpfsOpLog.createNew[IO](ipfs).unsafeRunSync()
    val entry1 = Payload("my-set", "test-op1", "test1")
    val entry2 = Payload("my-set", "test-op2", "test2")
    //when
    val result = (log.append(entry1) >> log.append(entry2) >> log.entries()).unsafeRunSync()
    //then
    result shouldBe List(entry1, entry2)
  }

  it should "update a log head" in {
    //given
    val log1 = IpfsOpLog.createNew[IO](ipfs).unsafeRunSync()
    val log2 = IpfsOpLog.createNew[IO](ipfs).unsafeRunSync()
    val entry1 = Payload("my-set", "test-op1", "test1")
    val entry2 = Payload("my-set", "test-op2", "test2")
    val entry3 = Payload("my-set", "test-op3", "test3")
    //when
    val head1 = log1.append(entry1).unsafeRunSync()
    val result1 = log2.updateHead(head1).unsafeRunSync()
    val head2 = (log1.append(entry2) >> log1.append(entry3)).unsafeRunSync()
    val result2 = log2.updateHead(head2).unsafeRunSync()
    val result3 = log2.entries().unsafeRunSync()
    //then
    result1 shouldBe List(entry1)
    result2 shouldBe List(entry2, entry3)
    result3 shouldBe List(entry1, entry2, entry3)
  }

  lazy val dockerComposeFile = "./ipfs-client/src/test/resources/test-docker-compose.yml"

  lazy val localBindAddress = "127.0.0.1"

  lazy val restPort = 5001

  implicit val ioCtxShift = IOContextShift(global)

  lazy val ipfs = new IpfsRestClient[IO](localBindAddress, restPort)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    logger.debug("Waiting for ipfs")
    Tcp.waitForPort(localBindAddress, restPort)
    Thread.sleep(10000)
  }

}
