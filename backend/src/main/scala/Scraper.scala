import Connection.db
import DataBaseRw.{Games, gamesQuery, Genres, genresQuery}
import Steam.SteamApi.getSteamAppDetails
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

  case class SteamGame(appid: Long, name: String)

  implicit object SteamGameFormat extends RootJsonReader[Vector[SteamGame]] {
    def read(json: JsValue): Vector[SteamGame] =
      json.asJsObject.fields("applist").asJsObject.fields("apps") match {
      case JsArray(vector) => vector.map(_.asJsObject.getFields("appid", "name") match {
          case Seq(JsNumber(appId), JsString(name)) => SteamGame(appId.toLong, name)
          case _ => throw new Exception("Wrong formats")
        }
      )
      case _ => throw new Exception("Vector expected")
    }
  }

  private val timeout = 60000.millis

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val getApps: Future[HttpResponse] = Http().singleRequest(
    HttpRequest(uri = "https://api.steampowered.com/ISteamApps/GetAppList/v2/")
  )

  val apps = getApps.flatMap(res => res.entity.toStrict(timeout))
    .map(x => x.data.utf8String.parseJson.convertTo[Vector[SteamGame]])

  val addToDb2 = apps.flatMap(processSeq1)

  def processSeq1(vector: Vector[SteamGame]): Future[Int] = {
    def inner(acc: Int, tail: Vector[SteamGame]): Future[Int] = tail match {
      case Vector() => Future.successful(acc)
      case head +: tail => processItem(head).flatMap(_ => inner(acc + 1, tail))
    }

    inner(0, vector)
  }

  def processItem(steamGame: SteamGame): Future[Unit] = {
    println(s"Processing steam game(${steamGame.appid}) ${steamGame.name}")
    db.run(gamesQuery.filter(_.steamId === steamGame.appid).result.headOption).flatMap {
      case Some(_) =>
        println(s"Found a game, updating")
        Future.successful(())
      case None =>
        println(s"Not found, creating")

        for {
          gameDetails <- getSteamAppDetails(steamGame.appid)
          gameIdInDb <- gameDetails match {
            case Some(appDetails) => db.run {
              gamesQuery returning gamesQuery.map(_.id) += Games(
                id = 0L,
                name = steamGame.name,
                steamId = appDetails.steamId,
                capsuleImageV5 = appDetails.headerImage
              )
            }
            case None => Future.successful(0L)
          }
          gameAdded <- gameDetails match {
            case Some(appDetails) => db.run {
              genresQuery ++= appDetails.genres.map(listGenres =>
                Genres(
                  id = 0L,
                  gameID = gameIdInDb,
                  genre = listGenres
                )
              )
            }
            case None => Future.successful(0L)
          }
        } yield ()
    }
  }

  addToDb2.onComplete {
    case Success(res) =>
      println(res)
    case Failure(res) =>
      println(res)
  }
}

