import Connection.db
import DataBaseRw.{Games, gamesQuery}
import org.apache.pekko
import pekko.actor.typed.ActorSystem
import pekko.actor.typed.scaladsl.Behaviors
import pekko.http.scaladsl.Http
import pekko.http.scaladsl.model._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import spray.json._


object Scraper extends App {

  implicit object GamesFormat extends RootJsonFormat[Vector[Games]] {
    def read(json: JsValue): Vector[Games] =
      json.asJsObject.fields("applist").asJsObject.fields("apps") match {
      case JsArray(vector) => vector.map(_.asJsObject.getFields("appid", "name") match {
          case Seq(JsNumber(number), JsString(string)) => Games(0, string, number.toLong, "")
          case _ => throw new Exception("Wrong formats")
        }
      )
      case _ => throw new Exception("Vector expected")
    }
    def write(obj: Vector[Games]): JsValue = throw new Exception("Writing is impossible")
  }


  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val getApps: Future[HttpResponse] = Http().singleRequest(
    HttpRequest(uri = "https://api.steampowered.com/ISteamApps/GetAppList/v2/")
  )
  val timeout = 1000.millis
  val apps = getApps.flatMap(res => res.entity.toStrict(timeout))
    .map(x => x.data.utf8String.parseJson.convertTo[Vector[Games]])
  val addToDb = apps.flatMap { x =>
    db.run(gamesQuery ++= x)
  }


  val addToDb2 = apps.flatMap { vector =>
    Future.sequence(
      vector.map(
        oneApp => {
          db.run(gamesQuery.filter(_.steamId === oneApp.steamId).result.headOption).flatMap {
            case Some(res) =>
              db.run(gamesQuery.filter(_.steamId === oneApp.steamId).map(_.name).update(oneApp.name))
            case None =>
              db.run(gamesQuery += oneApp)
          }
        }
      )
    )
  }


  addToDb2.onComplete {
    case Success(res) =>
      println(res)
    case Failure(res) =>
      println(res)
  }
}





object TestDb extends App {
  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val test = db.run(gamesQuery.filter(_.steamId === 19034806546L).result.headOption)

  test.onComplete {
    case Success(res) =>
      println(res)
    case Failure(res) =>
      println(res)
  }
}


//  def descendingOrder(num: Int): Int = num.toString.sorted.reverse.toInt
