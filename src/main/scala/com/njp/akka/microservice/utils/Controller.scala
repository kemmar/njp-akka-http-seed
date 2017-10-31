package com.njp.akka.microservice.utils

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import com.njp.akka.microservice.domain.NJPError
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

trait Controller extends Directives with FailFastCirceSupport {
  type NJPResult[T] = Either[NJPError, T]

  val route: Route

  private def njpComplete[T](resp: T, status: StatusCode)
                            (implicit _marshal: ToResponseMarshaller[T]): StandardRoute = complete(resp)

  def completion[T](response: Either[NJPError, T], status: StatusCode = StatusCodes.OK)
                   (implicit _marshal: ToResponseMarshaller[T]): StandardRoute = response match {
    case Right(resp) => njpComplete(resp, status)
//    case Left(error) => handleError(error)
  }

//  private def handleError: PartialFunction[NJPError, StandardRoute] = withErrorHandler.orElse {
//    case _@ServiceError(code, description, status) => njpComplete(ProcessingError(code, description), status)
//    case e: ProcessingError => njpComplete(e, StatusCodes.UnprocessableEntity)
//  }

  def withErrorHandler: PartialFunction[NJPError, StandardRoute] = PartialFunction.empty[NJPError, StandardRoute]
}