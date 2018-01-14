import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class WsSimulation extends Simulation {
  val httpConf = http
    .baseURL("http://localhost:8080")
    .userAgentHeader("Gatling2")
    .wsBaseURL("ws://localhost:8080")

  val scn = scenario("WebSocket")
    .exec(ws("Connect WS").open("/"))
    .pause(1)
    .exec(session => session.set("id", session.userId))
    .repeat(2, "i") {
      def id = "${id}-${i}"
      def text = "Hello, I am ${id}. This is message ${i}!"
      def msg = s"""
          |{
          | "id": "$id",
          | "text": "$text"
          |}
        """.stripMargin
      exec(
        ws("Say Hello WS")
        .sendText(msg)
        .check(wsAwait.within(10).until(1).regex(".*ack.*"))
      ).pause(1)
    }
    .pause(10)
    .exec(ws("Close WS").close)

  setUp(
    scn.inject(
      constantUsersPerSec(1000) during (1 seconds)
    )
  ).protocols(httpConf)
}
