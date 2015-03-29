package org.ucl.compgs04.scm.git

import java.io.{InputStreamReader, BufferedReader, InputStream, File}

import org.scalatest.FlatSpec
import org.ucl.compgs04.GitLineHistory
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.ucl.compgs04.model.ShortHash
import java.util.zip.{ZipEntry, ZipFile}


class GitScmTest extends FlatSpec with MockitoSugar with Matchers {
  val mockOperations = mock[GitOperations]
  val scm = new GitScm(mockOperations)

  "Git Scm" should "run diff correctly" in {
    /* TODO delete if tests pass with folder instead of zip file
    val rootZip = new ZipFile("src/test/resources/repositories/compgs04-example-repo.zip")
    val file = rootZip.getEntry("compgs04-example-repo/compgs04-example.txt")
    val inputStream = rootZip.getInputStream(file)
    val reader: BufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
    var data = reader.readLine()
    while(data != null) {
      println(data)
      data = reader.readLine()
    }*/

    val fileName = Seq("src/test/resources/repositories/compgs04-example-repo/compgs04-example.txt") // TODO add the actual file name

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

    val commandLineOutput = output.mkString("\n")

    val actualOutput = GitLineHistory.process(fileName.toArray)
    assert(actualOutput == commandLineOutput)
  }

  it should "identify the diffs correctly" in {
    val hashA = "abc"
    val hashB = "def"
    val fileName = "file"
    val diffResult = """diff --git a/Dockerfile b/Dockerfile
                       |index 87da1bd..e40d7db 100644
                       |--- a/Dockerfile
                       |+++ b/Dockerfile
                       |@@ -1,7 +1,3 @@
                       |-asdf
                       |-asfsdgsdg
                       |-dsagjldsghl
                       |-asdfj;ladsf
                       | FROM node2
                       | MAINTAINER rc@inocr8.co
                       |""".stripMargin
    val logResult = """commit 1716c35fad8757a347a77b7fbd45f90f8c9a247e
                      |Author: Akos Krivachy <ak@inocr8.co>
                      |Date:   Mon Mar 2 14:35:33 2015 +0000
                      |
                      |    change1
                      |""".stripMargin
    val file = """asdf
                 |asfsdgsdg
                 |dsagjldsghl
                 |asdfj;ladsf
                 |FROM node2
                 |MAINTAINER rc@inocr8.co
                 |
                 |WORKDIR /src
                 |
                 |COPY zephyrguard.py /src/zephyrguard.py
                 |
                 |# replace this with your application's default port
                 |EXPOSE 1337
                 |
                 |ENV NODE_ENV production
                 |ENV ZEPHYR_LOG_FILES_ENABLED true
                 |ENV ZEPHYR_LOG_INFO_FILE /zephyr-info.log
                 |ENV ZEPHYR_LOG_DEBUG_FILE /zephyr-debug.log
                 |ENV ZEPHYR_LOG_ERROR_FILE /zephyr-error.log
                 |
                 |RUN apt-get update && apt-get -y install supervisor
                 |COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
                 |
                 |ADD sails/ /src
                 |
                 |# install your application's dependencies
                 |RUN npm install
                 |
                 |# log.io
                 |RUN npm install -g log.io --user "root"
                 |ADD harvester.conf /root/.log.io/harvester.conf
                 |
                 |CMD ["/usr/bin/supervisord"]
                 |""".stripMargin.split('\n')

    when(mockOperations.gitDiff(hashA, hashB, fileName)).thenReturn(diffResult)
    when(mockOperations.gitLog(fileName)).thenReturn(logResult)
    when(mockOperations.readFile(fileName)).thenReturn(file)
    val result = scm.historyForFile(fileName)
    result.lineHistory should have size file.length
    result.lineHistory.map(_.originalLine.line) should contain theSameElementsAs file
    result.lineHistory.flatMap(_.history).distinct should contain only ShortHash("1716c35fad8757a347a77b7fbd45f90f8c9a247e")
  }

}
