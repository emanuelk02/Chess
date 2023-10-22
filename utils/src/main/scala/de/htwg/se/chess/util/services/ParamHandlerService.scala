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

import data.ChessJsonProtocol._
import patterns.ChainHandler


trait ParamHandlerService:
    
    def getValidatingParamHandler(
      fieldValidators: List[String => Boolean],
      resolveFunction: Seq[String] => String
      ) : ChainHandler[Seq[String], StandardRoute] = ChainHandler(
        ( fieldValidators.zipWithIndex.map( (validator, index) => (params: Seq[String]) => paramValidator(validator)(params(index))).toList ).appended(
        ( (params: Seq[String]) =>
          Some(complete(
            HttpEntity(
              ContentTypes.`application/json`,
              resolveFunction(params.toArray)
            )
          ))
        ))
      )

    def paramValidator(validator: String => Boolean): String => Option[StandardRoute] =
        (value: String) =>
          if validator(value)
            then None
            else Some(complete(BadRequest, s"""Invalid parameter: $value"""))

    def handleParamRequest(handler: ChainHandler[Seq[String], StandardRoute], errorMessage: String = "Something went wrong", params: String*): Route =
            handler.handleRequest(params)
              .getOrElse(complete {
                HttpResponse(InternalServerError, entity = errorMessage)
              })
