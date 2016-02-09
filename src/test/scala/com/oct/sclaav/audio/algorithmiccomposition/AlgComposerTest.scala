package com.oct.sclaav.audio.algorithmiccomposition

import com.oct.sclaav.TestHelpers
import de.sciss.synth._
import ugen._
import Ops._
import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}

class AlgComposerTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("does audio") {

    val cfg = Server.Config()
    cfg.program = "/usr/bin/scsynth"

    // runs a server and executes the function
    // when the server is booted, with the
    // server as its argument
    Server.run(cfg) { s =>
      // play is imported from package de.sciss.synth.
      // it provides a convenience method for wrapping
      // a synth graph function in an `Out` element
      // and playing it back.
      play {
        val f = LFSaw.kr(0.4).madd(24, LFSaw.kr(8, 7.23).madd(3, 80)).midicps
        CombN.ar(SinOsc.ar(f) * 0.04, 0.2, 0.2, 4)
      }
    }
  }

}
