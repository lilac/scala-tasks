package com.whiteprompt.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.whiteprompt.TestData
import com.whiteprompt.api.utils.Json4sJacksonSupport
import com.whiteprompt.domain.TaskEntity
import com.whiteprompt.services.TaskManager
import org.scalatest.{ Matchers, WordSpec }

class TasksRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest {

  trait Scope extends TaskRoutes with Json4sJacksonSupport with TestData {
    override lazy val taskManager: TaskManager = new TaskManager(taskRepository())
  }

  "When sending a POST request, the Task API" should {
    "create a new Task and return a 201 Response if the request is valid" in new Scope {
      val name = "Create name"
      val description = "Create description"
      Post("/tasks", TaskData(name, description)) ~> route ~> check {
        response.status shouldEqual StatusCodes.Created
        header[Location] shouldBe defined
      }
    }
    "not create a Task and return a 400 Response if the request is not valid" in new Scope {
      val name = "" // Name must not be empty
      val description = "Create description"
      Post("/tasks", Map("name" -> name, "description" -> description)) ~> Route.seal(route) ~> check {
        response.status shouldEqual StatusCodes.BadRequest
      }
    }
  }

  "When sending a GET request, the Task API" should {
    "return a 200 Response with the requested Task if it exists" in new Scope {
      Get(s"/tasks/${taskEntity1.id}") ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
        responseAs[TaskEntity] shouldEqual taskEntity1
      }
    }
    "return a 404 Response if the requested Task does not exist" in new Scope {
      Get(s"/tasks/$nonExistentTaskId") ~> Route.seal(route) ~> check {
        response.status shouldEqual StatusCodes.NotFound
      }
    }
  }

  "When sending a PUT request, the Task API" should {
    "update the Task with the given data and return it back in a 200 Response" in new Scope {
      val updatedId = taskEntity1.id
      val updatedName = "Updated name"
      val updatedDescription = "Updated description"
      Put(s"/tasks/$updatedId", TaskData(updatedName, updatedDescription)) ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
        responseAs[TaskEntity] shouldEqual TaskEntity(updatedId, updatedName, updatedDescription)
      }
    }
    "not update the Task and return a 400 Response if the request is not valid" in new Scope {
      val updatedId = taskEntity1.id
      val name = "" // Name must not be empty
      val description = "Updated description"
      Put(s"/tasks/$updatedId", Map("name" -> name, "description" -> description)) ~> Route.seal(route) ~> check {
        response.status shouldEqual StatusCodes.BadRequest
      }
    }
    "return a 404 Response if the requested Task does not exist" in new Scope {
      val updatedId = nonExistentTaskId
      val updatedName = "Updated name"
      val updatedDescription = "Updated description"
      Put(s"/tasks/$updatedId", TaskData(updatedName, updatedDescription)) ~> Route.seal(route) ~> check {
        response.status shouldEqual StatusCodes.NotFound
      }
    }
  }

  "When sending a DELETE request, the Task API" should {
    "delete the requested Task and return a 204 Response if the Task exists" in new Scope {
      Delete(s"/tasks/${taskEntity1.id}") ~> route ~> check {
        response.status shouldEqual StatusCodes.NoContent
      }
    }
    "return a 404 Response if the requested Task does not exist" in new Scope {
      Delete(s"/tasks/$nonExistentTaskId") ~> Route.seal(route) ~> check {
        response.status shouldEqual StatusCodes.NotFound
      }
    }
  }

  "When sending a GET request, the Task API" should {
    "return a list of all Tasks" in new Scope {
      Get("/tasks") ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
        responseAs[List[TaskEntity]] should contain theSameElementsAs(Seq(taskEntity1, taskEntity2))
      }
    }
  }
}
