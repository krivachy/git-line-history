package org.ucl.compgs04

import java.io.File

import org.clapper.argot.{ArgotParser, ArgotUsageException}
import org.ucl.compgs04.output.CommandLineOutput
import org.ucl.compgs04.scm.Scm
import org.ucl.compgs04.scm.git.{GitScm, RealGitOperations}

object GitLineHistory {

  // Argot docs: http://software.clapper.org/argot/
  val parser = new ArgotParser("git-line-history", preUsage = Some("Version 1.0"))

  val scm = parser.option[Scm]("scm", "scm", "Select the SCM to use: git (default), svn") {
    (s, opt) =>
      s.toLowerCase match {
        case "git" => new GitScm(RealGitOperations)
        case "svn" => parser.usage("SVN is not supported yet.")
        case _ => parser.usage(s"Provided SCM not valid: $s")
      }
  }

  val file = parser.parameter[File]("file", "File to produce git history for.", optional = false) {
    (s, opt) =>
      val file = new File(s)
      if (!file.exists) parser.usage(s"""Input file "$s" does not exist.""")
      file
  }

  def main(args: Array[String]): Unit = {
    try {
      process(args)
    } catch {
      case e: ArgotUsageException => println(e.message)
      case e: Exception => println(e.getMessage)
    }
  }

  def process(args: Array[String]): Unit = {
      val output = new CommandLineOutput(println)
      parser.parse(args)
      val scmToUse = scm.value.getOrElse(new GitScm(RealGitOperations))
      val inputFile = file.value.getOrElse(throw new Exception("File not parsed correctly."))
      val result = scmToUse.historyForFile(inputFile.getAbsolutePath)
    
      output.processToOutput(result)
  }
}
