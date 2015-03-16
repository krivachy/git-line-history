package org.ucl.compgs04.scm.git

import org.scalatest.FlatSpec
import org.ucl.compgs04.GitLineHistory


class GitScmTest extends FlatSpec {
  val scm = new GitScm
  "Git Scm" should "run diff correctly" in {
    val fileName = Seq("file_name") // TODO add the actual file name

    // TODO hard-code revision short hashes
    val revision1 = "short_hash_1"
    val revision2 = "short_hash_2"
    val revision3 = "short_hash_3"
    val revision4 = "short_hash_4"
    val revision5 = "short_hash_5"

    val output = Seq(
      s"$revision1 $revision4: 1 changed",
      s"$revision1 $revision4: 2 changed",
      s"$revision1 $revision4: 3 changed",
      s"$revision1 $revision3 $revision4 $revision5: new 1",
      s"$revision1 $revision3 $revision4 $revision5: new 2",
      s"$revision1 $revision3 $revision4 $revision5: new 3",
      s"$revision1 $revision3 $revision4 $revision5: new 4",
      s"$revision1 $revision3 $revision4 $revision5: new 5",
      s"$revision1 $revision3 $revision4 $revision5: new 6",
      s"$revision1 $revision3 $revision4 $revision5: new 7",
      s"$revision1 $revision3 $revision4 $revision5: new 8",
      s"$revision1 $revision3: 8 changed",
      s"$revision1 $revision3: 9 changed",
      s"$revision1 $revision3: 10 changed",
      s"$revision2: 11"
    )

    val commandLineOutput = output.mkString("\n")

    val actualOutput = GitLineHistory.process(fileName.toArray)
    assert(actualOutput == commandLineOutput)
  }
}
