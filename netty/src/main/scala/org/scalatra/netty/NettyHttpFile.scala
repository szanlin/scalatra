package org.scalatra
package netty

import org.jboss.netty.handler.codec.http2.FileUpload
import java.io.{InputStream, FileInputStream, File}
import org.jboss.netty.buffer.ChannelBufferInputStream
import org.scalatra.HttpFile

class NettyHttpFile(underlying: FileUpload) extends HttpFile {
  val name: String = underlying.getFilename

  val contentType: String = underlying.getContentType

  lazy val size: Long = underlying.length()

  lazy val inputStream: InputStream = if (underlying.isInMemory) {
    new  ChannelBufferInputStream(underlying.getChannelBuffer)
  } else {
    new FileInputStream(underlying.getFile)
  }

  lazy val bytes: Array[Byte] = underlying.get()

  lazy val string: String = underlying.getString

  def saveTo(file: File): Unit = underlying.renameTo(file)

  def delete(): Unit = underlying.delete()
}