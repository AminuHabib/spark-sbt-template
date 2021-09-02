import org.scalatest.FunSuite

class AppTest extends FunSuite {

  val testLog: List[String] =
    List("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

  test("test if the first element of the list is assigned to IP") {
    assert(testLog.head === AccessJob.toAccessLog(testLog).ip)
  }

  test("Accessing invalid index should throw IndexOutOfBoundsException") {
    assertThrows[IndexOutOfBoundsException] {
      testLog(11)
    }
  }
}
