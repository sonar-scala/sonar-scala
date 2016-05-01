package com.buransky.plugins.scoverage.samples.sbt.multiModule.module1

import org.scalatest.{FlatSpec, Matchers}

class PubSpec extends FlatSpec with Matchers {
  behavior of "Delirium"

  it must "give me what I want" in {
    the[NoSuchElementException] thrownBy Delirium.giveMeGreat
  }
}