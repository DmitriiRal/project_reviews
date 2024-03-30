import DataBaseRw.{executionContext, getGame}
import Steam.SteamApi.getSteamAppDetails

import scala.concurrent.Future

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

  def searchGames: Future[Seq[GameInfo]] = ???

//  def searchGames(game: String, offset: Int, limit: Int) =

}



