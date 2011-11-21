package org.scalatra

import javax.servlet.{ServletContext, ServletContextEvent, ServletContextListener}

trait LifeCycleListener extends ServletContextListener {
  private var lifeCycle: Option[LifeCycle[ServletContext]] = None

  def contextInitialized(sce: ServletContextEvent) {
    initLifeCycle()
    lifeCycle foreach { _.start(sce.getServletContext) }
  }

  private def initLifeCycle() {
    try {
      lifeCycle = Some(Class.forName("ScalatraLifeCycle").newInstance.asInstanceOf[LifeCycle[ServletContext]])
    }
    catch {
      case e: ClassNotFoundException =>
    }
  }

  def contextDestroyed(sce: ServletContextEvent) {
    lifeCycle foreach { _.stop(sce.getServletContext) }
  }
}
