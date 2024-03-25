package Steam

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.HttpRequest
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import spray.json._

object SteamApi {

  def steamAppDetailFormat(steamId: Long): RootJsonReader[Option[SteamAppDetails]] =
    (value: JsValue) => {
      val jsObject = value.asJsObject.fields(s"$steamId").asJsObject
      jsObject.getFields("success") match {
        case Seq(JsBoolean(true)) =>
          jsObject.fields("data").asJsObject
            .getFields("name", "detailed_description", "header_image") match {
            case Seq(JsString(name), JsString(description), JsString(picture)) =>
              Some(SteamAppDetails(name, description, picture, steamId,
                s"https://store.steampowered.com/api/appdetails?appids=$steamId"))
            case _ => throw new Exception("Wrong fields")
          }
        case _ => None
      }
    }

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  def getSteamAppDetails(steamId: Long): Future[Option[SteamAppDetails]] = {
    Http().singleRequest(HttpRequest(uri =
        s"https://store.steampowered.com/api/appdetails?appids=$steamId"))
      .flatMap(res => res.entity.toStrict(1000.millis))
      .map(x =>
        x.data.utf8String.parseJson.convertTo(steamAppDetailFormat(steamId))
      )
  }


//  def getSteamAppDetails2(steamId: Long): Future[Option[SteamAppDetail]] =
//    Http().singleRequest(HttpRequest(uri =
//        s"https://store.steampowered.com/api/appdetails?appids=$steamId"))
//      .flatMap(res => res.entity.toStrict(1000.millis))
//      .map(x =>
//        x.data.utf8String.parseJson.convertTo
//      )


  def temporaryJsonParser(steamId: Long) =
    Http().singleRequest(HttpRequest(uri =
        s"https://store.steampowered.com/api/appdetails?appids=$steamId"))
      .flatMap(res => res.entity.toStrict(1000.millis))
      .map(x =>
        x.data.utf8String.parseJson.asJsObject.fields(s"$steamId")
          .asJsObject.fields("data").asJsObject)
}
