package Steam

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.HttpRequest
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import spray.json._

object SteamApi {

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  def getSteamData(steamId: Long): Future[Option[SteamData]] =
    Http().singleRequest(HttpRequest(uri =
        s"https://store.steampowered.com/api/appdetails?appids=$steamId"))
      .flatMap(res => res.entity.toStrict(1000.millis))
      .map(x =>
        x.data.utf8String.parseJson.asJsObject.fields(s"$steamId")
          .asJsObject.fields("data").asJsObject
          .getFields("name", "detailed_description", "header_image") match {
          case Seq(JsString(name), JsString(description), JsString(picture)) =>
            Some(SteamData(name, description, picture, steamId,
              s"https://store.steampowered.com/api/appdetails?appids=$steamId"))
          case _ => None
        }
      )


  def temporaryJsonParser(steamId: Long) =
    Http().singleRequest(HttpRequest(uri =
        s"https://store.steampowered.com/api/appdetails?appids=$steamId"))
      .flatMap(res => res.entity.toStrict(1000.millis))
      .map(x =>
        x.data.utf8String.parseJson.asJsObject.fields(s"$steamId")
          .asJsObject.fields("data").asJsObject)
}
