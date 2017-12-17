package com.whiteprompt.api

import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.whiteprompt.api.utils.Json4sJacksonSupport
import com.whiteprompt.domain.Task
import com.whiteprompt.persistence.TaskRepository
import com.whiteprompt.services.TaskManager

case class TaskData(name: String, description: String) extends Task {
  require(name.nonEmpty)
  require(description.nonEmpty)
}

class TaskRoutes(implicit val system: ActorSystem) extends Json4sJacksonSupport {
  implicit val timeout = Timeout(5 seconds)
  private val ec = system.dispatchers.lookup("contexts.single-thread")

  lazy val taskManager: TaskManager = new TaskManager(TaskRepository()(ec))

  def create: Route =
    (pathEnd & post & entity(as[TaskData])) { task =>
      extractUri { uri =>
        onSuccess(taskManager.create(task)) { task =>
          respondWithHeader(Location(s"$uri/${task.id}")) {
            complete(StatusCodes.Created)
          }
        }
      }
    }

  def retrieve: Route =
    (path(JavaUUID) & get) { id =>
      onSuccess(taskManager.retrieve(id)) {
        case Some(task) => complete(task)
        case None => complete(StatusCodes.NotFound)
      }
    }

  def update: Route =
    (path(JavaUUID) & put & entity(as[TaskData])) { (id, task)  =>
      onSuccess(taskManager.update(id, task)) {
        case Some(task) => complete(task)
        case None => complete(StatusCodes.NotFound)
      }
    }

  def remove: Route =
    (path(JavaUUID) & delete) { id =>
      onSuccess(taskManager.delete(id)) {
        case Some(_) => complete(StatusCodes.NoContent)
        case None => complete(StatusCodes.NotFound)
      }
    }

  def list: Route =
    (pathEnd & get) {
      onSuccess(taskManager.list()) { tasks =>
        complete(tasks)
      }
    }

  def route: Route =
    pathPrefix("tasks") {
      create ~
      retrieve ~
      update ~
      remove ~
      list
    }
}

