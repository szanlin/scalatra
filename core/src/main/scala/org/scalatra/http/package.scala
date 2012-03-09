package org.scalatra

import grizzled.slf4j.Logger
import io.backchat.http._
import io.backchat.http.parser.HttpParser

package object http {
  private val logger = Logger[this.type]

  def toContentType(s: String): ContentType =
    HttpParser.parse(HttpParser.CONTENT_TYPE, s).fold(
      { e => logger.warn("Invalid Content-Type: %s".format(e)); ContentType(MediaTypes.`text/plain`) },
      { h => h.contentType })  
}
