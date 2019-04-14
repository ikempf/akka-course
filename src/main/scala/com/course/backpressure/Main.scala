package com.course.backpressure

import akka.actor.ActorSystem
import akka.util.Timeout
import com.course.backpressure.Consumer.Take
import com.course.backpressure.Producer.{Offer, Pull}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration._
import scala.concurrent.Await

object Main extends App {

  implicit val system: ActorSystem        = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()(system)
  implicit val timeout: Timeout           = Timeout(5.seconds)
  import system.dispatcher

  val scheduler = system.scheduler
  val producer  = system.actorOf(Producer.props(50.milliseconds))
  val consumer  = system.actorOf(Consumer.props(200.milliseconds))

//  while (true) {
//    val Offer(i) = Await.result(producer ? Pull, 5.second)
//    consumer ! Take(i)
//  }

  Source
    .fromIterator(() => Iterator.continually(producer ? Pull))
    .mapAsync(1)(identity)
    .mapAsync(1) {
      case Offer(i) => consumer ? Take(i)
    }
    .runWith(Sink.ignore)

}
