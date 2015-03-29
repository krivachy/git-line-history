package org.ucl.compgs04.output

import org.scalatest.FlatSpec
import org.ucl.compgs04.model._


class CommandLineOutputTest extends FlatSpec {
  "CommandlineOutput" should "output the values in the command line" in {
    val test = FileLineHistory("TestFile", Seq(
      LineHistory(Seq(ShortHash("Alex"), ShortHash("Akos"), ShortHash("Sara")), Line("First Line")),
      LineHistory(Seq(ShortHash("Alex"), ShortHash("Akos"), ShortHash("Sara")), Line("Second Line")),
      LineHistory(Seq(ShortHash("Alex"), ShortHash("Akos"), ShortHash("Sara")), Line("Third Line"))
    ))
    new CommandLineOutput(println).processToOutput(test)
  }
}
