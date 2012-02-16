package org.scalatra

import javax.servlet.http.HttpServletRequest

trait Service extends (HttpServletRequest => Result) {
  def orElse(service: Service) = Service { request =>
    this(request) match {
      case NoResult => service(request)
      case result => result
    }
  }
}

object Service {
  implicit def apply(f: HttpServletRequest => Result) = new Service {
    def apply(req: HttpServletRequest): Result = f(req)
  }
}
