package com.buransky.plugins.scoverage.samples.sbt.multiModule.module1

import org.scalatest.{Matchers, FlatSpec}

class BeerSpec extends FlatSpec with Matchers {
  behavior of "Beer"

  "isGood" must "be true if not empty" in {
    val beer = new Beer { val volume = 0.1 }
    beer.isGood should equal(true)
  }

  behavior of "EmptyBeer"

  it must "be empty" in {
    EmptyBeer.volume should equal(0.0)
  }
}
