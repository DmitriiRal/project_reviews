import slick.jdbc.PostgresProfile.api._
import scala.util.{Failure, Success}
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
                          steamId: Long
                        )
  class GamesTable(tag: Tag) extends Table[Games](tag, Some("db_reviews"), "games") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def steamId = column[Long]("steam_id")
    override def * = (id, name, steamId) <> (Games.tupled, Games.unapply)
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

  def getTopFiveGames(game: String): Future[Seq[Games]] = {
    val query = gamesQuery.filter(w => w.name.like(s"$game%")).take(5).result
    val run = Connection.db.run(query)
    run
  }

}

//  val query = gamesQuery.filter(w => w.id === 88.toLong).result
//  val run = Connection.db.run(query)
//  getGame(88).onComplete {
//    case Success(res) =>
//      println(res)
//    case Failure(res) =>
//      println(res)
//  }

//  val query = gamesQuery.filter(w => w.id === 88L).result
//  val run2 = Connection.db.run(query)

//  run2.onComplete {
//    case Success(res) =>
//      println(res)
//    case Failure(res) =>
//      println(res)
//  }


//  val addGame = gamesQuery ++= Vector(Games(0, "Deep Rock Galactic", 10), Games(0, "Deep Rock Galactic", 10))
//  val query = gamesQuery.filter(w => w.id === 0L).result
//  val run = addGame.flatMap(_ => query)


//  try {
//    // val resultFuture: Future[_] = { ... }
//    val n = Await.result(db.run(run), Duration.Inf)
//    println(n)
//  } finally db.close

//  val plainQuery = sql"select name from db_reviews.games".as[String]


//  val q = for (c <- coffees) yield c.name
//  val a = q.result
//  val f: Future[Seq[String]] = db.run(a)
//  f.onComplete {
//    case Success(s) =>
//      println(s"Result: $s")
//    case Failure(t) =>
//      t.printStackTrace()
//  }

