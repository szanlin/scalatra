package org.scalatra

import javax.servlet.http.HttpServletRequest

trait Service extends (HttpServletRequest => Result)
