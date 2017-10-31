package com.njp.akka.microservice

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.{Directives, HttpApp, Route, StandardRoute}
import com.njp.akka.microservice.domain.{NJPError, ProcessingError, ServiceError}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

trait Controller extends Directives with FailFastCirceSupport{
  type NJPResult[T] = Either[NJPError, T]

  val route: Route

  private def njpComplete[T](resp: T, status: StatusCode)(implicit _marshal: ToResponseMarshaller[T]): StandardRoute =
    complete(resp)

  def completion[T](response: Either[NJPError, T], status: StatusCode = StatusCodes.OK)(implicit _marshal: ToResponseMarshaller[T]): StandardRoute = response match {
    case Right(resp) => njpComplete(resp, status)(_marshal)
    case Left(error) => handleError(error)
  }

  private def handleError: PartialFunction[NJPError, StandardRoute] = withErrorHandler.orElse {
    case _@ServiceError(code, description, status) => njpComplete(ProcessingError(code, description), status)
    case e: ProcessingError => njpComplete(e, StatusCodes.UnprocessableEntity)
  }

  def withErrorHandler: PartialFunction[NJPError, StandardRoute] = PartialFunction.empty[NJPError, StandardRoute]
}

case class Foo(bar: String)

class TestController extends Controller {

  override val route: Route =
    path("hello") {
      entity(as[Foo]) { foo =>
        put {
          completion(Right(foo))
        }
      }
    }
}

object WebServer extends HttpApp {

  override def routes: Route = new TestController().route

  def routeBuilder(ctrl: Seq[Controller] = Seq.empty[Controller]) = startServer("localhost", 8080)

}