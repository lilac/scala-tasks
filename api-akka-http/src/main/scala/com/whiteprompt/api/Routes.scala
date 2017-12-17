package com.whiteprompt.api

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class Routes(implicit val system: ActorSystem) {
  val taskRoute: Route = new TaskRoutes().route
  val route: Route =
    pathPrefix("v1") {
      taskRoute
    } ~
    path("health-check"){
      get {
        complete("Akka HTTP API: up and running!")
      }
    }
}