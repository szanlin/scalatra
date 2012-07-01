package org.scalatra

import scala.collection.mutable

trait HttpSession extends mutable.Map[String, AnyRef] with mutable.MapLike[String, AnyRef, HttpSession] {

  def applicationContext: ApplicationContext

  def id: String

  def invalidate(): Unit
}
