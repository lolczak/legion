package legion.data

import org.scalatest.flatspec.AnyFlatSpec
import Path._
import org.scalatest.matchers.must.Matchers

class PathSpec extends AnyFlatSpec with Matchers {

  "A path" should "be composed of segments" in {
    root.toString mustBe "/"
    (root / "baz.txt").toString mustBe "/baz.txt"
    (root / "foo" / "bar" / "baz.txt").toString mustBe "/foo/bar/baz.txt"
  }

}
