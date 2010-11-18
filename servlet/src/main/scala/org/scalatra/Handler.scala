package org.scalatra

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import ssgi.{Request => SsgiRequest, Response => SsgiResponse}

trait Handler {
  type Request <: SsgiRequest

  def handle(req: Request): SsgiResponse[_]
}