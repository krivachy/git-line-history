package org.ucl.compgs04.scm.git

import difflib.DiffUtils
import org.ucl.compgs04.model._
import org.ucl.compgs04.scm.Scm
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

//case class Position(start: Int, length: Int)
case class Chunk(position: Int, contents: Seq[Line])
case class Delta(original: Chunk, revised: Chunk)
case class Diff(commitHashA: String, commitHashB: String, deltas: Seq[Delta])

case class IntermediateLineHistory(currentLines: Seq[Line], originalLineNumbers: Seq[LineNumberHistory])
case class IntermediateHistory(lineHistory: Seq[IntermediateLineHistory]) {
  def modifyChunk(startPosition: Int, length: Int, hash: String): IntermediateHistory = {
    val (_, updatedHistory, _) = lineHistory.foldLeft((0, Seq.empty[IntermediateLineHistory], Seq.empty[IntermediateLineHistory])) {
      case ((counter, historyAccumulator, chunkAccumulator), nextChunk) =>
        val chunkSize = nextChunk.currentLines.size
        val (h, c) = if(counter <= startPosition && counter + chunkSize > startPosition) { //TODO put logic into chunk
          // Found this is in this chunk
          // We need to split it if the chunks don't match exactly
          val (notPart, firstPart) = nextChunk.currentLines.splitAt(startPosition-counter)
          val amendedFirstPart = historyAccumulator :+ IntermediateLineHistory(notPart, nextChunk.originalLineNumbers)
          if(firstPart.size > length) {
            val (wholePart, lastNotPart) = firstPart.splitAt(length)
            val fullHistory = amendedFirstPart :+ IntermediateLineHistory(wholePart, nextChunk.originalLineNumbers.map(_.addHash(ShortHash(hash)))) :+ IntermediateLineHistory(lastNotPart, nextChunk.originalLineNumbers)
            (fullHistory, Nil)
          } else {
            (amendedFirstPart, Seq(IntermediateLineHistory(firstPart, nextChunk.originalLineNumbers)))
          }
        } else if(counter <= startPosition + length && counter + chunkSize > startPosition + length) {
          // Case when we're rolling from previous chunk
          if(chunkAccumulator.isEmpty)
            assert(chunkAccumulator.nonEmpty)
          val (stillIn, outside) = nextChunk.currentLines.splitAt(startPosition+length)

          val outsideHistory = IntermediateLineHistory(outside, nextChunk.originalLineNumbers)
          val currentLines = chunkAccumulator.flatMap(_.currentLines) ++ stillIn
          val originalLineNumbers = chunkAccumulator.flatMap(_.originalLineNumbers).map(_.addHash(ShortHash(hash)))

          (historyAccumulator :+ IntermediateLineHistory(currentLines.toIndexedSeq, originalLineNumbers) :+ outsideHistory, Nil)
        } else {
          chunkAccumulator match {
            case Nil =>
              (historyAccumulator :+ nextChunk, Nil)
            case some =>
              (historyAccumulator, some :+ nextChunk)
          }
        }
        (counter + chunkSize, h, c)

    }
    IntermediateHistory(updatedHistory.toIndexedSeq)
  }
}

class GitScm(gitOperations: GitOperations) extends Scm {
  // Guide for scala.sys.process: http://www.scala-lang.org/api/current/index.html#scala.sys.process.package
  override def historyForFile(filePath: String): FileLineHistory = {
//    Seq("git", "show", filePath).!!
    // TODO: Implement algorithm
    val linesInFinalFile = gitOperations.readFile(filePath)

    def initialHistoryForCommit(hash: ShortHash) = linesInFinalFile.map(line => LineHistory(Seq(hash), Line(line))).toSeq
    //def emptyLineHistory = linesInFinalFile.map(line => LineHistory(Nil, Line(line)))

    val lineHistory = commitHistory(filePath) match {
      case Nil => Nil
      case one :: Nil => initialHistoryForCommit(one)
      case first :: rest =>
        val (_, diffHistoryFutures) = rest.foldLeft((first, Seq.empty[Future[Diff]])) {
          case ((previousCommitHash, list), nextCommitHash) =>
            (nextCommitHash, list :+ Future(diff(previousCommitHash.hash, nextCommitHash.hash, filePath)))
        }

        val diffHistory = Await.result(Future.sequence(diffHistoryFutures), Duration.Inf)
        val initial = linesInFinalFile.zipWithIndex.map { case (line, number) => IntermediateLineHistory(IndexedSeq(Line(line)), Seq(LineNumberHistory(Nil, number))) }

        val finalHistory = diffHistory.foldLeft(IntermediateHistory(initial)) {
          (intermediateHistory, diff) =>
            diff.deltas.foldLeft(intermediateHistory) {
              (history, delta) =>
                delta.revised.contents.toList match {
                  case Nil => // An add, we don't care about it
                    history
                  case lines =>
                    history.modifyChunk(delta.revised.position, delta.revised.contents.size, diff.commitHashB)
                }
            }
        }
        val pairedWithNumber = finalHistory.lineHistory.flatMap(_.originalLineNumbers).groupBy(_.originalLineNumber).toSeq.sortBy(_._1).zipWithIndex
        pairedWithNumber.foreach { case ((inNum, history), num) => assert(inNum == num, s"$inNum != $num: ${history.flatMap(_.history).map(_.hash).mkString(", ")}") }
        val verifiedPairing = pairedWithNumber.map(_._1._2.flatMap(_.history)) // TODO: preserver order
        verifiedPairing.zip(linesInFinalFile).map { case (hashes, line) => LineHistory(hashes, Line(line)) }
    }
    FileLineHistory(filePath, lineHistory)
  }

  val commitRegex = """commit ([a-f0-9]{40})""".r
  def commitHistory(fileName: String): Seq[ShortHash] = {
    val gitOutput = gitOperations.gitLog(fileName)
    val matches = commitRegex.findAllMatchIn(gitOutput)
    matches.map(_.group(1)).map(ShortHash).toList
  }

  def diff(commitHashA: String, commitHashB: String, fileName: String): Diff = {
    val diffOutput = gitOperations.gitDiff(commitHashA, commitHashB, fileName)
    val patch = DiffUtils.parseUnifiedDiff(diffOutput.split('\n').toList)
    val deltas = patch.getDeltas.map { delta =>
      val orig = delta.getOriginal
      val rev = delta.getRevised
      Delta(
        original = Chunk(orig.getPosition, orig.getLines.asInstanceOf[java.util.List[String]].map(Line.apply)),
        revised = Chunk(rev.getPosition, rev.getLines.asInstanceOf[java.util.List[String]].map(Line.apply))
      )
    }
    Diff(commitHashA, commitHashB, deltas)
  }
}
