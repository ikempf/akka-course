package com.course.streamapi

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration._

object StreamApi extends App {

  implicit val as: ActorSystem       = ActorSystem()
  implicit val am: ActorMaterializer = ActorMaterializer()

  val single = Source.single(0)
  val list   = Source.apply(List(1, 2, 3))
  val const  = Source.repeat(0)
  val tick   = Source.tick(500.milliseconds, 1.second, -1)
  val fib    = Source.unfold((0, 1)) { case (a, b) => Some(((b, a + b), a)) }

  fib.take(10).runWith(Sink.foreach(println))

}
