package org.ucl.compgs04.scm

import java.io.File

import org.ucl.compgs04.model.FileLineHistory

trait Scm {

  def historyForFile(file: File): FileLineHistory

}
