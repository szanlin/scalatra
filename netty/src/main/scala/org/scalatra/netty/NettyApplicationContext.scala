package org.scalatra
package netty

import java.net.URL
import collection.mutable
import collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap

class NettyApplicationContext(val contextPath: String) extends ApplicationContext  {

  private val attributes = new ConcurrentHashMap[String, AnyRef]().asScala


  def mount(handler: Handler, urlPattern: String, name: String) {

  }

  def mount[T <: Handler](handlerClass: Class[T], urlPattern: String, name: String) {

  }

  val initParameters: mutable.Map[String, String] = new ConcurrentHashMap[String, String]().asScala

  def resource(path: String): Option[URL] = Option(getClass.getResource(path))

  def get(key: String): Option[AnyRef] = attributes.get(key)

  def iterator: Iterator[(String, AnyRef)] = attributes.iterator

  def +=(kv: (String, AnyRef)): NettyApplicationContext = { attributes += kv; this }

  def -=(key: String): NettyApplicationContext = { attributes -= key; this }
}