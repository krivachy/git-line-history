package org.ucl.compgs04.model

case class Line(line: String) extends AnyVal
case class ShortHash(hash: String) extends AnyVal {
  def getShort() = hash.substring(0,7)
}
case class LineHistory(history: Seq[ShortHash], originalLine: Line) {
  def addHash(hash: ShortHash) = this.copy(history = Seq(hash) ++ this.history)
}
case class FileLineHistory(fileName: String, lineHistory: Seq[LineHistory])
