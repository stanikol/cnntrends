package org.cnntrends

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{Sink, Source}
import org.cnntrends.CleanTitle.cleanTitle
import org.cnntrends.Models.GoogleTrend
import org.openqa.selenium.firefox.{FirefoxBinary, FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.{By, WebElement}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Success, Try}

object SaveTrends {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("h2-slick")
  implicit val session: SlickSession = SlickSession.forConfig(databaseConfig)
  system.registerOnTermination(session.close())

  val trendsUrl = "https://trends.google.com/trends/trendingsearches/daily?geo=US"
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")

  def main(args: Array[String]): Unit = {
    (for {  _ <- createTableIfNotExist
            _ <- downloadAndSaveTrends
         } yield ())
    .onComplete { _ =>
          session.db.close()
          system.terminate()
      }
  }

  def createTableIfNotExist(implicit session: SlickSession): Future[Int] = {
    session.db.run(SQL.createTrendsIfNotExists)
      .recoverWith { case e =>
        val m = s"Error creating `google_trends` table: ${e.getMessage} $e"
        println(m)
        Future.failed(new Exception(m))
      }
  }

  def downloadAndSaveTrends(implicit session: SlickSession): Future[Done] = {
    {
      Source.tick(0.second, 5.minutes, trendsUrl)
        .map( downloadTrendsSelenium )
        .collect { case Success(trends) => trends }
        .mapConcat(identity)
        .via(Slick.flow{SQL.insertTrend})
        .map(i=>println(s"$i google trend record is inserted into db."))
        .runWith(Sink.ignore)
    }
  }

  def downloadTrendsSelenium(url: String): Try[List[GoogleTrend]] = {
    println("Downloading google trends for news ... ")
    val webDriver = startFirefox
    try {
      for {
        wd <- webDriver
        date <- Try {
          wd.get(url)
          println("Trends url is opened in selenium.")
          val css = "div.feed-list-wrapper > div.content-header > div.content-header-title"
          val d = wd.findElementByCssSelector(css).getText
          LocalDate.parse(d, dateTimeFormatter)
        }
        trendsList: List[GoogleTrend] <- Try {
          wd.findElementsByClassName("feed-item-header").asScala.toList
            .map(parseHTML(_, date))
            .collect { case Success(value) => value }
        }
      } yield trendsList
    } finally {
      webDriver.map(_.quit())
      println("Firefox is closed")
    }
  }

  def parseHTML(element: WebElement, date: LocalDate): Try[GoogleTrend] = Try {
//      println("parseHTML ...")
      val e = element.findElement(By.cssSelector("div.summary-text > a"))
      val (title, link) = (e.getText, e.getAttribute("href"))
      val searchCount = element.findElement(By.cssSelector("div.search-count-title")).getText
      val keyWords = element.findElement(By.cssSelector("div.details-top span > a")).getText
      val googleTrend = GoogleTrend(None, title, cleanTitle(title), link, keyWords, searchCount, date)
//      println("parseHTML end.")
      googleTrend
  }

  def startFirefox: Try[FirefoxDriver] = Try {
    val firefoxBinary = new FirefoxBinary
    firefoxBinary.addCommandLineOptions("--headless")
    //  System.setProperty("webdriver.gecko.driver", "/opt/geckodriver")
    val firefoxOptions = new FirefoxOptions
    firefoxOptions.setBinary(firefoxBinary)
    new FirefoxDriver(firefoxOptions)
  }

//  def saveTrendsToDb(trend: GoogleTrend) = {
//    session.db.run(googleTrends += trend)
//      .map(_ => println(s"inserted in db $trend"))
//      .recoverWith {
//        case _: JdbcSQLIntegrityConstraintViolationException =>
//          println("Already in db")
//          Future.successful()
//        case e =>
//          val m = s"Error SQL: ${e.getMessage} $e"
//          println(m)
//          Future.failed(e)
//      }
//  }
}
