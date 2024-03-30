import slick.jdbc.PostgresProfile.api._
import java.time.LocalDate
import org.apache.pekko
import pekko.actor.typed.ActorSystem
import pekko.actor.typed.scaladsl.Behaviors
import scala.concurrent.{ExecutionContextExecutor, Future}


object Connection {
  val db = Database.forConfig("mydb")
}

object DataBaseRw {

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  final case class Games(
                          id: Long,
                          name: String,
                          steamId: Long,
                          capsuleImageV5: String
                        )
  class GamesTable(tag: Tag) extends Table[Games](tag, Some("db_reviews"), "games") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def steamId = column[Long]("steam_id")
    def capsuleImageV5 = column[String]("capsule_imagev5")
    override def * = (id, name, steamId, capsuleImageV5) <> (Games.tupled, Games.unapply)
  }
  val gamesQuery = TableQuery[GamesTable]


  final case class Individuals(
                                id: Long,
                                name: String,
                                birthday: LocalDate
                              )
  class IndividualsTable(tag: Tag) extends Table[Individuals](tag, Some("db_reviews"), "individuals") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def birthday = column[LocalDate]("birthday")
    override def * = (id, name, birthday) <> (Individuals.tupled, Individuals.unapply)
  }
  val individualsQuery = TableQuery[IndividualsTable]


  final case class Reviews(
                            id: Long,
                            text: String,
                            personID: Long,
                            gameID: Long,
                            score: Short,
                            createdAt: LocalDate
                          )
  class ReviewsTable(tag: Tag) extends Table[Reviews](tag, Some("db_reviews"), "reviews") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text")
    def personId = column[Long]("person_id")
    def gameId = column[Long]("game_id")
    def score = column[Short]("score")
    def createdAt = column[LocalDate]("created_at")
    override def * = (id, text, personId, gameId, score, createdAt) <> (Reviews.tupled, Reviews.unapply)
  }
  val reviewsQuery = TableQuery[ReviewsTable]

  def getGame(gameId: Int): Future[Option[Games]] = {
    val query = gamesQuery.filter(w => w.id === gameId.toLong).result.headOption
    val run = Connection.db.run(query)
    run
  }

  def getTenGames(game: String): Future[Seq[Games]] = {
    val query = gamesQuery.filter(w => w.name.like(s"$game%")).take(10).result
    Connection.db.run(query)
  }

  def getGames2(game: String, offset: Int, limit: Int): Future[Seq[Games]] = {
    val query = gamesQuery.filter(w => w.name.like(s"$game%")).drop(offset).take(limit).result
    Connection.db.run(query)
  }

  def getGames(game: String, offset: Int, limit: Int): Future[PaginatedResult[GamesTable#TableElementType]] = Connection.db.run {
    for {
      gameList <- gamesQuery.filter(_.name.like(s"$game%")).drop(offset).take(limit).result
      numberOfGamesFound <- gamesQuery.filter(_.name.like(s"$game%")).length.result
    } yield PaginatedResult(gameList, numberOfGamesFound)
  }

}