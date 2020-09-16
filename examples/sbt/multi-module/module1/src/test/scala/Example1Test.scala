import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Example1Test extends AnyFlatSpec with Matchers {
  "Example1" should "sum two numbers" in {
    Example1.sum(1, 5) shouldBe 6
  }
}
