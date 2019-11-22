package org.cnntrends

import java.time.{LocalDate, LocalDateTime}

object Models {
  case class RssFeed(id: Option[Int], title: String, title2: String, link: String,
                     pubDate: Option[LocalDateTime], description: String, topic: String)

  case class GoogleTrend(id: Option[Int], title: String, title2: String, link: String,
                         keyWords: String, searchCount: String, date: LocalDate)

  case class InTrendFeed(title: String, keyWords: String, pubDate: Option[LocalDateTime],
                         description: String, topic: String, link: String,
                         googleLink: String, searchCount: String, date: LocalDate)
  //select f."title", g."key_words", f."pubDate", f."description", f."topic", f."link",
  //        g."link" as "google_link", g."searchCount", g."date"
  //          from "rss_feeds" f inner join "google_trends" g on locate(g."title2", f."title2") >0;
}
