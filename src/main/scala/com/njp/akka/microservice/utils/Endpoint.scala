package com.njp.akka.microservice.utils

import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.slf4j.Logger
import com.njp.akka.microservice.utils.Constants.ProxyType._

import scala.concurrent.ExecutionContext

class Endpoint(endpoint: String, service: String)(implicit config: Config, log: Logger, system: ActorSystem, ec: ExecutionContext) extends HttpSupport {

  lazy val endpointConfig = config.atPath(service)

  def url(pathVariables: String*) = String.format(endpointConfig.getString("url"), pathVariables)

  override val ProxyType = INTERNAL
}
