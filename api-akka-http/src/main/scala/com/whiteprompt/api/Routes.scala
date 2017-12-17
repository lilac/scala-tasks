package com.whiteprompt.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object Routes {

  val route: Route =
    pathPrefix("v1") {
      TaskRoutes.route
    } ~
    path("health-check"){
      get {
        complete("Akka HTTP API: up and running!")
      }
    }
}