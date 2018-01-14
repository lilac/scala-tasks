import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class WsSimulation extends Simulation {
  val httpConf = http
    .baseURL("http://echo.websocket.org")
    .userAgentHeader("Gatling2")
    .wsBaseURL("wss://echo.websocket.org")

  val scn = scenario("WebSocket")
    .exec(ws("Connect WS").open("/"))
    .pause(1)
    .repeat(2, "i") {
      exec(ws("Say Hello WS")
        .sendText("""{"text": "Hello, this is message ${i}!"}""")
        .check(wsAwait.within(30).until(1).regex(".*message ${i}.*"))
      ).pause(1)
    }
    .exec(ws("Close WS").close)

  setUp(
    scn.inject(
      constantUsersPerSec(10) during (1 seconds)
    )
  ).protocols(httpConf)
}
