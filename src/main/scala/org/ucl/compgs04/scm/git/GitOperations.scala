package org.ucl.compgs04.scm.git

import java.io.File

import scala.io.Source
import scala.sys.process._

trait GitOperations {
  def gitLog(fileName: String): String
  def gitDiff(commitHashA: String, commitHashB: String, fileName: String): String
  def gitWorkingCopyDiff(fileName: String): String
  def gitShow(commitHash: String, fileName: String): String
  def readFile(fileName: String): Seq[String]
}

object RealGitOperations extends GitOperations {
  private def getWorkingDirectory(fileName: String): File = new File(fileName).getParentFile

  private def exec(fileName: String, commands: Seq[String]): String = Process(commands, getWorkingDirectory(fileName)).!!

  override def gitLog(fileName: String): String = exec(fileName, Seq("git", "log", fileName))
  override def gitDiff(commitHashA: String, commitHashB: String, fileName: String): String = exec(fileName, Seq("git", "diff", commitHashA, commitHashB, "--", fileName))
  override def gitWorkingCopyDiff(fileName: String): String = exec(fileName, Seq("git", "diff", "--", fileName))
  override def gitShow(commitHash: String, fileName: String): String = exec(fileName, Seq("git", "show", commitHash, "--oneline", "--", fileName))
  override def readFile(fileName: String): Seq[String] = Source.fromFile(fileName).getLines().toSeq
}