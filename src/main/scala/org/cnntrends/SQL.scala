package org.cnntrends

import java.sql.{Date, Timestamp, Types}
import java.time._

import org.cnntrends.Models.{GoogleTrend, RssFeed}
import org.cnntrends.Models.{GoogleTrend, RssFeed}
import slick.jdbc.H2Profile.api._
import slick.jdbc.SetParameter


object SQL {
  implicit val setlocalDate: AnyRef with SetParameter[LocalDate] = SetParameter[LocalDate] {
    case (ld, params) => params.setDate(Date.valueOf(ld))
  }

  implicit val setOptionLocalDateTime: AnyRef with SetParameter[Option[LocalDateTime]] = SetParameter[Option[LocalDateTime]] {
    case (Some(ld), params) => params.setTimestamp(Timestamp.valueOf(ld))
    case (None, params) => params.setNull(Types.TIMESTAMP)
  }

  def createFeedsIfNotExists =
    sqlu"""create table if not exists "rss_feeds" (
             "id" INTEGER NOT NULL AUTO_INCREMENT,
             "title" VARCHAR NOT NULL,
             "title2" VARCHAR NOT NULL,
             "link" VARCHAR NOT NULL,
             "pubDate" TIMESTAMP,
             "description" VARCHAR NOT NULL,
             "topic" VARCHAR NOT NULL);
             create unique index if not exists "rss_feeds_unique"
                      on "rss_feeds" ("title2","link","pubDate","topic");"""

  def insertFeed(feed: RssFeed):DBIO[Int] =
  //    sqlu"""insert into "rss_feeds"("title", "title2" , "link", "topic", "description", "pubDate")
  //              select '1','1','1','1','1','2019-01-01'
  //              where not exists (select "id" from "rss_feeds"
  //                                  where "title2"='1' and "link"='1' and "pubDate"='2019-01-01' and "topic"='1');"""
      sqlu"""insert into "rss_feeds"("title", "title2" , "link", "topic", "description", "pubDate")
                select ${feed.title}, ${feed.title2}, ${feed.link}, ${feed.topic}, ${feed.description}, ${feed.pubDate},
                where not exists (select "id" from "rss_feeds"
                                    where "title2"=${feed.title2} and "link"=${feed.link} and "pubDate"=${feed.pubDate} and "topic"=${feed.topic});"""

  def createTrendsIfNotExists =
    sqlu"""create table if not exists "google_trends"
             ("id" INTEGER NOT NULL AUTO_INCREMENT,
              "title" VARCHAR NOT NULL,
              "title2" VARCHAR NOT NULL,
              "link" VARCHAR NOT NULL,
              "key_words" VARCHAR NOT NULL,
              "searchCount" VARCHAR NOT NULL,
              "date" DATE NOT NULL);
              create unique index if not exists "google_trends_unique"
                        on "google_trends" ("title2","link", "date");"""

  def insertTrend(trend: GoogleTrend) =
//    sqlu"""insert into "google_trends"( "title", "title2", "link", "key_words", "searchCount", "date" )
//              select '1', '1', '1', '1', '1', '2019-01-01'
//              where not exists (select "id" from "google_trends"
//                                 where "title2"='1' and "link"='1' and "date"='2019-01-01'); """
    sqlu"""insert into "google_trends"( "title", "title2", "link", "key_words", "searchCount", "date" )
              select ${trend.title}, ${trend.title2}, ${trend.link}, ${trend.keyWords}, ${trend.searchCount}, ${trend.date}
              where not exists (select "id" from "google_trends"
                                 where "title2"=${trend.title2} and "link"=${trend.link} and "date"=${trend.date}); """

  def selectInTrendFeeds =
    sql"""select f."title", g."key_words", f."pubDate", f."description", f."topic", f."link", g."link" as "google_link", g."searchCount", g."date"
          from "rss_feeds" f inner join "google_trends" g on locate(g."title2", f."title2") >0;"""

  def delme = sql"select 11;"
//  implicit val localDateType = MappedColumnType.base[LocalDate, Date](
//    {l => Date.valueOf(l)},
//    {d => d.toLocalDate}
//  )
//
//  implicit val localDateTimeToTimestamp = MappedColumnType.base[LocalDateTime, Timestamp](
//    l => Timestamp.valueOf(l),
//    t => t.toLocalDateTime
//  )

}
