package com.njp.akka.microservice.utils.testData

import com.njp.akka.microservice.utils.{Controller, WebApplication}

class ExampleDependency(controller: Controller*) extends WebApplication {
  val serviceControllers = controller

  startServer("localhost",8088)
}
