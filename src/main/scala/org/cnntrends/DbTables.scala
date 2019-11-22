package org.cnntrends

import java.time.{LocalDate, LocalDateTime}

import org.cnntrends.Models.{RssFeed, _}
import org.cnntrends.Models.{GoogleTrend, RssFeed}
import slick.jdbc.H2Profile.api._

object DbTables {

  val rssFeeds = TableQuery[RssFeedTable]
  val googleTrends = TableQuery[GoogleTrendTable]

  //  case class User(id: Option[Int], first: String, last: String)
  class RssFeedTable(tag: Tag) extends Table[RssFeed](tag, "rss_feeds") {
    def * = (id.?, title, title2, link, pubDate, description, topic) <> (RssFeed.tupled, RssFeed.unapply)

    def id = column[Int]("id", O.AutoInc)

    def title = column[String]("title")

    def title2 = column[String]("title2")

    def link = column[String]("link")

    def pubDate = column[Option[LocalDateTime]]("pubDate")

    def description = column[String]("description")

    def topic = column[String]("topic")

    def idx = index("rss_feeds_unique", (title2, link, pubDate, topic), unique = true)
  }

  class GoogleTrendTable(tag: Tag) extends Table[GoogleTrend](tag, "google_trends") {
    def * = (id.?, title, title2, link, keyWords, viewsCount, date) <> (GoogleTrend.tupled, GoogleTrend.unapply)

    def id = column[Int]("id", O.AutoInc)

    def keyWords = column[String]("key_words")

    def viewsCount = column[String]("searchCount")

    def title = column[String]("title")

    def title2 = column[String]("title2")

    def link = column[String]("link")

    def date  = column[LocalDate]("date")

    def idx = index("google_trends_unique", (title2, link), unique = true)
  }

}
