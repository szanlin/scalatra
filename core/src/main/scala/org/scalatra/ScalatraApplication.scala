package org.scalatra

import javax.servlet.http.HttpServletRequest

trait ScalatraApplication extends (HttpServletRequest => Result)
