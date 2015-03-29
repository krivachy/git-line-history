package org.ucl.compgs04.scm.git

import org.ucl.compgs04.model._
import org.ucl.compgs04.scm.Scm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class Chunk(positionStart: Int, positionEnd: Int)
case class Diff(commitHashA: String, commitHashB: String, originalChanges: Seq[Chunk], revisedChanges: Seq[Chunk])

class GitScm(gitOperations: GitOperations) extends Scm {

  private val diffParser = DiffParser

  override def historyForFile(filePath: String): FileLineHistory = {
    val linesInFinalFile = gitOperations.readFile(filePath)
    val hashHistory = commitHistory(filePath)

    val lineHistory = hashHistory.toList match {
      case Nil => Nil
      case firstHash :: restHashes =>
        val (_, diffHistoryFutures) = restHashes.foldLeft((firstHash, Seq.empty[Future[Diff]])) {
          case ((previousCommitHash, list), nextCommitHash) =>
            (nextCommitHash, list :+ Future(diff(previousCommitHash.hash, nextCommitHash.hash, filePath)))
        }
        val extendedDiffHistoryWithFirstAndLast = Seq(Future(show(firstHash.hash, filePath))) ++ diffHistoryFutures :+ Future(currentFileStatusDiff(hashHistory.last.hash, filePath))
        val diffHistory = Await.result(Future.sequence(extendedDiffHistoryWithFirstAndLast), Duration.Inf)

        val finalFileHistory = traceHistoryThroughDiffs(diffHistory)

        assert(linesInFinalFile.size == finalFileHistory.size, s"Parsed history incorrect. Should have ended up with ${linesInFinalFile.size} lines, instead ended up with ${finalFileHistory.size} lines based on the history.")

        val correctOrderWithWorking = hashHistory.map(_.hash) :+ "working"
        finalFileHistory.zip(linesInFinalFile).map {
          case (hashes, line) => LineHistory(correctOrderWithWorking.filter(hashes.contains).map(ShortHash), Line(line))
        }
    }
    FileLineHistory(filePath, lineHistory)
  }

  private def traceHistoryThroughDiffs(diffHistory: Seq[Diff]): Seq[Set[String]] = {
    val initialFileStatus: Seq[Set[String]] = Seq.empty
    diffHistory.foldLeft(initialFileStatus) {
      case (currentFileStatus, diff) =>
        val hash = diff.commitHashB
        val linesDeleted = diff.originalChanges.foldLeft(Set.empty[Int]) { case (acc, chunk) => acc ++ (chunk.positionStart to chunk.positionEnd).toSet }
        val linesAdded = diff.revisedChanges.foldLeft(Set.empty[Int]) { case (acc, chunk) => acc ++ (chunk.positionStart to chunk.positionEnd).toSet }.toSeq.sorted
        val mergedHistory = linesDeleted.flatMap(index => currentFileStatus(index - 1)) + hash
        val fileStatusAfterDelete = currentFileStatus.zipWithIndex.filterNot { case (originalLineNum, currentLineNum) => linesDeleted.contains(currentLineNum + 1) }.unzip._1

        linesAdded.foldLeft(fileStatusAfterDelete) {
          case (lineStatus, addedLine) =>
            val (before, after) = lineStatus.splitAt(addedLine - 1)
            (before :+ mergedHistory) ++ after
        }
    }
  }

  private val commitRegex = """commit ([a-f0-9]{40})""".r
  private def commitHistory(fileName: String): Seq[ShortHash] = {
    commitRegex.findAllMatchIn(gitOperations.gitLog(fileName)).map(_.group(1)).map(ShortHash).toSeq.reverse
  }

  private def sanitizeForDiffParsing(s: String): String = s.split("\r?\n").mkString("\n")

  private val emptyResult = (Seq.empty[Chunk], Seq.empty[Chunk])

  private def diff(commitHashA: String, commitHashB: String, fileName: String): Diff = {
    val diffOutput = gitOperations.gitDiff(commitHashA, commitHashB, fileName)
    val (deleteChanges, addChanges) = diffParser.process(sanitizeForDiffParsing(diffOutput))
    Diff(commitHashA, commitHashB, deleteChanges, addChanges)
  }

  private def currentFileStatusDiff(lastCommitHash: String, fileName: String): Diff = {
    val diffOutput = gitOperations.gitWorkingCopyDiff(fileName)
    val sanitizedOutput = sanitizeForDiffParsing(diffOutput)
    val (deleteChanges, addChanges) = if (diffOutput.trim.isEmpty) emptyResult else diffParser.process(sanitizedOutput)
    Diff(lastCommitHash, "working", deleteChanges, addChanges)
  }

  private def show(commitHash: String, fileName: String): Diff = {
    val diffOutput = gitOperations.gitShow(commitHash, fileName)
    val sanitizedOutput = diffOutput.split("\r?\n").tail.mkString("\n")
    val (deleteChanges, addChanges) = diffParser.process(sanitizedOutput)
    Diff("/dev/null", commitHash, deleteChanges, addChanges)
  }
}