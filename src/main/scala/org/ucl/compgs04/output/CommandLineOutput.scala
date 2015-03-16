package org.ucl.compgs04.output

import org.ucl.compgs04.model.FileLineHistory

class CommandLineOutput extends Output {
  /**
   * Takes a file history and outputs it to wherever (command line/file/etc.)
   * @param fileHistory - the result
   */
  override def processToOutput(fileHistory: FileLineHistory): Unit = ??? // TODO: Implement for expected output
}
