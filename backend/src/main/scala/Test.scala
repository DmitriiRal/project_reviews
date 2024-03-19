import DataBaseRw.getTopFive

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Test extends App {

  getTopFive("Counter").onComplete {
    case Success(res) =>
      println(res)
    case Failure(res) =>
      println(res)
  }

}
