package org.scalatra
package servlet

import javax.servlet.http._

trait ScalatraServletKernel extends Handler with ScalatraKernel {
  type Request = ssgi.servlet.ServletRequest

  abstract override def handle(request: Request, response: HttpServletResponse) {
    // As default, the servlet tries to decode params with ISO_8859-1.
    // It causes an EOFException if params are actually encoded with the other code (such as UTF-8)
    if (request.getCharacterEncoding == null)
      request.setCharacterEncoding(defaultCharacterEncoding)
    super.handle(request, response)
  }

  protected implicit def requestWrapper(r: HttpServletRequest) = RichRequest(r)

  protected def session = request.getSession
  protected def sessionOption = request.getSession(false) match {
    case s: HttpSession => Some(s)
    case null => None
  }
}