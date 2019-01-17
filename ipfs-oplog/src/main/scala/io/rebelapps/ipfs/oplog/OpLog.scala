package io.rebelapps.ipfs.oplog

import scala.language.higherKinds

trait OpLog[F[_]] {

  def length(): F[Long]

  def lastSeqNumber(): F[Long]

  def head(): F[Payload]

  def headHash(): F[Hash]

  def entries(): F[List[Payload]]

  def append(entry: Payload): F[Hash]

  def updateHead(hash: Hash): F[List[Payload]]

}
