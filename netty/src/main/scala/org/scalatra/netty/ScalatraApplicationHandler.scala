//package org.scalatra
//package netty
//
//import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext}
//import org.jboss.netty.channel.ChannelHandler.Sharable
//
///**
// * This handler is shared across the entire server, providing application level settings
// */
//@Sharable
//class ScalatraApplicationHandler(implicit val appContext: NettyApplicationContext) extends ScalatraUpstreamHandler {
//
//  protected val sessions = new InMemorySessionStore()
//
//  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
//    e.getMessage match {
//      case req: NettyHttpRequest => {
//        appContext.application(req) match {
//          case Some(app: ScalatraApp with SessionSupport) => {
//            val current = req.cookies get appContext.sessionIdKey flatMap sessions.get
//            app.session = current | sessions.newSession
//            if (current.isEmpty) req.cookies += appContext.sessionIdKey -> app.session.id
//          }
//          case _ =>
//        }
//        ctx.sendUpstream(e)
//      }
//      case _ => {
//        super.messageReceived(ctx, e)
//      }
//    }
//  }
//
//  def stop() = sessions.stop()
//
//}
