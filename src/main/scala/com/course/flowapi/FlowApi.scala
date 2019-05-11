package com.course.flowapi

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.course.flowapi.Stub.saveToDb

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object StreamApi extends App {

  implicit val as: ActorSystem       = ActorSystem()
  implicit val am: ActorMaterializer = ActorMaterializer()

  // Construct sources
  Source.single(0)
  Source.apply(List(1, 2, 3))
  Source.repeat(0)
  Source.tick(500.milliseconds, 1.second, -1)
  Source.unfold((0, 1)) { case (a, b) => Some(((b, a + b), a)) }

  // Intermediate manipulation
  val source = Source.empty[Int]
  source.map(_ + 1)
  source.mapAsync(parallelism = 50)(saveToDb)
  source.flatMapConcat(i => Source.apply(List(i, i, i)))
  source.take(15)
  source.filter(_ % 2 == 0)
  source.grouped(5).map(group => group.sum)

  // Final operations
  source.runFold(0)(_ + _)
  source.runForeach(println)
  source.runWith(Sink.ignore)

}

object Stub {
  def saveToDb(i: Int): Future[Int] = Future(i)
}
