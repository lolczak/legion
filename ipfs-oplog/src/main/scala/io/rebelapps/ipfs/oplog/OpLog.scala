package io.rebelapps.ipfs.oplog

import scala.language.higherKinds

trait OpLog[F[_]] {

  def length(): F[Long]

  def lastSeqNumber(): F[Long]

  def head(): F[Entry]

  def headHash(): F[Hash]

  def entries(): F[List[Entry]]

  def append(entry: Entry): F[Hash]

  def updateHead(hash: Hash): F[List[Entry]]

}
