package org.scalatra
package servlet

import javax.servlet.http.{ HttpSession => ServletSession }

object ServletHttpSession {
  def apply(session: ServletSession) = new ServletHttpSession(session)
}

/**
 * Extension methods to the standard HttpSession.
 */
class ServletHttpSession(session: ServletSession)
  extends SessionWrapper(session)
  with HttpSession
  with AttributesMap 
{


  val applicationContext: ApplicationContext = ServletApplicationContext(session.getServletContext)

  def id = session.getId

  protected def attributes = session
}
