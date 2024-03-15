import DataBaseRw.{gamesQuery, getGame}
import GamesController.getGameById
import org.apache.pekko
import pekko.actor.typed.ActorSystem
import pekko.actor.typed.scaladsl.Behaviors
import pekko.http.scaladsl.Http
import pekko.http.scaladsl.model._
import pekko.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol.{IntJsonFormat, JsValueFormat, StringJsonFormat, listFormat, mapFormat}

import scala.io.StdIn
import scala.util.{Failure, Success}
import spray.json._


object RequestHandler extends App {

  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  val route = concat(
    get {
      path("hello") {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to Pekko HTTP</h1>"))
      }
    },
    get {
      path("games" / IntNumber) { int =>
        onComplete(getGameById(int)) {
          case Success(res) => res match {
            case Some(result) =>
              complete(
                HttpResponse(
//                  headers = Seq(headers.`Content-Type`(ContentTypes.`application/json`)),
                  entity =
                    HttpEntity(
                      ContentTypes.`application/json`,
                      JsObject(
                        "game_id" -> JsNumber(result.id),
                        "game_name" -> JsString(result.name),
                        "description" -> JsString(result.description)
                      ).prettyPrint
                    )
                )
              )
            case None => complete(StatusCodes.NotFound)
          }
          case Failure(res) =>
            complete(StatusCodes.Conflict)
        }
      }
    }


    //                StatusCodes.OK,
    //                JsObject(
    //                  "game_id" -> JsNumber(result.id),
    //                  "game_name" -> JsString(result.name),
    //                  "description" -> JsString(result.description)
    //                ).prettyPrint


//    get {
//      path("games") {
//        parameters("id1") { (id1) =>
//          onComplete(getGame(id1.toInt)) {
//            case Success(res) =>
//              complete(s"Found a game $res")
//            case Failure(res2) =>
//              complete((StatusCodes.Conflict, s"An error occurred: $res2"))
//          }
//        }
//      }
//    }




  )

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
