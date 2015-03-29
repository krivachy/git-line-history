package org.ucl.compgs04.scm.git

import org.scalatest.{FlatSpec, Matchers}


class DiffParserTest extends FlatSpec with Matchers with DiffParsingLogic {

  val header = """diff --git a/file1.scala b/file2.scala
                 |index 220ecd4..08abdb8 100644
                 |--- a/file1.scala
                 |+++ b/file2.scala
                 |""".stripMargin
  val chunkInfo =
    """@@ -10,53 +10,55 @@
      |""".stripMargin
  val lineData = """  */
                   | a
                   | some code;
                   |-  a
                   |-  a
                   |-  a
                   |-  a
                   |-  a
                   |-  a
                   |-  a
                   |+  b
                   |+  b
                   |+  b
                   |+  b
                   |+  b
                   |+  b
                   |+  b
                   | a
                   |""".stripMargin

  "Diff parsing" should "parse headerlines correctly" in {
    val res = parseAll(headerLines, header)
    res shouldBe a[Success[_]]
    //res.get should have size 5
  }

  it should "parse the chunk info" in {
    val res = parseAll(chunkStart, chunkInfo)
    res shouldBe a[Success[_]]
    res.get should be((10, 10))
  }

  it should "parse the header + chunk correctly" in {
    val res = parseAll(chunkHeader, header + chunkInfo)
    res shouldBe a[Success[_]]
    res.get should be((10, 10))
  }

}
