package com.njp.akka.microservice.utils

import akka.http.scaladsl.server._

trait WebApplication extends HttpApp {

  val serviceControllers: Seq[Controller]

  override def routes: Route = serviceControllers.map(_.route).reduce(_ ~ _)
}