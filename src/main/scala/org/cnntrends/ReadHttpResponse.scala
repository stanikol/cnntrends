package org.cnntrends

import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCodes}
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object ReadHttpResponse {
  implicit class EntityToString(e: ResponseEntity) {
    def entityToString(implicit materializer: Materializer, executionContext: ExecutionContext): Future[String] =
      e.toStrict(120.seconds).map(_.data.utf8String)
  }

  def asString(httpResponse: HttpResponse)
              (implicit materializer: Materializer,
                   executionContext: ExecutionContext)
        : Future[String] = httpResponse match {
    case HttpResponse(StatusCodes.OK, _, entity, _) =>
      println("Got OK response.")
      entity.entityToString
    case HttpResponse(invalidResponseCode, _, entity, _) =>
      entity.entityToString.flatMap { entityText =>
        val msg = s"Got invalid http response code $invalidResponseCode: $entityText !\n"
        println(msg)
        Future.failed(new Exception(msg))
      }
  }

}
