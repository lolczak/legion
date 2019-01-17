package io.rebelapps.ipfs.oplog

import cats.effect.Effect
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.Stream.emit
import io.rebelapps.ipfs.api.IpfsApi
import io.rebelapps.ipfs.model.ObjectPutResponse
import fs2._

import scala.language.higherKinds

class IpfsOpLog[F[_]] private(ipfs: IpfsApi[F], mementoRef: Ref[F, LogMemento])
                             (implicit F: Effect[F])
  extends OpLog[F] {

  import F._

  override def length(): F[Long] = mementoRef.get.map(_.seqNumber)

  override def lastSeqNumber(): F[Long] = mementoRef.get.map(_.seqNumber)

  override def head(): F[Payload] = mementoRef.get.map(_.entries.head.entry)

  override def headHash(): F[Hash] = mementoRef.get.map(_.headHash)

  override def entries(): F[List[Payload]] = mementoRef.get.map(_.entries.reverse.tail.map(_.entry))

  override def append(entry: Payload): F[Hash] = {
    for {
      memento <- mementoRef.get
      newHead <- delay(EntryEnvelope(memento.seqNumber + 1, Some(memento.headHash), entry))
      hash    <- ipfs.objectOps.putJson(newHead) flatMap {
        case Left(err) =>
          raiseError[Hash](new RuntimeException(err.toString)) //todo error handling

        case Right(ObjectPutResponse(hash)) =>
          point(hash)
      }
      _       <- mementoRef.set(memento.copy(seqNumber = memento.seqNumber+1, headHash = hash, entries = newHead :: memento.entries))
    } yield hash
  }

  override def updateHead(hash: Hash): F[List[Payload]] = {
    def loop[G[_], A](start: A)(f: A => Stream[G, A]): Stream[G, A] =
      emit(start) ++ f(start).flatMap(loop(_)(f))

    def fetchTo(seqNumber: Long): F[List[EntryEnvelope]] =
      Stream
        .eval { loadEntry(hash) }
        .flatMap { head =>
          loop(head) {
            case entry if entry.isRoot || entry.sequenceNumber == seqNumber + 1 => //todo error handling
              Stream.empty

            case entry if entry.sequenceNumber > seqNumber =>
              Stream.eval(loadEntry(entry.maybeNext.get))
          }
        }
        .compile
        .toList

      for {
        memento <- mementoRef.get
        branch  <- fetchTo(memento.seqNumber)
        _       <- mementoRef.set(memento.copy(seqNumber = branch.head.sequenceNumber, headHash = hash, entries = branch ++ memento.entries))
      } yield branch.reverse.map(_.entry)

  }

  private def loadEntry(hash: Hash): F[EntryEnvelope] = {
    ipfs.objectOps.getJson[EntryEnvelope](hash) flatMap {
      case Left(err) =>
        raiseError[EntryEnvelope](new RuntimeException(err.toString)) //todo error handling

      case Right(entry) =>
        point(entry)
    }
  }

}

object IpfsOpLog {

  def createNew[F[_] : Effect](ipfs: IpfsApi[F]): F[OpLog[F]] = {
    val E = implicitly[Effect[F]]
    ipfs.objectOps.putJson(EntryEnvelope.Root) flatMap {
      case Left(err) =>
        E.raiseError(new RuntimeException(err.toString)) //todo error handling

      case Right(ObjectPutResponse(hash)) =>
        val memento = LogMemento(0, hash, List(EntryEnvelope.Root))
        Ref.of[F, LogMemento](memento) map {ref => new IpfsOpLog[F](ipfs, ref) }
    }
  }

  def fromExisting[F[_] : Effect](ipfs: IpfsApi[F], headHash: Hash): F[OpLog[F]] = ???

}

case class LogMemento(seqNumber: Long,
                      headHash: Hash,
                      entries: List[EntryEnvelope])
