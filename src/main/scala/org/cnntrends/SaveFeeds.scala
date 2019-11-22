package org.cnntrends

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{Sink, Source}
import org.cnntrends.CleanTitle.cleanTitle
import org.cnntrends.Models.RssFeed
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}
import scala.xml.XML

object SaveFeeds {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("h2-slick")
  implicit val session: SlickSession = SlickSession.forConfig(databaseConfig)

  system.registerOnTermination(session.close())

  val rssUrl = "http://rss.cnn.com/rss/edition.rss"

  def main(args: Array[String]): Unit = {
    (for {
      _ <- createTableIfNotExists
      _ <- downloadAndSaveFeeds
    } yield ())
      .onComplete { _ =>
        session.db.close()
        system.terminate()
      }

  }

  def createTableIfNotExists(implicit session: SlickSession): Future[Int] = {
    session.db.run(SQL.createFeedsIfNotExists)
      .recoverWith { case e =>
        val m = s"Error creating `rss_feeds` table: ${e.getMessage} $e"
        println(m)
        Future.failed(new Exception(m))
      }
  }

  def downloadAndSaveFeeds(implicit session: SlickSession): Future[Done] = {
    Source.tick(0.second, 5.minutes, CnnRssFeeds.cnnRssFeeds)
      .map { f => println("Downloading feeds ..."); f }
      .mapConcat(identity)
      .throttle(1, 5.seconds)
      .mapAsync(1) { case (feedName, feedUrl) =>
        downloadAndParseOneTopic(feedName, feedUrl)
      }
      .mapConcat(identity)
      .filter(_.title.nonEmpty)
      .via(Slick.flow(SQL.insertFeed))
      .map(i=>println(s"$i feed is inserted into db."))
      .runWith(Sink.ignore)
  }

  def downloadAndParseOneTopic(feedName: String, feedUrl: String): Future[List[RssFeed]] = {
    println(s"Downloading feeds for `$feedName` ...")
    Http()
      .singleRequest(HttpRequest(uri = feedUrl))
      .flatMap(ReadHttpResponse.asString)
      .map { s => parseXML(s, feedName) }
      .collect{case Success(value) => value}
  }


  def parseXML(s: String, feedName: String): Try[List[RssFeed]] = Try{
    val xml = XML.loadString(s)
    (xml \\ "item").toList.map { item =>
      val t = (item \ "title").text
      val l = (item \ "link").text
      val p = (item \ "pubDate").text
      val d = (item \ "description").text
      val formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss z")
      val pd = Try(LocalDateTime.parse(p, formatter)).toOption
      RssFeed(None, t, cleanTitle(t), l, pd, d, feedName)
    }
  }.recoverWith{case e =>
    println(s"Error in `parseXML`: ${e.getLocalizedMessage} $e")
    Failure(e)
  }

//  def saveFeedToDb(feed: RssFeed): Future[Unit] = {
//    session.db.run(rssFeeds += feed)
//      .map(_ => println(s"inserted $feed"))
//      .recoverWith {
//        case e: JdbcSQLIntegrityConstraintViolationException =>
//          println("Already in db")
//          Future.successful()
//        case e =>
//          val m = s"Error SQL: ${e.getMessage} $e"
//          println(m)
//          Future.failed(e)
//      }
//  }

}
