package org.scalatra
package netty

import util.RicherString._

import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.CookieDecoder
import org.jboss.netty.handler.codec.http.QueryStringDecoder
import java.net.URI
import util.MultiMap
import java.io.InputStream

class NettyHttpRequest(
  val requestMethod: HttpMethod,
  val uri: URI, 
  val headers: Map[String, String],
  val queryString: String,
  postParameters: MultiMap,
//        val files: GenSeq[HttpFile],
  val serverProtocol: HttpVersion,
  val inputStream: InputStream)
/*    (implicit appContext: ApplicationContext) */
extends HashMap[String, AnyRef] with Request {

  override lazy val pathInfo = uri.getPath.replaceFirst("^" + scriptName, "")

  override lazy val scriptName = "" /*PathManipulationOps.ensureSlash(appContext.server.base)*/

  override lazy val urlScheme = uri.getScheme match {
    case "http" => Http
    case "https" => Https
  }

  override lazy val cookies: scala.collection.Map[String, String] = {
    val nettyCookies = new CookieDecoder(true).decode(headers.getOrElse(Names.COOKIE, ""))
    Map((nettyCookies map { nc => nc.getName -> nc.getValue }).toSeq: _*)
/*
    val requestCookies =
      Map((nettyCookies map { nc =>
        val reqCookie: RequestCookie = nc
        reqCookie.name -> reqCookie
      }).toList:_*)
    new CookieJar(requestCookies)
*/
  }

  val contentType = headers.get(Names.CONTENT_TYPE).flatMap(_.blankOption)

  private def isWsHandshake =
    requestMethod == Get && headers.contains(Names.SEC_WEBSOCKET_KEY1) && headers.contains(Names.SEC_WEBSOCKET_KEY2)

  private def wsZero = Some(if (isWsHandshake) 8L else 0L)
  val contentLength =
    headers get Names.CONTENT_LENGTH flatMap (_.blankOption map { cl => Some(cl.toLong) } getOrElse wsZero)

  // TODO handle missing Host
  lazy val serverName = 
    headers.get(Names.HOST).get.split(":").head

  // TODO handle missing Host
  // TODO handle missing Port
  lazy val serverPort = 
    headers.get(Names.HOST).get.split(":").drop(1).head.toInt

  override val parameters: MultiParams = {
    val queryDecoder = new QueryStringDecoder("?"+uri.getQuery)
    Map() ++ queryDecoder.getParameters.mapValues { _.toSeq } ++ postParameters
  }

  def characterEncoding: Option[String] =
    headers.get(Names.CONTENT_TYPE) flatMap { ct =>
      ct.split(";").drop(1).headOption
    }

  protected[netty] def newResponse(ctx: ChannelHandlerContext) = new NettyHttpResponse(this, ctx)

}
