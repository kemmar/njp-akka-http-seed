package com.njp.akka.microservice.utils

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.{ClientTransport, Http}
import akka.stream.Materializer
import com.njp.akka.microservice.utils.Constants.ProxyType._
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.slf4j.Logger

import scala.concurrent.ExecutionContext

abstract class HttpSupport(implicit log: Logger, conf: Config, system: ActorSystem, ec: ExecutionContext) extends FailFastCirceSupport {

  val endpointConfig: Config

  val ProxyType: ProxyType = EXTERNAL

  private def basicCredentials(username: String, password: String) = BasicHttpCredentials(username, password)

  private def addCredentials(req: HttpRequest): HttpRequest = {
    val hasCredentials = endpointConfig.hasPath("credentials.username")

    lazy val username = endpointConfig.getString("credentials.username")
    lazy val password = endpointConfig.getString("credentials.password")

    if (hasCredentials)
      req.addCredentials(basicCredentials(username, password))
    else req
  }

  private val proxyClientTransport = {
    val proxyConfig: Config = conf.atPath(if (ProxyType == INTERNAL) "proxy-internal" else "proxy")

    lazy val proxyHost = proxyConfig.getString("url")
    lazy val proxyPort = proxyConfig.getInt("port")
    lazy val proxyPassword = proxyConfig.getString("password")
    lazy val proxyUsername = proxyConfig.getString("username")

    lazy val auth = basicCredentials(proxyUsername, proxyPassword)

    ClientTransport.httpsProxy(InetSocketAddress.createUnresolved(proxyHost, proxyPort), auth)
  }

  private val settings = ConnectionPoolSettings(system).withTransport(proxyClientTransport)

  def logRequest(httpRequest: HttpRequest): HttpRequest = {
    log.info(
      s"""
         |Sending Request to: ${httpRequest.uri}
         |body: ${httpRequest.entity}
         |METHOD: ${httpRequest.method}
         |headers: ${httpRequest.headers}
     """.stripMargin)

    httpRequest
  }

  def logResponse(resp: HttpResponse): HttpResponse = {
    log.info(
      s"""
         |headers: ${resp.headers}
         |status: ${resp.status}
         |body: ${resp.entity}
     """.stripMargin)

    resp
  }

  def makeRequest[T](httpRequest: HttpRequest)(implicit fm: Materializer) = {
    logRequest(httpRequest)

    Http().singleRequest(addCredentials(httpRequest), settings = settings).map { resp =>
      logResponse(resp)
      resp.entity.toString
    }
  }
}
