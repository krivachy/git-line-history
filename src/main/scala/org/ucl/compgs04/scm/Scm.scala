package org.ucl.compgs04.scm

import org.ucl.compgs04.model.FileLineHistory

trait Scm {

  def historyForFile(filePath: String): FileLineHistory

}
