package gatling_Abgabe

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class RecordedSimulation extends Simulation {

  private val httpProtocol = http
    .baseUrl("http://localhost:8081")
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.32.2")
  
  private val headers_0 = Map("Postman-Token" -> "1ca4274c-f4f3-48d9-b427-c55b76046ed2")
  
  private val headers_1 = Map("Postman-Token" -> "66d79ccc-4ab0-4ed8-8a79-62458670f4af")
  
  private val headers_2 = Map("Postman-Token" -> "68a6d078-7cda-4485-974c-dde132e2aa11")
  
  private val headers_3 = Map("Postman-Token" -> "15c6a393-c2df-484d-8f86-9c1765502da3")
  
  private val headers_4 = Map("Postman-Token" -> "21d2c3dc-141a-4c94-9f47-261882df5965")
  
  private val headers_5 = Map("Postman-Token" -> "063721ec-a216-4ec2-98b7-b04e083d7212")
  
  private val headers_6 = Map("Postman-Token" -> "17cb4587-289e-49ab-a7ed-c0346b9583f0")
  
  private val headers_7 = Map("Postman-Token" -> "593c9e82-acd6-4a66-b761-166701b2cf23")
  
  private val headers_8 = Map("Postman-Token" -> "4bd406da-3631-4534-98b3-17c99d8ce144")
  
  private val headers_9 = Map("Postman-Token" -> "ea94fd94-894d-4eeb-8b77-e860b72af581")
  
  private val headers_10 = Map("Postman-Token" -> "f4d30b19-3baf-4216-bc29-4647b50d4879")
  
  private val headers_11 = Map("Postman-Token" -> "3a4283c9-1fb5-4298-9154-42c115633962")
  
  private val headers_12 = Map("Postman-Token" -> "fc4b7707-e70c-4470-8468-a402934451b7")
  
  private val headers_13 = Map("Postman-Token" -> "750375b6-eba2-4cf5-a90c-244baff7e4c9")
  
  private val headers_14 = Map("Postman-Token" -> "7bb9df51-ff18-463b-858e-a9376c475f01")
  
  private val headers_15 = Map("Postman-Token" -> "1c6612f0-64b3-491e-9df9-fdccbd3cb5e5")
  
  private val headers_16 = Map(
  		"Content-Type" -> "text/plain",
  		"Postman-Token" -> "84c01759-22b7-4c10-953f-4d34e9ab6301"
  )
  
  private val headers_17 = Map("Postman-Token" -> "42aca165-80f5-4a56-af1e-05aaf90d52e2")
  
  private val headers_18 = Map("Postman-Token" -> "b7304c24-c134-48bd-9f32-bb97a0078e43")
  
  private val headers_19 = Map(
  		"Content-Type" -> "application/json",
  		"Postman-Token" -> "9bf92264-8f1d-454f-b5c3-9c54ff1cdc1e"
  )
  
  private val headers_20 = Map(
  		"Content-Type" -> "application/json",
  		"Postman-Token" -> "3969b486-38db-43e1-b3fd-af03cf05fdfd"
  )
  
  private val headers_21 = Map(
  		"Content-Type" -> "application/json",
  		"Postman-Token" -> "3ecf5089-f8cd-4a16-a27f-9bf3ac817e24"
  )
  
  private val headers_22 = Map("Postman-Token" -> "b6b08df1-5480-4703-bd4f-287d559f5aee")
  
  private val headers_23 = Map(
  		"Content-Type" -> "application/json",
  		"Postman-Token" -> "e0ba308e-daf8-468a-8ad3-4c999e1e5598"
  )
  
  private val headers_24 = Map(
  		"Content-Type" -> "application/json",
  		"Postman-Token" -> "6bf29f16-bf66-4286-81a0-1bc7fd2be3fa"
  )
  
  private val headers_25 = Map(
  		"Content-Type" -> "application/json",
  		"Postman-Token" -> "a3c1eb1f-c540-4d1b-a666-69a775fd30e9"
  )
  
  private val headers_26 = Map(
  		"Content-Type" -> "text/plain",
  		"Postman-Token" -> "d64f1be3-5b50-439b-bfb9-de586ac2add4"
  )
  
  private val headers_27 = Map(
  		"Content-Type" -> "application/json",
  		"Postman-Token" -> "369f787b-747a-435d-9b91-36b1903f85a3"
  )
  
  private val headers_28 = Map("Postman-Token" -> "7a465547-0a31-417f-8a1c-6e8e63900a16")
  
  private val headers_29 = Map("Postman-Token" -> "f8a93c14-2314-4d81-86b0-8a05c2e89baa")
  
  private val headers_30 = Map("Postman-Token" -> "27708ddd-769c-445c-9f98-9a4154229c40")
  
  private val headers_31 = Map("Postman-Token" -> "dd320dd6-9a7a-4040-9647-b63f4244921e")
  
  private val headers_32 = Map(
  		"Content-Type" -> "text/plain",
  		"Postman-Token" -> "eb7d568a-9619-4bdc-9e62-d70984975bc7"
  )
  
  private val headers_33 = Map("Postman-Token" -> "baf37fa2-4928-47f2-8dad-0e0f41df5e55")
  
  private val headers_34 = Map("Postman-Token" -> "85dd5975-eaf0-4a33-a8b8-d7379aef4923")
  
  private val headers_35 = Map("Postman-Token" -> "580d3ec1-aa2a-4ec7-bbb4-c517f80f5686")
  
  private val headers_36 = Map("Postman-Token" -> "f6fe731c-5398-45fc-bb78-a28ca53ba21b")
  
  private val headers_37 = Map("Postman-Token" -> "6912c871-f37d-4a5f-856b-fc553c1ef896")
  
  private val uri1 = "localhost"

  private val scn = scenario("RecordedSimulation")
    .exec(
      http("request_0")
        .get("/controller/fen")
        .headers(headers_0)
    )
    .pause(2)
    .exec(
      http("request_1")
        .get("/controller/cells?tile=%22D1%22")
        .headers(headers_1)
    )
    .pause(1)
    .exec(
      http("request_2")
        .get("/controller/states?query=check")
        .headers(headers_2)
    )
    .pause(2)
    .exec(
      http("request_3")
        .get("/controller/states?query=playing")
        .headers(headers_3)
    )
    .pause(2)
    .exec(
      http("request_4")
        .get("/controller/states?query=size")
        .headers(headers_4)
    )
    .pause(2)
    .exec(
      http("request_5")
        .get("/controller/states?query=selected")
        .headers(headers_5)
    )
    .pause(1)
    .exec(
      http("request_6")
        .get("/controller/states?query=king")
        .headers(headers_6)
    )
    .pause(2)
    .exec(
      http("request_7")
        .get("/controller/moves?tile=%22D2%22")
        .headers(headers_7)
    )
    .pause(2)
    .exec(
      http("request_8")
        .put("/controller/moves?from=%22D2%22&to=%22D4%22")
        .headers(headers_8)
    )
    .pause(2)
    .exec(
      http("request_9")
        .put("/controller/undo")
        .headers(headers_9)
    )
    .pause(2)
    .exec(
      http("request_10")
        .put("/controller/redo")
        .headers(headers_10)
    )
    .pause(2)
    .exec(
      http("request_11")
        .put("/controller/cells?tile=%22D4%22&piece=Q")
        .headers(headers_11)
    )
    .pause(2)
    .exec(
      http("request_12")
        .put("/controller/fen?fen=Q2rB2b/q1p1Pp2/P1p2r2/pN1p1pNP/pP2nP2/2nPP1p1/6KP/3k2B1%20w%20Kq%20-%200%201")
        .headers(headers_12)
    )
    .pause(2)
    .exec(
      http("request_13")
        .put("/controller/states?query=playing&state=false")
        .headers(headers_13)
    )
    .pause(1)
    .exec(
      http("request_14")
        .put("/controller/states?query=playing&state=true")
        .headers(headers_14)
    )
    .pause(2)
    .exec(
      http("request_15")
        .put("/controller/states?query=selected&tile=%22D7%22")
        .headers(headers_15)
    )
    .pause(1)
    .exec(
      http("request_16")
        .post("/controller/users?name=Gatling%20")
        .headers(headers_16)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0016_request.txt"))
    )
    .pause(2)
    .exec(
      http("request_17")
        .put("/controller/saves")
        .headers(headers_17)
    )
    .pause(2)
    .exec(
      http("request_18")
        .get("/controller/saves")
        .headers(headers_18)
    )
    .pause(11)
    .exec(
      http("request_19")
        .get("http://" + uri1 + ":8082/moves")
        .headers(headers_19)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0019_request.json"))
    )
    .pause(2)
    .exec(
      http("request_20")
        .get("http://" + uri1 + ":8082/moves?tile=%22D3%22")
        .headers(headers_20)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0020_request.json"))
    )
    .pause(1)
    .exec(
      http("request_21")
        .get("http://" + uri1 + ":8082/attacks?tile=%22D3%22")
        .headers(headers_21)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0021_request.json"))
    )
    .pause(1)
    .exec(
      http("request_22")
        .post("https://" + uri1 + ":8083/users?name=Gatling")
        .headers(headers_22)
        .check(status.is(400))
    )
    .pause(2)
    .exec(
      http("request_23")
        .post("https://" + uri1 + ":8083/saves?user=Gatling&name=gatlingtest")
        .headers(headers_23)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0023_request.json"))
    )
    .pause(2)
    .exec(
      http("request_24")
        .post("https://" + uri1 + ":8083/saves?user-id=1")
        .headers(headers_24)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0024_request.json"))
    )
    .pause(2)
    .exec(
      http("request_25")
        .post("https://" + uri1 + ":8083/users/1/saves?name=gatlingtest2")
        .headers(headers_25)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0025_request.json"))
    )
    .pause(2)
    .exec(
      http("request_26")
        .post("https://" + uri1 + ":8083/users?name=Gatling")
        .headers(headers_26)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0026_request.txt"))
        .check(status.is(400))
    )
    .pause(2)
    .exec(
      http("request_27")
        .put("https://" + uri1 + ":8083/saves?id=1")
        .headers(headers_27)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0027_request.txt"))
        .check(status.is(404))
    )
    .pause(3)
    .exec(
      http("request_28")
        .put("https://" + uri1 + ":8083/users?id=1&name=gatling1")
        .headers(headers_28)
    )
    .pause(2)
    .exec(
      http("request_29")
        .get("https://" + uri1 + ":8083/saves?id=1")
        .headers(headers_29)
        .check(status.is(404))
    )
    .pause(4)
    .exec(
      http("request_30")
        .get("https://" + uri1 + ":8083/users?id=1")
        .headers(headers_30)
        .check(status.is(404))
    )
    .pause(1)
    .exec(
      http("request_31")
        .get("https://" + uri1 + ":8083/users?name=Gatling")
        .headers(headers_31)
    )
    .pause(3)
    .exec(
      http("request_32")
        .get("https://" + uri1 + ":8083/users?name=Gatling")
        .headers(headers_32)
        .body(RawFileBody("gatling_Abgabe/recordedsimulation/0032_request.json"))
    )
    .pause(2)
    .exec(
      http("request_33")
        .get("https://" + uri1 + ":8083/users/2/saves")
        .headers(headers_33)
    )
    .pause(2)
    .exec(
      http("request_34")
        .get("https://" + uri1 + ":8083/hash-checks?id=2")
        .headers(headers_34)
    )
    .pause(2)
    .exec(
      http("request_35")
        .delete("https://" + uri1 + ":8083/saves?id=1")
        .headers(headers_35)
        .check(status.is(404))
    )
    .pause(3)
    .exec(
      http("request_36")
        .delete("https://" + uri1 + ":8083/users?id=1")
        .headers(headers_36)
        .check(status.is(500))
    )
    .pause(25)
    .exec(
      http("request_37")
        .delete("https://" + uri1 + ":8083/users?id=2")
        .headers(headers_37)
    )

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
