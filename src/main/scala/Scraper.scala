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
import slick.ast.Library.Database // if you don't supply your own Protocol (see below)



object Scraper extends App {

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val getApps: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://api.steampowered.com/ISteamApps/GetAppList/v2/"))

  val timeout = 1000.millis
  val apps = getApps.flatMap(res => res.entity.toStrict(timeout))
    // read body
    .map(x => x.data.utf8String.parseJson.asJsObject.fields("applist").asJsObject.fields("apps"))
    .map {
      case JsArray(vector) => vector.map(x =>
        x.asJsObject.getFields("appid", "name") match {
          case Seq(JsNumber(number), JsString(string)) => Games(0, string, number.toLong)
          case _ => throw new Exception("Wrong formats")
        }
      )
      case _ => throw new Exception("Vector expected")
    }

  val addToDb = apps.flatMap { x =>
    db.run(gamesQuery ++= x)
  }

  def descendingOrder(num: Int): Int = num.toString.sorted.reverse.toInt


  addToDb.onComplete {
    case Success(res) =>
      println(res)
    case Failure(res) =>
      println(res)
  }
}



//case class SteamAppShortForm(appId: BigDecimal, gameName: String)

//object MyJsonProtocol {
//  def read(value: JsValue) =
//    value.asJsObject.getFields("appid", "name") match
//      case Seq(JsNumber(number), JsString(string)) => SteamAppShortForm(number, string)
//}
