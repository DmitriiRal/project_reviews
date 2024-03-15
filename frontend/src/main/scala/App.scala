import com.raquo.laminar.api.{*, given}
import com.raquo.laminar.api.L.*
import org.scalajs.dom
object App {

  val app: Element =
    div(h1("Sosi"))

  def main(args: Array[String]): Unit =
    render(dom.document.getElementById("app"), app)
}
