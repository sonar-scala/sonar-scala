package com.buransky.plugins.scoverage.samples.sbt.multiModule.module2

import org.scalatest.{Matchers, FlatSpec}

class AnimalSpec extends FlatSpec with Matchers {
  behavior of "fancy"

  it should "do nothing" in {
    Animal.fancy(Nil) should equal(Nil)
  }
}
