package org.datenlord
package arithmetic

import dfg.ArithInfo

import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Random

class BmcTest extends AnyFlatSpec {

  def genNoise = Random.nextInt(17) - 8 // -8~8

  // input bits formed a rectangular
  val infosRegular = Seq.fill(486)(ArithInfo(40, 0))

  // input bits formed a "pyramid", typically generated by a multiplier
  val infosFromMult = (1 until 754 / 40).flatMap { i =>
    val mid = 754 / 2 / 40
    val shift = i * 40
    val number = (mid - (i - mid).abs) * 6
    Seq.fill(number)(ArithInfo(40, shift))
  }

  // a "pyramid" with noise of shift and width, typically generated by a Karatsuba multiplier
  val infosFromMultWithNoise = (1 until 754 / 40).flatMap { i =>
    val mid = 754 / 2 / 40
    val shift = i * 40 + genNoise
    val number = (mid - (i - mid).abs) * 6
    Seq.fill(number)(ArithInfo(40 + genNoise, shift))
  }

  def testForInfos(infos: Seq[ArithInfo]) = {
    val data = infos.map(info => Random.nextBigInt(info.width))
    val config = BmcConfig(infos)
    TransformTest.test(config.implH, data, config.metric)
  }

  "Bit Matrix Compressor" should "work for all situations" in Seq(infosRegular, infosFromMult, infosFromMultWithNoise).map(testForInfos)

  ignore should "impl for rectangular bits" in VivadoImpl(BmcConfig(infosRegular).implH, "RegularSum")
  ignore should "impl for output from a multiplier" in VivadoImpl(BmcConfig(infosFromMult).implH, "PostSum")
  ignore should "impl for irregular output from a multiplier" in VivadoImpl(BmcConfig(infosFromMultWithNoise).implH, "PostSumIrregular")
}
