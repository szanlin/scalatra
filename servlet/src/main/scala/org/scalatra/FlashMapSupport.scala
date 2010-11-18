package org.scalatra

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.util.DynamicVariable

object FlashMapSupport {
  val sessionKey = FlashMapSupport.getClass.getName+".key"
}

trait FlashMapSupport extends Handler {
  import FlashMapSupport.sessionKey

  type Request <: ssgi.servlet.ServletRequest

  abstract override def handle(req: Request) = {
    _flash.withValue(getFlash(req)) {
      val response = super.handle(req)
      flash.sweep()
      req.getSession.setAttribute(sessionKey, flash)
      response
    }
  }

  private def getFlash(req: Request) =
    req.getSession.getAttribute(sessionKey) match {
      case flashMap: FlashMap => flashMap
      case _ => FlashMap()
    }


  private val _flash = new DynamicVariable[FlashMap](null)
  protected def flash = _flash.value
}