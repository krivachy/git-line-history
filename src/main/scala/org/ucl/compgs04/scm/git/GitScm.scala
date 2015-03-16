package org.ucl.compgs04.scm.git

import java.io.File
import difflib.DiffUtils
import org.ucl.compgs04.model.{LineHistory, ShortHash, Line, FileLineHistory}
import org.ucl.compgs04.scm.Scm
import scala.io.Source
import scala.sys.process._
import scala.collection.JavaConversions._

//case class Position(start: Int, length: Int)
case class Chunk(position: Int, contents: Seq[Line])
case class Delta(original: Chunk, revised: Chunk)
case class Diff(deltas: Seq[Delta])

class GitScm(gitOperations: GitOperations) extends Scm {
  // Guide for scala.sys.process: http://www.scala-lang.org/api/current/index.html#scala.sys.process.package
  override def historyForFile(filePath: String): FileLineHistory = {
//    Seq("git", "show", filePath).!!
    // TODO: Implement algorithm
    val linesInFinalFile = gitOperations.readFile(filePath)

    def initialHistoryForCommit(hash: ShortHash) = linesInFinalFile.map(line => LineHistory(Seq(hash), Line(line))).toSeq
    def emptyLineHistory = linesInFinalFile.map(line => LineHistory(Nil, Line(line)))

    val lineHistory = commitHistory(filePath) match {
      case Nil => Nil
      case one :: Nil => initialHistoryForCommit(one)
      case first :: rest =>
        ???
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
    Diff(deltas)
  }
}
