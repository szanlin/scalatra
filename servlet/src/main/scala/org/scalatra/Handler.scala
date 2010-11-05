package org.scalatra

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

trait Handler {
  type Request <: ssgi.Request

  def handle(req: Request, res: HttpServletResponse): Unit
}