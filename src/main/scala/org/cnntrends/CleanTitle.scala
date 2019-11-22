package org.cnntrends

object CleanTitle {
//
//  val test = "This's a, test. \\/:+=-_@#$%^&*<>?,.±§!\"'|{}[]~`` So  I - try to ceck. "
//
//  def main(args: Array[String]): Unit = {
//    println(test)
//    println(s"`${cleanTitle(test)}`")
//  }

  def cleanTitle(s: String): String = {
    s.replaceAll("[\\W\\_]", " ")
      .replaceAll("\\s+", " ")
      .toLowerCase.trim
  }

}
