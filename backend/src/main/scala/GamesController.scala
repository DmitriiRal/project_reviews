import DataBaseRw.{executionContext, getGame, getTopFive, system}
import Steam.SteamApi.{getSteamAppDetails, temporaryJsonParser}
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.HttpRequest

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import spray.json._

object GamesController extends App {

  def getGameById(id: Int): Future[Option[GameInfo]] = {
    getGame(id).flatMap {
      case Some(db) =>
        getSteamAppDetails(db.steamId).map {
          case Some(steamData) =>
            Some(
              GameInfo(
                db.name,
                db.id,
                steamData.detailedDescription,
                steamData.steamLink,
                steamData.headerImage
              )
            )
          case _ => None
        }
      case None => Future.successful(None)
    }.recover {
      case e: Exception =>
        println(e)
        throw e
    }
  }


  def getSmallPicture(steamId: Long): Future[String] = {
    temporaryJsonParser(steamId).map(x =>
      x.getFields("capsule_imagev5") match {
        case Seq(JsString(picture)) =>
          picture
        case _ => throw new Exception("Not found")
      }
    )
  }

  // Future[Seq[Future[GetTop]]] -> Future[Future[Seq[GetTop]]]
  // Seq[Future[_]] -> Future[Seq[_]]
  def getTopFiveGames(string: String): Future[Seq[GetTop]] = {
    getTopFive(string).flatMap { seq =>
      Future.sequence(
        seq.map(elem =>
          getSmallPicture(elem.steamId).map { pictureStr =>
            GetTop(
              elem.name,
              elem.id,
              elem.steamId,
              pictureStr
            )
          }
        )
      )
    }
  }


}

case class GameInfoOld(
                     name: String,
                     id: Int,
                     description: String,
                     steamLink: String,
                     gamePicture: String
                   )

case class GetTop(
                   name: String,
                   id: Long,
                   steamId: Long,
                   gamePicture: String
                 )




// Future[Option[GameInfo]]
//  def getGameById(id: Int): Future[Option[GameInfoOld]] = {
//    getGame(id).flatMap {
//      case Some(value) =>
//        Http().singleRequest(HttpRequest(uri = s"https://store.steampowered.com/api/appdetails?appids=${value.steamId}"))
//          .flatMap(res => res.entity.toStrict(1000.millis))
//          .map(x =>
//            x.data.utf8String.parseJson.asJsObject.fields(s"${value.steamId}")
//              .asJsObject.fields("data").asJsObject.getFields("name", "detailed_description", "header_image") match {
//              case Seq(JsString(name), JsString(description), JsString(picture)) =>
//                Some(GameInfoOld(name, id, description, picture, s"https://store.steampowered.com/api/appdetails?appids=${value.steamId}"))
//              case _ => throw new Exception("Not found")
//            }
//          )
//      case None => Future.successful(None)
//    }
//  }


