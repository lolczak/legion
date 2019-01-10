package io.rebelapps.ipfs.failure

case class InvalidResponse(code: Int, body: String, errorMsg: String)
