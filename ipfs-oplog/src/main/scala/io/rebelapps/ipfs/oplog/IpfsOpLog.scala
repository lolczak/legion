package io.rebelapps.ipfs.oplog

import cats.effect.Effect
import io.rebelapps.ipfs.api.IpfsApi

import scala.language.higherKinds
import cats.implicits._
import io.rebelapps.ipfs.model.ObjectPutResponse

class IpfsOpLog[F[_]] private(ipfs: IpfsApi[F], @volatile var memento: LogMemento)
                             (implicit E: Effect[F])
  extends OpLog[F] {

  override def length(): F[Long] = E.point(memento.seqNumber)

  override def lastSeqNumber(): F[Long] = E.point(memento.seqNumber)

  override def head(): F[Entry] = E.point(memento.entries.head.entry)

  override def headHash(): F[Hash] = E.point(memento.headHash)

  override def entries(): F[List[Entry]] = E.point(memento.entries.reverse.tail.map(_.entry))

  override def append(entry: Entry): F[Hash] = {
    for {
      newHead <- E.delay(EntryEnvelope(memento.seqNumber + 1, Some(memento.headHash), entry))
      hash    <- ipfs.objectOps.putJson(newHead) flatMap {
        case Left(err) =>
          E.raiseError[Hash](new RuntimeException(err.toString)) //todo error handling

        case Right(ObjectPutResponse(hash)) =>
          E.point(hash)
      }
      _       <- E.delay { memento = memento.copy(seqNumber = memento.seqNumber+1, headHash = hash, entries = newHead :: memento.entries) }
    } yield hash
  }

  override def updateHead(hash: Hash): F[List[Entry]] = ???

}

object IpfsOpLog {

  def createNew[F[_] : Effect](ipfs: IpfsApi[F]): F[OpLog[F]] = {
    val E = implicitly[Effect[F]]
    ipfs.objectOps.putJson(EntryEnvelope.Root) flatMap {
      case Left(err) =>
        E.raiseError(new RuntimeException(err.toString)) //todo error handling

      case Right(ObjectPutResponse(hash)) =>
        val memento = LogMemento(0, hash, List(EntryEnvelope.Root))
        E.point(new IpfsOpLog[F](ipfs, memento))
    }
  }

  def fromExisting[F[_] : Effect](ipfs: IpfsApi[F], headHash: Hash): F[OpLog[F]] = ???

}

case class LogMemento(seqNumber: Long,
                      headHash: Hash,
                      entries: List[EntryEnvelope])
