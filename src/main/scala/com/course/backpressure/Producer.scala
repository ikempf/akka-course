package com.course.backpressure

import akka.actor.{Actor, Props, Stash}
import com.course.backpressure.Producer.{Offer, Pull, Ready}

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class Producer(duration: FiniteDuration) extends Actor with Stash {

  import context.dispatcher

  override def receive: Receive = producing(0)

  def producing(count: Int): Receive = {
    case Pull =>
      if (count % 10 == 0)
        println(s"Produced $count")
      context.become(idle(count + 1))
      context.system.scheduler.scheduleOnce(duration, self, Ready)
      sender() ! Offer(Random.nextInt())
  }

  def idle(count: Int): Receive = {
    case Pull => stash()
    case Ready =>
      context.become(producing(count))
      unstashAll()
  }

}

object Producer {

  def props(produceDuration: FiniteDuration) =
    Props(classOf[Producer], produceDuration)

  case object Pull
  case class Offer(i: Int)
  private case object Ready

}
