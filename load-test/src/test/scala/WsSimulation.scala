import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class WsSimulation extends Simulation {
  val httpConf = http
    .baseURL("http://echo.websocket.org")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling2")
    .wsBaseURL("wss://echo.websocket.org")

  val scn = scenario("WebSocket")
//    .exec(http("Home").get("/"))
//    .pause(1)
//    .exec(session => session.set("id", "Steph" + session.userId))
//    .exec(http("Login").get("/room?username=${id}"))
//    .pause(1)
    .exec(ws("Connect WS").open("/"))
    .pause(1)
    .repeat(2, "i") {
      exec(ws("Say Hello WS")
        .sendText("""{"text": "Hello, this is message ${i}!"}""")
        .check(wsAwait.within(30).until(1).regex(".*message ${i}.*"))
      ).pause(1)
    }
    .exec(ws("Close WS").close)

  setUp(scn.inject(
    constantUsersPerSec(10) during (1 seconds)
    )
  ).protocols(httpConf)
}
