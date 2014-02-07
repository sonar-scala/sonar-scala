package com.buransky.plugins.scoverage.samples.sbt.multiModule.module2

trait Animal {
  val legs: Int
  val eyes: Int
  val canFly: Boolean
  val canSwim: Boolean
}

object Animal {
  def fancy(farm: Iterable[Animal]): Iterable[Animal] =
    farm.filter(_.legs > 10).filter(_.canFly).filter(_.canSwim)
}