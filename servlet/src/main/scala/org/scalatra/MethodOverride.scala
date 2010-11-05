package org.scalatra

import javax.servlet.http.{HttpServletRequestWrapper, HttpServletRequest, HttpServletResponse}

trait MethodOverride extends Handler {
  override type Request = ssgi.servlet.ServletRequest

  abstract override def handle(req: Request, res: HttpServletResponse) {
    val req2 = req.getMethod match {
      case "POST" =>
        req.getParameter(paramName) match {
          case null => req
          case method => new HttpServletRequestWrapper(req) { override def getMethod = method.toUpperCase }
        }
      case _ =>
        req
    }
    super.handle(new ssgi.servlet.ServletRequest(req2), res)
  }

  private val paramName = "_method"
}