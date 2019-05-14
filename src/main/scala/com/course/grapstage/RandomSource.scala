package com.course.grapstage

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}

import scala.util.Random

class RandomSource(seed: Long) extends GraphStage[SourceShape[Int]] {
  val out: Outlet[Int] = Outlet("RandomSource")
  override val shape: SourceShape[Int] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      private val Gen = new Random(seed)

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          push(out, Gen.nextInt())
        }
      })
    }
}


object RandomSource extends App {

  implicit val as: ActorSystem       = ActorSystem()
  implicit val am: ActorMaterializer = ActorMaterializer()

  Source
    .fromGraph(new RandomSource(1))
    .runWith(Sink.foreach(println))

}