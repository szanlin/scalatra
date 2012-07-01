package org.scalatra
package netty

import collection.{GenSeq, Map}
import java.net.URI
import java.io.InputStream
import collection.JavaConverters._
import org.jboss.netty.handler.codec.http.CookieDecoder
import org.jboss.netty.handler.codec.http2.HttpHeaders.Names
import org.jboss.netty.channel.ChannelHandlerContext
import util.MultiMap
import util.RicherString._

class NettyHttpRequest(
        val method: HttpMethod,
        val uri: URI,
        val headers: Map[String, String],
        val queryString: MultiMap,
        postParameters: MultiMap,
        val files: GenSeq[HttpFile],
        val serverProtocol: HttpVersion,
        val inputStream: InputStream)(implicit appContext: ApplicationContext) extends HttpRequest {

  val path = uri.getPath.replaceFirst("^" + appPath, "")

  val appPath = PathManipulationOps.ensureSlash(appContext.server.base)

  val scheme = uri.getScheme

  val cookies = {
    val nettyCookies = new CookieDecoder(true).decode(headers.getOrElse(Names.COOKIE, "")).asScala
    val requestCookies =
      Map((nettyCookies map { nc =>
        val reqCookie: RequestCookie = nc
        reqCookie.name -> reqCookie
      }).toList:_*)
    new CookieJar(requestCookies)
  }

  val contentType = headers.get(Names.CONTENT_TYPE).flatMap(_.blankOption)

  private def isWsHandshake =
    method == Get && headers.contains(Names.SEC_WEBSOCKET_KEY1) && headers.contains(Names.SEC_WEBSOCKET_KEY2)

  private def wsZero = if (isWsHandshake) 8L.some else 0L.some
  val contentLength =
    headers get Names.CONTENT_LENGTH flatMap (_.blankOption some (_.toLong.some) none wsZero)

  val serverName = appContext.server.name

  val serverPort = appContext.server.port

  val parameters = MultiMap(queryString ++ postParameters)

  protected[scalatra] def newResponse(ctx: ChannelHandlerContext) = new NettyHttpResponse(this, ctx)

}