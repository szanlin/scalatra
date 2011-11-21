package org.scalatra

import javax.servlet.{Filter, ServletContext}
import javax.servlet.http.HttpServlet

trait Context[+A] {
  def mount(kernel: ScalatraKernel, prefix: String = "/", name: Option[String] = None): Unit
  def delegate: A
}

object Context {
  implicit def servletContextContext(servletContext: ServletContext): Context[ServletContext] = new Context[ServletContext] {
    def delegate = servletContext

    def mount(kernel: ScalatraKernel, prefix: String, name: Option[String]) = {
      def mountName = name getOrElse kernel.getClass.getName
      def mapping(prefix: String) = prefix match {
        case "/" => "/*"
        case prefix if prefix startsWith "/" => prefix+"/*"
        case _ => "/"+prefix+"/*"
      }
      kernel match {
        case servlet: HttpServlet =>
          val reg = servletContext.addServlet(mountName, servlet)
          reg.addMapping(mapping(prefix))
        case filter: Filter =>
          val reg = servletContext.addFilter(mountName, filter)
          reg.addMappingForUrlPatterns(null, false, mapping(prefix))
        case _ =>
          sys.error("Kernel must be an HttpServlet or a Filter.")
      }
    }
  }
}
