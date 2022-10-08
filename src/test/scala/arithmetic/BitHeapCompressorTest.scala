package org.datenlord
package arithmetic

import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Random

class BitHeapCompressorTest extends AnyFlatSpec {

  def genNoise: Int = Random.nextInt(17) - 8 // -8~8

  val infosSmall = Seq.fill(10)(ArithInfo(10, 0))

  val infosSmallWithNoise = (1 to 5).flatMap { i =>
    val mid = 3
    val shift = i * 2
    val number = (mid - (i - mid).abs) * 2
    Seq.fill(number)(ArithInfo(2, shift))
  }

  // input bits formed a rectangular
  val infosRegular = Seq.fill(40)(ArithInfo(40, 0))

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

  val infosWithNegative = Seq.fill(10)(ArithInfo(10, 0, true)) ++ Seq.fill(5)(ArithInfo(5, 5, false))

  def testForInfos(infos: Seq[ArithInfo]) = {
    val data = (0 until 1000).flatMap(_ => infos.map(info => Random.nextBigInt(info.width)))
    val config = BitHeapCompressorUseInversionConfig(infos)
    TransformTest.test(config.implH, data)
  }

  behavior of "Bit Matrix Compressor"

  it should "work on simple situation" in testForInfos(infosSmall)
  it should "impl for a simple situation" in VivadoImpl(BitHeapCompressorConfig(infosSmall).implH, "SmallTree")

  it should "work on simple situation with noise" in testForInfos(infosSmallWithNoise)
  ignore should "impl for simple situation with noise" in testForInfos(infosSmallWithNoise)

  skipComponentSim = true

  it should "work for situation 0" in testForInfos(infosRegular)
  it should "work for situation 1" in testForInfos(infosFromMult)
  it should "work for situation 2" in testForInfos(infosFromMultWithNoise)

  ignore should "impl for rectangular bits" in VivadoImpl(BitHeapCompressorConfig(infosRegular).implH, "RegularSum")
  ignore should "impl for output from a multiplier" in VivadoImpl(BitHeapCompressorConfig(infosFromMult).implH, "PostSum")
  it should "impl for irregular output from a multiplier" in VivadoImpl(BitHeapCompressorConfig(infosFromMultWithNoise).implH, "PostSumIrregular")

  it should "work for negative operands" in testForInfos(infosWithNegative)
}
