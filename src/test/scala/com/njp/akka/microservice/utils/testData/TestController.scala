package com.njp.akka.microservice.utils.testData

import akka.http.scaladsl.server.Route
import com.njp.akka.microservice.utils.Controller

class TestController() extends Controller {
  override val route: Route = (path("hi") & get) { completion(Right("test"))}
}