package org.scalatra
package servlet

import java.nio.charset.Charset
import javax.servlet.http._
import ssgi.{CharRenderable, Renderable}
import ssgi.servlet.SsgiServletResponse
import scala.util.DynamicVariable

trait ScalatraServletKernel extends Handler with ScalatraKernel {
  type Request = ssgi.servlet.ServletRequest
  type ResponseBuilder = ssgi.servlet.SsgiServletResponse

  private val _response = new DynamicVariable[ResponseBuilder](null.asInstanceOf[ResponseBuilder])
  protected def response = _response.value

  abstract override def handle(request: Request) = {
    // As default, the servlet tries to decode params with ISO_8859-1.
    // It causes an EOFException if params are actually encoded with the other code (such as UTF-8)
    if (request.getCharacterEncoding == null)
      request.setCharacterEncoding(defaultCharacterEncoding)
    super.handle(request)
  }

  protected def handle(request: Request, servletResponse: HttpServletResponse) {
    _response.withValue(new SsgiServletResponse(servletResponse)) {
      val response = handle(request)
      render(response, servletResponse)
    }
  }

  protected def render(ssgiResponse: ssgi.Response[_], resp: HttpServletResponse): Unit = {
    resp.setStatus(response.status)
    ssgiResponse.headers foreach { case (key, value) => resp.addHeader(key, value) }
    ssgiResponse.renderableBody match {
      case cr: CharRenderable => cr.writeTo(resp.getWriter)
      case r: Renderable => r.writeTo(resp.getOutputStream, Charset.forName("utf-8"))
    }
  }

  protected implicit def requestWrapper(r: HttpServletRequest) = RichRequest(r)

  protected def session = request.getSession
  protected def sessionOption = request.getSession(false) match {
    case s: HttpSession => Some(s)
    case null => None
  }
}