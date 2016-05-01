package com.buransky.plugins.scoverage.samples.sbt.multiModule.module1

import scala.util.Random

trait Beer {
  val volume: Double
  def isGood: Boolean = (volume > 0.0)
}

case object EmptyBeer extends {
  val volume = 0.0
} with Beer

trait SlovakBeer extends Beer {
  override def isGood = Random.nextBoolean
}

trait BelgianBeer extends Beer {
  if (volume > 0.25)
    throw new IllegalArgumentException("Too big beer for belgian beer!")

  override def isGood = true
}

case class HordonBeer(volume: Double) extends SlovakBeer {
  override def isGood = false
}

case class ChimayBeer(volume: Double) extends BelgianBeer
