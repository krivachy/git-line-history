package org.ucl.compgs04.scm.git

import org.scalatest.FlatSpec


class GitScmTest extends FlatSpec {
  val scm = new GitScm
  "Git Scm" should "run diff correctly" in {
    // TODO hard-code revision short hashes
    val revision1 = "short_hash_1"
    val revision2 = "short_hash_2"
    val revision3 = "short_hash_3"
    val revision4 = "short_hash_4"
    val revision5 = "short_hash_5"

    val output = new Array[String](15)
    output(0) = revision1 + " " + revision4 + ": 1 changed"
    output(1) = revision1 + " " + revision4 + ": 2 changed"
    output(2) = revision1 + " " + revision4 + ": 3 changed"
    output(3) = revision1 + " " + revision3 + " " + revision4 + " " + revision5 + ": new 1"
    output(4) = revision1 + " " + revision3 + " " + revision4 + " " + revision5 + ": new 2"
    output(5) = revision1 + " " + revision3 + " " + revision4 + " " + revision5 + ": new 3"
    output(6) = revision1 + " " + revision3 + " " + revision4 + " " + revision5 + ": new 4"
    output(7) = revision1 + " " + revision3 + " " + revision4 + " " + revision5 + ": new 5"
    output(8) = revision1 + " " + revision3 + " " + revision4 + " " + revision5 + ": new 6"
    output(9) = revision1 + " " + revision3 + " " + revision4 + " " + revision5 + ": new 7"
    output(10) = revision1 + " " + revision3 + " " + revision4 + " " + revision5 + ": new 8"
    output(11) = revision1 + " " + revision3 + ": 8 changed"
    output(12) = revision1 + " " + revision3 + ": 9 changed"
    output(13) = revision1 + " " + revision3 + ": 10 changed"
    output(14) = revision2 + ": 11"

    val commandLineOutput = output.mkString("\n")

    val actualOutput = ??? // TODO call function once implemented
    assert(actualOutput == commandLineOutput)
  }
}
