package org.ucl.compgs04.scm

import java.io.File

trait Scm {

  def historyForFile(file: File): String

}
