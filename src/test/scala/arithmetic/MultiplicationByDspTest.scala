package org.datenlord
package arithmetic

import device.MultiplicationByDspConfig
import xilinx.VivadoUtilRequirement

import org.scalatest.flatspec.AnyFlatSpec
import spinal.core.IntToBuilder

import scala.language.postfixOps
import scala.util.Random

class MultiplicationByDspTest extends AnyFlatSpec {

  val testCount = 1000
  val config0 = MultiplicationByDspConfig(FullMultiplier)
  val config1 = MultiplicationByDspConfig(LsbMultiplier)
  val config2 = MultiplicationByDspConfig(SquareMultiplier)

  val data16 = (0 until testCount * 2).map(_ => Random.nextBigInt(32))
  val data17 = (0 until testCount * 2).map(_ => Random.nextBigInt(34))
  val data17Square = (0 until testCount).flatMap { _ =>
    val data = Random.nextBigInt(34)
    Seq.fill(2)(data)
  }

  "Multiplication by DSP Slice for Xilinx Ultrascale" should "work for full multiplication" in TransformTest.test(config0.implH, data16, name = "Mult32ForFull")
  it should "work for low-bit multiplication" in TransformTest.test(config1.implH, data17, name = "Mult34ForLow")
  it should "work for squaring" in TransformTest.test(config2.implH, data17Square, name = "Mult34ForSquare")

  val utilRequirement = VivadoUtilRequirement(dsp = 3)
  val fmaxRequirement = 800 MHz

  ignore should "synth for full multiplication " in VivadoSynth(config0.implH, "Mult32ForFull").require(utilRequirement, fmaxRequirement)
  ignore should "synth for low-bit multiplication " in VivadoSynth(config1.implH, "Mult34ForLow").require(utilRequirement, fmaxRequirement)
  ignore should "synth for squaring " in VivadoSynth(config2.implH, "Mult34ForSquare").require(utilRequirement, fmaxRequirement)
}
