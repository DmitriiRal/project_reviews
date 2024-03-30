import DataBaseRw.{Games, GamesTable, getGames}
import GamesController.getGameById
import org.apache.pekko
import pekko.actor.typed.ActorSystem
import pekko.actor.typed.scaladsl.Behaviors
import pekko.http.scaladsl.Http
import pekko.http.scaladsl.model._
import pekko.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import scala.util.{Failure, Success}
import spray.json._


object RequestHandler extends App {

  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext = system.executionContext
  implicit val gameInfoFormat: RootJsonFormat[GameInfo] = jsonFormat5(GameInfo)
  implicit val gamesTableFormat: RootJsonFormat[Games] = jsonFormat4(Games)
  implicit def paginatedResultFormat[T](implicit writeForT: JsonWriter[T]): RootJsonWriter[PaginatedResult[T]] = (obj: PaginatedResult[T]) =>
    JsObject(
      "entities" -> JsArray(obj.entities.map(_.toJson).toVector),
      "Total_count" -> JsNumber(obj.totalCount)
    )



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
                  headers = Seq(headers.`Access-Control-Allow-Origin`.*),
                  entity =
                    HttpEntity(
                      ContentTypes.`application/json`,
                      result.toJson.prettyPrint
                    )
                )
              )
            case None => complete(StatusCodes.NotFound)
          }
          case Failure(res) =>
            complete(StatusCodes.Conflict)
        }
      }
    },
    get {
      path("games") {
        parameters("searchString", "offset".as[Int], "limit".as[Int]) { (string, offset, limit) =>
          onComplete(getGames(string, offset, limit)) {
            case Success(res) =>
              complete(
                HttpResponse(
                  headers = Seq(headers.`Access-Control-Allow-Origin`.*),
                  entity =
                    HttpEntity(
                      ContentTypes.`application/json`,
                      res.toJson.prettyPrint
                    )
                )
              )
            case Failure(res2) =>
              complete((StatusCodes.Conflict, s"An error occurred: $res2"))
          }
        }
      }
    }

  )

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
