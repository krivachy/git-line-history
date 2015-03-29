package org.ucl.compgs04.scm.git

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
  override def gitLog(fileName: String): String = Seq("git", "log", fileName).!!
  override def gitDiff(commitHashA: String, commitHashB: String, fileName: String): String = Seq("git", "diff", commitHashA, commitHashB, "--", fileName).!!
  override def gitWorkingCopyDiff(fileName: String): String = Seq("git", "diff", "--", fileName).!!
  override def gitShow(commitHash: String, fileName: String): String = Seq("git", "show", commitHash, "--oneline", "--", fileName).!!
  override def readFile(fileName: String): Seq[String] = Source.fromFile(fileName).getLines().toSeq
}