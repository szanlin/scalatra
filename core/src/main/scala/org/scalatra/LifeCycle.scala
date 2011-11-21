package org.scalatra

trait LifeCycle[-A] {
  def start(ctx: Context[A]): Unit
  def stop(ctx: Context[A]): Unit
}
