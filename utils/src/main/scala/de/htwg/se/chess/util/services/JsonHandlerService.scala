/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package util
package services

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import spray.json._
import scala.concurrent.ExecutionContext
import scala.util.{Try,Success,Failure}
import akka.http.scaladsl.unmarshalling.Unmarshaller


trait JsonHandlerService:

    def checkForJsonFields(fields: List[String])(json: JsObject): Option[StandardRoute] =
        if fields.forall(json.fields.contains(_))
            then None
            else Some(complete(
                BadRequest,
                s"""Missing fields in body: ${fields.filterNot(json.fields.contains(_)).map(field => "\"" + field + "\"").mkString}"""
            ))
    
    def validateJsonField[T: JsonReader](field: String, validator: T => Boolean)(json: JsObject): Option[StandardRoute] =
        Try(json.getFields(field).head.convertTo[T]) match
            case Success(value) =>
                if validator(value)
                    then None
                    else Some(complete(BadRequest, s"""Invalid $field: ${json.getFields(field).head}"""))
            case Failure(_) => Some(complete(BadRequest, s"""Invalid $field: ${json.getFields(field).head}"""))

    def handleRequestEntity(handler: ChainHandler[JsObject, StandardRoute], errorMessage: String = "Something went wrong"): Route =
        entity(as[String]) { str =>
            handler.handleRequest(str.parseJson.asJsObject)
              .getOrElse(complete {
                HttpResponse(InternalServerError, entity = errorMessage)
              })
            }
