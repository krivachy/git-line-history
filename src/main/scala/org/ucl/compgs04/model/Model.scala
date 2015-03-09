package org.ucl.compgs04.model

case class Line(line: String) extends AnyVal
case class ShortHash(hash: String) extends AnyVal
case class LineHistory(history: Seq[ShortHash], originalLine: Line)
case class FileLineHistory(fileName: String, lineHistory: Seq[LineHistory])
