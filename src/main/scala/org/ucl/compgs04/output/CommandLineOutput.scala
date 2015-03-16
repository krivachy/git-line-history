package org.ucl.compgs04.output

import org.ucl.compgs04.model.FileLineHistory

object CommandLineOutput extends Output {
  /**
   * Takes a file history and outputs it to wherever (command line/file/etc.)
   * @param fileHistory - the result
   */
  override def processToOutput(fileHistory: FileLineHistory): Unit = {
    println("File history for the following file: "+fileHistory.fileName)
    val lineHis = new StringBuilder
    fileHistory.lineHistory.foreach{
      line =>
        lineHis.append(line.history.map(hash => hash.hash).mkString(" "))
        lineHis.append(": ").append(line.originalLine.line)
        lineHis.append(System.getProperty("line.separator"))
    }
    print(lineHis)
  } // TODO: Implement for expected output
}
