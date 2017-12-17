package com.whiteprompt.services

import java.util.UUID

import akka.actor.{ Actor, Props }
import akka.pattern.pipe
import com.whiteprompt.domain.{ Task, TaskEntity }
import com.whiteprompt.persistence.TaskRepository
import scala.concurrent.{ ExecutionContext, Future }

class TaskManager(val taskRepository: TaskRepository) {

  def create(task: Task): Future[TaskEntity] = {
    val id = UUID.randomUUID()
    taskRepository.create(TaskEntity(id, task.name, task.description))
  }

  def update(id: UUID, toUpdate: Task): Future[Option[TaskEntity]] = {
    taskRepository.update(TaskEntity(id, toUpdate.name, toUpdate.description))
  }

  def retrieve(id: UUID): Future[Option[TaskEntity]] = {
    taskRepository.retrieve(id)
  }

  def delete(id: UUID): Future[Option[TaskEntity]] = {
    taskRepository.delete(id)
  }

  def list(): Future[Seq[TaskEntity]] = {
    taskRepository.list()
  }
}

class TaskService(taskRepository: TaskRepository) extends TaskManager(taskRepository) with Actor {
  import TaskService._

  implicit val ec: ExecutionContext = context.dispatcher
  val api = new TaskManager(taskRepository)

  override def receive: Receive = {
    case CreateTask(task) =>
      create(task) pipeTo sender

    case UpdateTask(id, task) =>
      update(id, task) pipeTo sender

    case RetrieveTask(taskId) =>
      retrieve(taskId) pipeTo sender

    case DeleteTask(taskId) =>
      delete(taskId) pipeTo sender

    case ListTasks =>
      list pipeTo sender
  }

}

object TaskService {

  val Name = "task-service"

  def props(taskRepository: TaskRepository): Props = {
    Props(classOf[TaskService], taskRepository)
  }

  case class CreateTask(task: Task)
  case class RetrieveTask(taskId: UUID)
  case class UpdateTask(taskId: UUID, task: Task)
  case class DeleteTask(taskId: UUID)
  case object ListTasks
}