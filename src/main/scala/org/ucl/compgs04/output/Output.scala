package org.ucl.compgs04.output

import org.ucl.compgs04.model.FileLineHistory

trait Output {
  /**
   * Takes a file history and outputs it to wherever (command line/file/etc.)
   * @param fileHistory - the result
   */
  def processToOutput(fileHistory: FileLineHistory): Unit
}
