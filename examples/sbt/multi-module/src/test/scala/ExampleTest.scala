import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExampleTest extends AnyFlatSpec with Matchers {
  "Example" should "sum two numbers" in {
    Example.sum(1, 5) shouldBe 6
  }
}
