package com.course.exercise

import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest}
import com.course.exercise.Util.requestFib

import scala.concurrent.Future
import scala.util.Random

object Exercise extends App {

  implicit val system: ActorSystem = ActorSystem()
  import system.dispatcher

  val futures = Util.bigFile.map(requestFib)
  Future
    .sequence(futures)
    .onComplete(
      result => {
        println(s"Finished with $result")
        system.terminate()
      }
    )

}

object Util {

  val Url      = "http://localhost:9000/fib"
  val started  = new AtomicLong(0)
  val finished = new AtomicLong(0)

  def requestFib(n: Int)(implicit as: ActorSystem): Future[Unit] = {
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
      .map(resp => {
        val currFinished = finished.incrementAndGet()
        logFinished(currFinished)

        if (resp.status.intValue() != 200) {
          println(resp.status)
          System.exit(1)
        }
      })
  }

  private def logFinished(currentStart: Long): Unit =
    log("Started", currentStart)

  private def logStarted(currentStart: Long): Unit =
    log("Started", currentStart)

  private def log(label: String, currentStart: Long): Unit =
    if (currentStart % 10 == 0)
      println(s"$label $currentStart requests")

  def bigFile: Iterator[Int] =
    Iterator.fill(1000)(Random.nextInt(20))

}
