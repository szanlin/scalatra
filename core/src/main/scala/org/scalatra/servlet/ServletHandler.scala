package org.scalatra
package servlet

trait ServletHandler extends Handler {
  type RequestT = ServletHttpRequest
  type ResponseT = ServletHttpResponse
}
