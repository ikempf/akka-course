package com.course.graphapi

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition, Source}
import akka.stream.{ActorMaterializer, FlowShape, Graph}
import com.course.graphapi.Util._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
case class Mail(to: String, body: String)

object GraphApi extends App {

  implicit val as: ActorSystem       = ActorSystem()
  implicit val am: ActorMaterializer = ActorMaterializer()
  import as.dispatcher

  val source = Source.fromIterator(() => fileChars)

  val countSymbols: Graph[FlowShape[String, (Any, Int)], NotUsed] =
    GraphDSL.create()(implicit builder => {

      val broadcast     = builder.add(Broadcast[String](2))
      val punctuationF  = Flow[Char].filter(keepPonctuation)
      val wordF         = Flow.fromFunction(stripPonctuation).flatMapConcat(line => Source(splitLines(line)))
      val wordLengthF   = Flow.fromFunction(wordLength)
      val itoaF         = Flow.fromFunction(itoa)
      val toCharsF      = Flow.fromFunction(toChars).flatMapConcat(Source(_))
      def groupByLength = Flow[Int].groupBy(100, identity).mergeSubstreams
      def mapReduce[A]  = Flow[A].map(a => (a, 1)).groupBy(100, identity).reduce((t1, t2) => (t1._1, t1._2 + t2._2)).mergeSubstreams
      def printF[A]     = Flow[A].map(a => { println("--" + a); a })

      val ponctuations = builder.add(Partition[Char](Ponctuations.size, partitionPonctuation))
      val countMerge   = builder.add(Merge[(Any, Int)](Ponctuations.size + 1))

      broadcast.out(0) ~> wordF ~> wordLengthF ~> groupByLength ~> itoaF ~> mapReduce[String] ~> countMerge.in(Ponctuations.size)
      broadcast.out(1) ~> toCharsF ~> punctuationF ~> ponctuations.in
      ponctuations.outlets.zipWithIndex.foreach {
        case (out, i) => out ~> mapReduce[Char] ~> countMerge.in(i)
      }

      FlowShape(broadcast.in, countMerge.out)
    })

  println("Counting ...")
  val run = source.via(countSymbols).runForeach(prettyPrint)
  run.onComplete(println)
  Await.ready(
    run,
    5.seconds
  )
  println("Done ...")

  as.terminate()

}

object Util {

  val Ponctuations = List('.', ':', ',', ';')

  def keepPonctuation(char: Char): Boolean =
    Ponctuations.contains(char)

  def splitLines(str: String): List[String] =
    str.split(" ").toList

  def stripPonctuation(str: String): String =
    str.replaceAll(Ponctuations.mkString, " ")

  def toChars(str: String): List[Char] =
    str.toCharArray.toList

  def itoa(int: Int): String =
    Integer.toString(int)

  def wordLength(str: String): Int =
    str.length

  def partitionPonctuation(char: Char): Int =
    Ponctuations.indexOf(char)

  def prettyPrint[A, B](tuple: (A, B)): Unit =
    println(s"'${tuple._1}' -> ${tuple._2}")

  def fileChars: Iterator[String] =
    io.Source.fromResource("lorem").getLines()

}
