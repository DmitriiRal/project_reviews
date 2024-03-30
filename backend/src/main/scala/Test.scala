import DataBaseRw.{getTenGames, getGames}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Test extends App {

//  getTenGames("Counter").onComplete {
//    case Success(res) =>
//      println(res)
//    case Failure(res) =>
//      println(res)
//  }

  getGames("A",0, 10).onComplete {
    case Success(res) =>
      println(res)
    case Failure(res) =>
      println(res)
  }

}
