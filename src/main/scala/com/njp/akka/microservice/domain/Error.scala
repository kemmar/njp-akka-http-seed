package com.njp.akka.microservice.domain

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

trait NJPError {
  val code: String
  val description: String
}

case class ProcessingError(code: String, description: String) extends NJPError

case class ServiceError(code: String, description: String, status: StatusCode = StatusCodes.UnprocessableEntity) extends NJPError

case class OrchestrationError(code: String, description: String, status: StatusCode = StatusCodes.InternalServerError) extends NJPError