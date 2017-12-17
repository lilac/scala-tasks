package com.whiteprompt

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import com.whiteprompt.api.Routes
import com.whiteprompt.conf.Config
import com.whiteprompt.persistence.TaskRepository
import com.whiteprompt.services.TaskService

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App with Config {
  implicit val system = ActorSystem("api-akka-http-system")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val log = Logging(system, getClass)

  // Services
  val taskService = system.actorOf(FromConfig.props(TaskService.props(TaskRepository())), TaskService.Name)

  // Initialize server
  Http().bindAndHandle(Routes.route, httpInterface, httpPort)

  scala.sys.addShutdownHook{
    log.info("Shutting down server and actor system")
    system.terminate()
    Await.result(system.whenTerminated, 30 seconds)
  }
}

