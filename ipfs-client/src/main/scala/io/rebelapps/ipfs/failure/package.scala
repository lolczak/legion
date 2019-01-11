package io.rebelapps.ipfs

import shapeless.{:+:, CNil}

package object failure {

  type ObjectPutFailure = NetworkFailure :+: UnrecognizedFailure :+: InvalidRequest :+: InvalidResponse :+: CNil

  type ObjectGetFailure = NetworkFailure :+: NotFound.type :+: UnrecognizedFailure :+: InvalidRequest :+: InvalidResponse :+: CNil

}
