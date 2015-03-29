package org.ucl.compgs04.scm.git

import scala.util.parsing.combinator.RegexParsers

trait DiffParsingLogic extends RegexParsers {

  override def skipWhitespace: Boolean = false

  protected val lineEnd = "\r?\n".r

  protected val headerLine1 = """diff --git .+""".r <~ lineEnd

  private val startOfExtendedHeaderLines = Seq(
    "mode ",
    "deleted file mode",
    "new file mode",
    "index "
  ) //0-3

  protected val e = startOfExtendedHeaderLines.map(s => ((s + ".+").r <~ lineEnd).?)
  // Need this to force the type system to be correct
  /// ~ e(4) ~ e(5) ~ e(6) ~ e(7) ~ e(8) ~ e(9) ~ e(10)
  protected val extendedHeaderLines = e(0) ~ e(1) ~ e(2) ~ e(3) ^^ identity

  protected val headerLine3 = """--- .+""".r <~ lineEnd
  protected val headerLine4 = """\+\+\+ .+""".r <~ lineEnd

  protected val integer = """\d+""".r ^^ { _.toInt }

  protected val chunkStart = "@@ -" ~ integer ~ "," ~ integer ~ " +" ~ integer ~ "," ~ integer ~ """ @@.*""".r <~ lineEnd ^^ {
    case _ ~ oStart ~ _ ~ oCount ~ _ ~ rStart ~ _ ~ rCount ~ _ => (oStart, rStart)
  }

  protected def headerLines = headerLine1 ~ extendedHeaderLines ~ headerLine3 ~ headerLine4 ^^ identity

  protected def chunkHeader = headerLines ~> chunkStart ^^ identity


  protected val possibleLines = """[\+-\\ ].*""".r <~ lineEnd

  protected def lines2 = possibleLines ~ rep(possibleLines) ^^ { case  head ~ tail => head :: tail }

  sealed trait LineType
  case class Add(startLine: Int) extends LineType
  case object Unmodified extends LineType
  case class Delete(startLine: Int) extends LineType

  protected def modified = chunkHeader ~  lines2 ^^ {
    case (originalStart, revisedStart) ~ lines =>
      var originalPos = originalStart
      var revisedPos = revisedStart
      var originalChunks = Seq.empty[Chunk]
      var revisedChunks = Seq.empty[Chunk]
      var startOfLastChunk: LineType = Unmodified

      def handleNewLine(current: Option[LineType]) = {
        current match {
          case Some(Add(_)) =>
            startOfLastChunk match {
              case Add(_) => // do nothing
              case Delete(s) =>
                originalChunks = originalChunks :+ Chunk(s, originalPos - 1)
                startOfLastChunk = current.get
              case Unmodified =>
                startOfLastChunk = current.get
            }
          case Some(Delete(_)) =>
            startOfLastChunk match {
              case Add(s) =>
                revisedChunks = revisedChunks :+ Chunk(s, revisedPos - 1)
                startOfLastChunk = current.get
              case Delete(_) => // do nothing
              case Unmodified =>
                startOfLastChunk = current.get
            }
          case Some(Unmodified) =>
            startOfLastChunk match {
              case Add(s) =>
                revisedChunks = revisedChunks :+ Chunk(s, revisedPos - 1)
                startOfLastChunk = current.get
              case Delete(s) =>
                originalChunks = originalChunks :+ Chunk(s, originalPos - 1)
                startOfLastChunk = current.get
              case Unmodified => // do nothing
            }
          case None =>
            startOfLastChunk = Unmodified
        }
      }

      lines.foreach { line =>
          line.headOption match {
            case Some('+') =>
              handleNewLine(Some(Add(revisedPos)))
              revisedPos += 1
            case Some('-') =>
              handleNewLine(Some(Delete(originalPos)))
              originalPos += 1
            case Some(' ') | None =>
              handleNewLine(Some(Unmodified))
              originalPos += 1
              revisedPos += 1
            case Some('\\') | Some(_) => // Do nothing
          }
      }
      // We "close out" the diff to make sure all chunks are assembled
      handleNewLine(Some(Unmodified))
      (originalChunks, revisedChunks)
  }
}

object DiffParser extends DiffParsingLogic {
  def process(input: String): (Seq[Chunk], Seq[Chunk]) = {
    parseAll(modified, input + "\n") match {
      case Success(res, _) => res
      case NoSuccess(fail, _) => println("Input:\n" + input + "\nError:\n" + fail); throw new Exception(fail)
      case Failure(msg, _) => println("Input:\n" + input + "\nError:\n" + msg); throw new Exception(msg)
    }
  }
}
