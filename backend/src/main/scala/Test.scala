import DataBaseRw.getTopFiveGames

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Test extends App {

  getTopFiveGames("Counter").onComplete {
    case Success(res) =>
      println(res)
    case Failure(res) =>
      println(res)
  }

}
