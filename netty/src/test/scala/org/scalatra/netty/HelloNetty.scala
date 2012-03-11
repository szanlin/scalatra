package org.scalatra
package netty

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.group.DefaultChannelGroup
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory


object HelloNetty extends ScalatraHandler {
  get("/hello/:netty") {
    params("netty")
  }

  override def handle(req: RequestT, res: ResponseT) {
    super.handle(req, res)
  }
}

class NettyServer(port: Int = 8080 /*info: ServerInfo*/) /* extends WebServer */ {

  private val bossThreadPool = Executors.newCachedThreadPool()
  private val workerThreadPool = Executors.newCachedThreadPool()
  private val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool))

  val channelFactory = new ScalatraPipelineFactory(HelloNetty)
  
  def start() {
//    DiskFileUpload.baseDirectory = info.tempDirectory.path.toAbsolute.path
//    DiskAttribute.baseDirectory = DiskFileUpload.baseDirectory
//    logger info ("Starting Netty HTTP server on %d" format port)
    bootstrap setPipelineFactory channelFactory
    bootstrap.bind(new InetSocketAddress(port))
  }

  def stop() {
//    appContext.applications.valuesIterator foreach (_.mounted.destroy())
    new DefaultChannelGroup().close().awaitUninterruptibly()
    bootstrap.releaseExternalResources()
    workerThreadPool.shutdown()
    bossThreadPool.shutdown()
//    logger info ("Netty HTTP server on %d stopped." format port)
  }
}

object Test extends Application {
  val server = new NettyServer
  server.start()
  new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine()
  server.stop()
}
