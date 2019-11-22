package org.cnntrends

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.Sink
import org.cnntrends.Models.InTrendFeed
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

object Main {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("h2-slick")
  implicit val session: SlickSession = SlickSession.forConfig(databaseConfig)
  system.registerOnTermination(session.close())

  val routes =
    path("trends") {
      get {
//        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        onSuccess(getInTrendFeeds) { html =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,html))
        }
      }
    }

  def main(args: Array[String]): Unit = {

    val createTables = for{ _ <- SaveFeeds.createTableIfNotExists
                            _ <- SaveTrends.createTableIfNotExist }
                          yield ()

    val futureBinding = Http().bindAndHandle(routes, "0.0.0.0", 8080)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        println(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        println(s"ERROR: Failed to bind HTTP endpoint, terminating system. $ex")
        system.terminate()
    }

    for {
      _ <- createTables
      downloadFeeds = SaveFeeds.downloadAndSaveFeeds
      downloadTrends = SaveTrends.downloadAndSaveTrends
      _ <- downloadFeeds
      _ <- downloadTrends
    } yield ()

    StdIn.readLine("Press any key to quit 1")
    println("Quiting ...")
    futureBinding.flatMap(_.unbind)
      .onComplete(_ => system.terminate())
    Behaviors.empty
  }

  implicit val getFeedsInTrend =
    GetResult(r => InTrendFeed(r.nextString, r.nextString, r.nextTimestampOption.map(_.toLocalDateTime),
                          r.nextString, r.nextString, r.nextString,
                          r.nextString, r.nextString, r.nextDate.toLocalDate))

  def getInTrendFeeds(implicit session: SlickSession) : Future[String] =
    Slick
      .source(SQL.selectInTrendFeeds.as[InTrendFeed])
      .runWith(Sink.seq)
        .map{inTrends =>
          """<style>table {border-collapse: collapse;}
                    table, th, td { border: 1px solid black;}
             </style>"""+
          "<table>" +
          "<thead><td>Google Date</td><td>Title</td><td>Search count</td><td>Key words</td><td>Topic</td><td>Pub date</td><td>Link2</td><td>Descr</td></thead>"+
            (for{t <- inTrends}
              yield s"<tr><td>${t.date}</td><td><a href='${t.googleLink}'>${t.title}</a></td><td>${t.searchCount}</td>"+
                        s"<td>${t.keyWords}</td><td>${t.topic}</td><td>${t.pubDate.getOrElse("")}</td><td>${t.link}</td><td>${t.description}</td></tr>").mkString +
          "</table>"

        }

}
