package com.course.backpressure

import akka.actor.{Actor, ActorRef, Props, Stash}
import com.course.backpressure.Consumer.{Complete, Consumed, Take}

import scala.concurrent.duration.FiniteDuration

class Consumer(duration: FiniteDuration) extends Actor with Stash {

  import context.dispatcher

  override def receive: Receive = consuming(0)

  def consuming(count: Int): Receive = {
    case Take(_) =>
      if (count % 10 == 0)
        println(s"Consumed $count")
      context.become(idle(count, sender()))
      context.system.scheduler.scheduleOnce(duration, self, Consumed)
  }

  def idle(count: Int, ref: ActorRef): Receive = {
    case _: Take => stash()
    case Consumed =>
      ref ! Complete
      context.become(consuming(count + 1))
      unstashAll()
  }

}

object Consumer {

  def props(consumeDuration: FiniteDuration): Props =
    Props(classOf[Consumer], consumeDuration)

  case class Take(i: Int)
  case object Complete
  private object Consumed

}
