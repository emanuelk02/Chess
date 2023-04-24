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
package legality

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import spray.json._

import LegalityComputer._
import LegalityService._
import util.Piece
import util.Piece._
import util.PieceColor._
import util.Tile
import util.Matrix
import util.FenParser._
import util.ChessState
import util.services.ChessJsonProtocol._
import spray.json.JsValue


class LegalityServiceSpec extends AnyWordSpec with BeforeAndAfterAll with ScalatestRouteTest:
  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "The LegalityComputerService " should {
    var fen = ""
    var tile = Tile("A1")
    val route = LegalityService.route
    /*
     * Test for path /compute/tile
     * 
     * Uses getLegalMoves(fen, tile) to get the legal moves for a given tile
     * It accepts a json object with the fields "fen" and "tile"
     * It returns a json array of with the tiles the piece on the given tile can move to
     */
    "respond with legal moves for a Post request with a FEN and a tile on route /compute/tile" in {
      fen = "8/6r1/8/8/8/3Q2K1/8/8 w - 0 1"
      Post("/compute?tile=\"D3\"", s"""{"fen":"$fen"}""") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[JsValue] shouldEqual getLegalMoves(fen, Tile("D3")).toJson
      }
    }
    /*
     * Test for path /compute/all
     * 
     * Uses getLegalMoves(fen) to get the legal moves for all pieces on the board
     * It accepts a json object with the field "fen"
     * It returns a json object with the fields being the tiles containing a piece that has any legal moves
     * The value of each field is an array of tiles the piece on the given tile can move to
     */
    "respond with a dictionary of legal moves for a Post request with a FEN on route /compute/all" in {
      fen = "8/8/8/8/8/8/3r4/R3K2R w KQ - 0 1"
      Post("/compute", s"""{"fen":"$fen"}""") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[JsValue] shouldEqual getLegalMoves(fen).toJson
      }
    }
    /*
     * Test for error handling for FEN on /compute/tile and /compute/all
     * 
     * Uses util.FenParser.checkFen(fen) to check if the given FEN is valid 
     * and tries to instantiate a ChessState
     * 
     * It accepts a json object with the fields "fen"
     * It returns a BadRequest if the FEN is invalid
     */
    "return a BadRequest for a Post request with an invalid FEN" in {
      // Route is sealed to ignore ErrorHandling
      fen = "8/8/8/8/8/8/8/8"
      Post("/compute?tile=\"A1\"", s"""{"fen":"$fen"}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
        responseAs[String] shouldEqual s"""Invalid fen: "$fen""""
      }
      Post("/compute", s"""{"fen":"$fen"}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
        responseAs[String] shouldEqual s"""Invalid fen: "$fen""""
      }

      // Wrong or missing field name in json object
      Post("/compute?tile=\"A1\"", s"""{"fenString":"$fen"}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
        responseAs[String] shouldEqual s"""Missing fields in body: "fen""""
      }
      Post("/compute", s"""{"fenString":"$fen"}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
        responseAs[String] shouldEqual s"""Missing fields in body: "fen""""
      }
      Post("/compute?tile=\"A1\"", s"""{}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
        responseAs[String] shouldEqual s"""Missing fields in body: "fen""""
      }
      Post("/compute", s"""{"other":"blah"}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
        responseAs[String] shouldEqual s"""Missing fields in body: "fen""""
      }
    }
    /*
     * Test for error handling for tiles on /compute/tile
     * 
     * Tries to instantiate a Tile to check if the given tile is valid
     * 
     * It accepts a json object with the fields "tile"
     * It returns a BadRequest if the tile is invalid
     */
    "return a BadRequest for a Post request with an invalid tile" in {
      // Route is sealed to ignore ErrorHandling
      fen = "8/8/8/8/8/8/8/8 w KQ - 0 1"
      Post("/compute?tile=\"A0\"", s"""{"fen":"$fen"}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
        responseAs[String] shouldEqual "The query parameter 'tile' was malformed:\nassertion failed: Invalid rank 0"
      }
      Post("/compute?tile=\"J4\"", s"""{"fen":"$fen"}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
        responseAs[String] shouldEqual "The query parameter 'tile' was malformed:\nassertion failed: Invalid file 10"
      }

      // Wrong query parameter name (is ignored and runs as if no tile was given)
      Post("/compute?piece=\"A1\"", s"""{"fen":"$fen"}""") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[JsValue] shouldEqual getLegalMoves(fen).toJson
      }
    }
  }
