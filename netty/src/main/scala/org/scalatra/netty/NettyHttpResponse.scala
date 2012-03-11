package org.scalatra
package netty

import util.RicherString._

import scala.io.Codec
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import org.jboss.netty.handler.codec.http2.HttpHeaders.Names
import org.jboss.netty.channel.{ChannelFutureListener, ChannelHandlerContext}
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBufferOutputStream}
import org.jboss.netty.handler.codec.http2.{HttpHeaders, DefaultHttpResponse, HttpResponseStatus, HttpVersion => JHttpVersion}
import java.io.{PrintWriter, OutputStreamWriter}
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean

class NettyHttpResponse(request: NettyHttpRequest, connection: ChannelHandlerContext) extends Response {
  private val _ended = new AtomicBoolean(false)
  private val underlying = new DefaultHttpResponse(nettyProtocol, HttpResponseStatus.OK)
  private def nettyProtocol = request.serverProtocol match {
    case Http10 => JHttpVersion.HTTP_1_0
    case Http11 => JHttpVersion.HTTP_1_1
  }

  def status = ResponseStatus(
    underlying.getStatus.getCode, underlying.getStatus.getReasonPhrase)
  def status_=(status: ResponseStatus) = underlying.setStatus(
    new HttpResponseStatus(status.code, status.message))

  def contentType = {
    headers.get(Names.CONTENT_TYPE).flatMap(_.blankOption)
  }
  def contentType_=(ct: Option[String]) {
    headers(Names.CONTENT_TYPE) = ct getOrElse null
  }
  var charset = Codec.UTF8

  def characterEncoding = Some(charset.name)

  def characterEncoding_=(encoding: Option[String]) =
    charset = (encoding map Charset.forName) getOrElse Codec.UTF8

  lazy val outputStream = 
    new ChannelBufferOutputStream(ChannelBuffers.dynamicBuffer())

  lazy val writer =
    new PrintWriter(new OutputStreamWriter(outputStream, characterEncoding getOrElse "UTF-8"))

  def end() = {
    writer.flush()
    if (_ended.compareAndSet(false, true)) {
      headers foreach {
        case (k, v) if k == Names.CONTENT_TYPE => {
          val Array(mediaType, hdrCharset) = {
            val parts = v.split(';').map(_.trim)
            if (parts.size > 1) parts else Array(parts(0), "")
          }
          underlying.setHeader(k, mediaType + ";" + (hdrCharset.blankOption getOrElse "charset=%s".format(charset.name)))
        }
        case (k, v) => {
          underlying.setHeader(k, v)
        }
      }
//      request.cookies.responseCookies foreach { cookie => underlying.addHeader(Names.SET_COOKIE, cookie.toCookieString) }
      val content = outputStream.buffer()
      if (content.readableBytes() < 1) content.writeByte(0x1A)
      underlying.setContent(content)
      val fut = connection.getChannel.write(underlying)
      if(!HttpHeaders.isKeepAlive(underlying) || !chunked) fut.addListener(ChannelFutureListener.CLOSE)

    }
  }

  def chunked = underlying.isChunked

  def chunked_=(chunked: Boolean) = underlying setChunked chunked

  def redirect(uri: String) = {
    underlying.setStatus(HttpResponseStatus.FOUND)
    underlying.setHeader(Names.LOCATION, uri)
    end()
  }

  override def addCookie(cookie: Cookie) {
    underlying.addHeader(Names.COOKIE, cookie.toCookieString)
  }

  object headers extends Map[String, String] {
    def get(key: String): Option[String] = 
      underlying.getHeaders(key) match {
	case xs if xs.isEmpty => None
	case xs => Some(xs mkString ",")
      }

    def iterator: Iterator[(String, String)] = 
      for (name <- underlying.getHeaderNames.iterator) 
      yield (name, underlying.getHeaders(name) mkString ",_")

    def +=(kv: (String, String)): this.type = {
      underlying.setHeader(kv._1, kv._2)
      this
    }

    def -=(key: String): this.type = {
      underlying.removeHeader(key)
      this
    }
  }
}
