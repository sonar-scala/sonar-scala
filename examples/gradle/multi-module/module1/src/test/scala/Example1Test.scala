import org.scalatest._

class Example1Test extends FlatSpec with Matchers {
  "Example1" should "sum two numbers" in {
    Example1.sum(1, 5) shouldBe 6
  }
}
