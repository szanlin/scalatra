package org.scalatra

import collection.mutable
import java.io.{FileInputStream, File}
import util.using
import util.io.zeroCopy

// Perhaps making renderResponseBody a stackable method this would also give a render pipeline maybe even a better one at that
//trait RenderResponseBody {
//  def renderResponseBody(actionResult: Any)
//}

object RenderPipeline {
  object Variance extends Enumeration {
    val Co, Contra, No = Value
  }

  class Def[ToMatch : Manifest](variance: Variance.Value*) {
    import Variance._

    def unapply[Candidate](candidate: Candidate)(implicit mf: Manifest[Candidate]): Option[Candidate] = {
      val toMatch = manifest[ToMatch]
      val typeArgsTriplet = toMatch.typeArguments.zip(mf.typeArguments).zipWithIndex

      def sameArgs = typeArgsTriplet forall {
        case ((desired,actual),index) if(getVariance(index) == Contra) => desired <:< actual
        case ((desired,actual),index) if(getVariance(index) == No) => desired == actual
        case ((desired,actual),index)  => desired >:> actual
      }

      val isAssignable = toMatch.erasure.isAssignableFrom(mf.erasure) || (toMatch >:> mf)
      if (isAssignable && sameArgs) Some(candidate.asInstanceOf[Candidate]) else None
    }

    def getVariance(index: Int) = {
      if(variance.length > index) variance(index) else No
    }
  }
}

/**
 * Allows overriding and chaining of response body rendering. Overrides [[ScalatraKernel#renderResponseBody]].
 */
trait RenderPipeline {this: ScalatraKernel =>

  import RenderPipeline._
  object ActionRenderer{
    def apply[A: Manifest, B: Manifest](fun: A => B) = new ActionRenderer(fun)
  }
  sealed trait RenderAction {
    type ResultType <: Any
    def apply[C : Manifest](v1: C): C
    def isDefinedAt[C : Manifest](x: C): Boolean
  }
  private[scalatra] class ActionRenderer[A: Manifest, B: Manifest](fun: A => B) extends PartialFunction[Any, Any] with RenderAction {
    type ResultType = B
    private val extractor = new Def[A](Variance.No)
    def apply[C](c: C)(implicit mf: Manifest[C]): C = extractor.unapply(c).get
    def isDefinedAt[C](c: C)(implicit mf: Manifest[C]) = extractor.unapply(c).isDefined
  }

//  private type RenderAction = PartialFunction[Any, Any]
  protected val renderPipeline = new mutable.ArrayBuffer[RenderAction] with mutable.SynchronizedBuffer[RenderAction]

  override def renderResponseBody[T: Manifest](actionResult: T) {
    useRenderPipeline apply actionResult
  }

  protected def useRenderPipeline: RenderAction = {
    case pipelined if renderPipeline.exists(_.isDefinedAt(pipelined)) => {
      (pipelined /: renderPipeline) {
        case (body, renderer) if (renderer.isDefinedAt(body)) => renderer(body)
        case (body, _) => body
      }
    }
  }
//
//  private def defaultRenderResponse: PartialFunction[Any, Unit] = {
//    case bytes: Array[Byte] =>
//      response.getOutputStream.write(bytes)
//    case file: File =>
//      using(new FileInputStream(file)) { in => zeroCopy(in, response.getOutputStream) }
//    case _: Unit =>
//    // If an action returns Unit, it assumes responsibility for the response
//    case x: Any  =>
//      response.getWriter.print(x.toString)
//  }

  /**
   * Prepend a new renderer to the front of the render pipeline.
   */
  def render[A: Manifest, B: Manifest](fun: A => B) {
    ActionRenderer(fun) +=: renderPipeline
  }

  render[Any, Unit] {
    case _: Unit => // If an action or renderer returns Unit, it assumes responsibility for the response
    case x => response.getWriter.print(x.toString)
  }

  render[File, Unit] { f => using(new FileInputStream(f)) {in => zeroCopy(in, response.getOutputStream)} }

  render[Array[Byte], Unit] {bytes =>
    response.getOutputStream.write(bytes)
  }

}
