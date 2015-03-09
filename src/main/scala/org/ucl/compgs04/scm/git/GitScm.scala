package org.ucl.compgs04.scm.git

import java.io.File
import org.ucl.compgs04.model.FileLineHistory
import org.ucl.compgs04.scm.Scm
import scala.sys.process._

class GitScm extends Scm {
  // Guide for scala.sys.process: http://www.scala-lang.org/api/current/index.html#scala.sys.process.package
  override def historyForFile(file: File): FileLineHistory = {
    Seq("git", "show", file.getAbsolutePath).!!
    // TODO: Implement algorithm
    ???
  }
}
