package org.ucl.compgs04.scm.git

import java.nio.file.{Files, Paths}

import org.apache.commons.io.FileUtils
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.ucl.compgs04.model.ShortHash
import org.ucl.compgs04.output.CommandLineOutput
import org.ucl.compgs04.util.UnzipUtility


class GitScmTest extends FlatSpec with MockitoSugar with Matchers {

  val mockOperations = mock[GitOperations]
  val scm = new GitScm(mockOperations)

  "Git Scm" should "run diff correctly" in {

    val repoLocations = Paths.get("src/test/resources/repositories/")
    val extractedDirLocation = repoLocations.resolve("compgs04-example-repo")
    if(Files.exists(extractedDirLocation)) {
      FileUtils.deleteDirectory(extractedDirLocation.toFile)
    }
    val fullZipPath = repoLocations.resolve("compgs04-example-repo.zip").toFile.getAbsolutePath

    UnzipUtility.unzip(fullZipPath, repoLocations.toFile.getAbsolutePath)

    val fileName = repoLocations.resolve("compgs04-example-repo").resolve("compgs04-example.txt").toFile.getAbsolutePath

    val revision1 = ShortHash("4ea87e456809b26dbf532e76396fbe1741b2e7d4").getShort()
    val revision2 = ShortHash("d6d5ce171c99796738c60c97b12c9c3008a903c0").getShort()
    val revision3 = ShortHash("8be8a10bd84a2cd6ceb97c6b037c81aba7782bd4").getShort()
    val revision4 = ShortHash("1b8062f7e37bd4ef704e01de3d6cce290aeda815").getShort()
    val revision5 = ShortHash("3a889f11eb08ea765b7bb85485ed4c6c82509e79").getShort()

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

    val expectedOutput = output.mkString(System.getProperty("line.separator"))

    val result = new GitScm(RealGitOperations).historyForFile(fileName)

    // Mutable: we will write into this variable later
    var consoleOutput = ""
    new CommandLineOutput(s => { consoleOutput = s }).processToOutput(result)

    assert(consoleOutput.trim == expectedOutput)
  }

}