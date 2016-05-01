package com.buransky.plugins.scoverage.samples.sbt.multiModule.module1

trait Pub {
  def offer: Iterable[_ <: Beer]
  def giveMeGreat: Beer
}

object Delirium extends Pub {
  def offer = List(HordonBeer(0.5), ChimayBeer(0.2))
  def giveMeGreat = offer.filter(_.isGood).filter(_.volume > 0.3).head
}