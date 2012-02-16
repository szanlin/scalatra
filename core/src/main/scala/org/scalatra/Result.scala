package org.scalatra

sealed trait Result

case class SyncResult(
  status: Int = 200, 
  headers: Map[String, String] = Map.empty, 
  body: Seq[Byte] = Array[Byte]()
) extends Result

// TODO AsyncResult

case object NoResult extends Result
