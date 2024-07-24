import DataBaseRw._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Test extends App {

  val test =
    getGamesByGenres(Seq("Yes"))
//    Connection.db.run(gamesQuery.schema.createIfNotExists)
//      .flatMap(x => Connection.db.run(individualsQuery.schema.createIfNotExists))
//      .flatMap(x => Connection.db.run(reviewsQuery.schema.createIfNotExists))
//      .flatMap(x => Connection.db.run(genresQuery.schema.createIfNotExists))

  test.onComplete {
    case Success(res) =>
      println(res)
    case Failure(res) =>
      println(res)
  }

//  db.run(gamesQuery.filter(_.steamId === steamGame.appid).result.headOption)
}
