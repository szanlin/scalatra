package org.scalatra

import javax.servlet.http.{HttpServletRequestWrapper, HttpServletRequest, HttpServletResponse}
import ssgi.Post

trait MethodOverride extends Handler {
  override type Request = ssgi.servlet.ServletRequest

  abstract override def handle(req: Request) = {
    val req2 = req.requestMethod match {
      case Post =>
        req.getParameter(paramName) match {
          case null => req
          case method => new HttpServletRequestWrapper(req) { override def getMethod = method.toUpperCase }
        }
      case _ =>
        req
    }
    super.handle(new ssgi.servlet.ServletRequest(req2))
  }

  private val paramName = "_method"
}