import org.scalatest._

class ExampleTest extends FlatSpec with Matchers {
  "Example" should "sum two numbers" in {
    Example.sum(1, 5) shouldBe 6
  }
}
