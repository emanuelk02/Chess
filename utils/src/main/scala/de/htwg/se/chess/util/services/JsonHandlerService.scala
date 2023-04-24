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

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import spray.json._
import scala.concurrent.ExecutionContext
import scala.util.{Try,Success,Failure}

import ChessJsonProtocol._


trait JsonHandlerService:

    def checkForJsonFields(fields: List[String])(json: JsObject): Option[StandardRoute] =
        if fields.forall(json.fields.contains(_))
            then None
            else Some(complete(
                BadRequest,
                s"""Missing fields in body: ${fields.filterNot(json.fields.contains(_)).map(field => "\"" + field + "\"").mkString}"""
            ))
    
    def getValidatingJsonHandler(
      fieldValidators: Map[String, (String, JsObject) => Option[StandardRoute]],
      resolveFunction: Array[JsValue] => String
      ) : ChainHandler[JsObject, StandardRoute] = ChainHandler(
        checkForJsonFields(fieldValidators.keys.toList) _
        :: fieldValidators.map((field, validator) => validator(field, _)).toList
        ::: ( (json: JsObject) =>
              Some(complete(
                HttpEntity(
                  ContentTypes.`application/json`,
                  resolveFunction(fieldValidators.map((field, _) => json.getFields(field).head).toArray)
                )
              ))
            )
        :: Nil
      )

    def jsonFieldValidator[T: JsonReader](validator: T => Boolean): (String, JsObject) => Option[StandardRoute] =
        deserializingValidateJsonField[T](jsVal => Try(jsVal.convertTo[T]), validator) _

    private def deserializingValidateJsonField[T: JsonReader](deserializer: JsValue => Try[T], validator: T => Boolean)(field: String, json: JsObject): Option[StandardRoute] =
        deserializer(json.getFields(field).head) match
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
