import com.raquo.laminar.api.L.*
import org.scalajs.dom
object App {

  val gameId: Var[String] = Var("")
  val response: Var[String] = Var("")

  val app: Element =
    div(
      "Put game id: ",
      input(
        value <-- gameId,
        onInput.mapToValue --> gameId.writer
      ),
      button(
        "Search",
        onClick.flatMap { _ =>
          FetchStream.get(s"http://localhost:8080/games/${gameId.now()}")
        } --> (r => response.set(r))
      ),
      div(
        child.text <-- response
      )
    )

  def main(args: Array[String]): Unit =
    render(dom.document.getElementById("app"), app)
}
