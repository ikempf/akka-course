package com.course.exercise

import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Sink, Source}
import com.course.exercise.Util.requestFib
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

object Exercise extends App {

  implicit val system: ActorSystem        = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  import system.dispatcher

  val futures = Util.bigFile.map(requestFib)

  val run = Future.sequence(futures)

  run
    .onComplete(
      result => {
        println(s"Finished with $result")
        system.terminate()
      }
    )

  Await.result(run, 1.minute)

}

object Util {

  val Url      = "http://localhost:9000/fib"
  val started  = new AtomicLong(0)
  val finished = new AtomicLong(0)

  def requestFib(n: Int)(implicit as: ActorSystem, map: Materializer): Future[Int] = {
    import as.dispatcher

    val currStarted = started.incrementAndGet()
    logStarted(currStarted)

    Http()
      .singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri = s"$Url",
          entity = HttpEntity(Integer.toString(n))
        )
      )
      .flatMap(resp => {
        val currFinished = finished.incrementAndGet()
        logFinished(currFinished)
        assertOk(resp)
        Unmarshal(resp.entity).to[String].map(_.toInt)
      })
  }

  private def logFinished(currentStart: Long): Unit =
    log("Finished", currentStart)

  private def logStarted(currentStart: Long): Unit =
    log("Started", currentStart)

  private def log(label: String, currentStart: Long): Unit =
    if (true)
      println(s"$label $currentStart requests")

  private def assertOk(resp: HttpResponse): Unit =
    if (resp.status.intValue() != 200) {
      println(resp.status)
      System.exit(1)
    }

  def bigFile: Iterator[Int] =
    Iterator.fill(1000)(Random.nextInt(20))

}
