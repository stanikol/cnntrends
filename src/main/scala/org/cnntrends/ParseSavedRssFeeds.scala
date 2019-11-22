package org.cnntrends

import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.Try
import scala.xml.XML._
//import scala.jdk.CollectionConverters._
import DbTables._
import slick.jdbc.SQLiteProfile.api._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

object ParseSavedRssFeeds extends App {

  val db = Database.forConfig("db")
  db.run(DBIO.seq(DbTables.rssFeeds.schema.create))
    .recover{case e =>println(s"Slick db error ${e.getLocalizedMessage}")}

  Files.walk(Paths.get("/Users/snc/scala/cnntest/rsss"))
    .iterator().asScala.toList
    .filter(Files.isRegularFile(_))
    .map { file =>
//    println(file)
    val xml = loadFile(file.toFile)
      (xml \\ "item").map{ item =>
        Try{
          val t = (item \ "title").text
          val l = (item \ "link").text
          val p = (item \ "pubDate").text
          val d = (item \ "description").text
          val formatter =  DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss z")
          val pd = Try(LocalDateTime.parse(p, formatter)).toOption

          if(pd.isEmpty || !List(t,l,d).forall(_.nonEmpty))
            println(s"\t=->  $file Empty t=`$t` l=`$l` d=`$d` p=`$p`")
          val rssFeed = Models.RssFeed(None, t, CleanTitle.cleanTitle(t), l, pd, d, file.getFileName.toString)
          db.run(rssFeeds += rssFeed).map(i=> println(s"inserted $i"))
        }.recover{case e =>
          println(s"\n\n $file Error XML ${e.getLocalizedMessage} in $item \n\n")
        }
    }
    db.close()
  }



}
