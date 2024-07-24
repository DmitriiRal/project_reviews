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

          def getGenres(vector: Vector[JsValue], list: List[String]): List[String] = { vector match {
            case empty if vector.isEmpty => list
            case head +: tail =>
              val genre = head.asJsObject.getFields("description") match {
                case Seq(JsString(oneGenre)) => oneGenre
                }
              getGenres(tail, genre :: list)
          }}

          def createClass(name: String, descr: String, pic: String, genre: Option[Vector[JsValue]]): Some[SteamAppDetails] = {
            Some(
              SteamAppDetails(
                name,
                descr,
                pic,
                steamId,
                s"https://store.steampowered.com/api/appdetails?appids=$steamId",
                if (genre.isEmpty) Nil else getGenres(genre.head, Nil)
              )
            )
          }

          jsObject.fields("data").asJsObject
            .getFields(
              "name",
              "detailed_description",
              "header_image",
              "genres"
            ) match {
            case Seq(
              JsString(name),
              JsString(description),
              JsString(picture),
              JsArray(genres)
            ) =>
              createClass(name, description, picture, Some(genres))
            case Seq(
            JsString(name),
            JsString(description),
            JsString(picture)
            ) =>
              println("This game has no genre")
              createClass(name, description, picture, None)
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


}
