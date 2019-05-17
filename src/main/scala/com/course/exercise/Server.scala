package com.course.exercise

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.course.exercise.Domain.fib

import scala.concurrent.ExecutionContext

object Server extends App {

  implicit val system: ActorSystem             = ActorSystem()
  implicit val executor: ExecutionContext      = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def route = path("fib") {
    post {
      entity(as[String]) { n =>
        println(s"Received fib($n)")
        complete(Integer.toString(fib(n.toInt)))
      }
    }
  }

  Http().bindAndHandle(route, "localhost", 9000)

}

object Domain {

  def fib(n: Int): Int =
    if (n <= 0 || n == 1)
      1
    else
      fib(n - 1) + fib(n - 2)

}
