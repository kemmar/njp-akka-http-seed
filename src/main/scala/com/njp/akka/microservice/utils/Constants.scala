package com.njp.akka.microservice.utils

object Constants {

  object ProxyType extends Enumeration {
    type ProxyType = Value
    val INTERNAL, EXTERNAL = Value
  }
}
