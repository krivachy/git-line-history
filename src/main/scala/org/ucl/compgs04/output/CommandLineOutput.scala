package org.ucl.compgs04.output

import org.ucl.compgs04.model.FileLineHistory

object CommandLineOutput extends Output {

  private val lineSeparator = System.getProperty("line.separator")
  /**
   * Takes a file history and outputs it to wherever (command line/file/etc.)
   * @param fileHistory - the result
   */
  override def processToOutput(fileHistory: FileLineHistory): Unit = {
    val lineHis = new StringBuilder
    fileHistory.lineHistory.toList match {
      case Nil => lineHis.append(s"File '${fileHistory.fileName}' has no history.")
      case history =>
        history.foreach { line =>
          lineHis.append(line.history.map(hash => hash.hash.take(7)).mkString(" "))
          lineHis.append(": ").append(line.originalLine.line)
          lineHis.append(lineSeparator)
        }
    }
    print(lineHis)
  }
}
