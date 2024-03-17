import DataBaseRw.{executionContext, getGame, system}
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import spray.json._

import scala.concurrent.Future

object GamesController extends App {
  def getGameById(id: Int): Future[Option[GameInfo]] = {
    getGame(id).flatMap {
      case Some(value) =>
        Http().singleRequest(HttpRequest(uri = s"https://store.steampowered.com/api/appdetails?appids=${value.steamId}"))
        .flatMap(res => res.entity.toStrict(1000.millis))
          .map(x =>
            x.data.utf8String.parseJson.asJsObject.fields(s"${value.steamId}")
            .asJsObject.fields("data").asJsObject.getFields("name", "detailed_description") match {
              case Seq(JsString(name), JsString(description)) => Some(GameInfo(name, id, description))
              case _ => throw new Exception("Not found")
            }
          )
      case None => Future.successful(None)
    }
  }

//  def getTopFiveSearch(str: String): Future[Option[GameInfo]] = {
//
//  }

//  val a = Http().singleRequest(HttpRequest(uri = s"https://store.steampowered.com/api/appdetails?appids=${value.steamId}"))
//    .flatMap(res => res.entity.toStrict(1000.millis))
//    .map(x =>
//      x.data.utf8String.parseJson.asJsObject.fields(s"${66}")
//        .asJsObject.fields("data").asJsObject.getFields("name", "detailed_description")
}



//  def getGameById(id: Int): Future[Option[GameInfo]] = {
//    getGame(id).flatMap {
//      case Some(value) =>
//        Http().singleRequest(HttpRequest(uri = s"https://store.steampowered.com/api/appdetails?appids=${value.steamId}"))
//          .flatMap(res => res.entity.toStrict(1000.millis))
//          .map(x =>
//            x.data.utf8String.parseJson.asJsObject.fields(s"${value.steamId}")
//              .asJsObject.fields("data").asJsObject.getFields("name", "detailed_description") match {
//              case Seq(JsString(name), JsString(description)) => Some(GameInfo(name, id, description))
//              case _ => throw new Exception("Бом Бом")
//            }
//          )
//      case None => Future.successful(None)
//    }
//  }


case class GameInfo(
                   name: String,
                   id: Int,
                   description: String
                   )
