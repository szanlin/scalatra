package org.scalatra
package servlet

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

class ApplicationServlet(private var application: ScalatraApplication) 
  extends HttpServlet
{
  override def service(request: HttpServletRequest, 
		       response: HttpServletResponse) {
    application(request) match {
      case SyncResult(status, headers, body) =>
	response.setStatus(status)
        headers foreach { case (name, value) =>
	  response.addHeader(name, value)
        }
        // TODO This is awful.  It's just a POC.
        body foreach { b => response.getOutputStream.write(b) }
      
      case NoResult =>
	response.sendError(HttpServletResponse.SC_NOT_FOUND)
    }
  }
}
