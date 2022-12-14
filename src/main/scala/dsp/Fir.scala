package org.datenlord
package dsp

import spinal.core._
import spinal.lib._

import scala.language.postfixOps

case class FirConfig(coeffs: Seq[Double], typeIn: HardType[SFix], structure: SopStructure) extends TransformBase {

  val taps = coeffs.length
  val typeCoeff = HardType(SFix(0 exp, -17 exp))
  val typeOut = HardType(SFix(log2Up(taps) exp, typeIn().minExp exp))

  override def impl(dataIn: Seq[Any]) = {
    val data = dataIn.asInstanceOf[Seq[Double]]
    matlab.Dsp.fir(data.toArray, coeffs.toArray).drop(coeffs.length - 1) // drop leading results, size unchanged
  }

  override val implMode = Infinite

  override val size = (1, 1)

  override def latency = DoFir.latency(taps, structure)

  override def implH = Fir(this)
}

case class Fir(config: FirConfig) extends TransformModule[SFix, SFix] {

  import config._

  override val dataIn = slave Flow Fragment(Vec(typeIn, inputPortWidth))
  override val dataOut = master Flow Fragment(Vec(typeOut, outputPortWidth))

  val coeffsHard = coeffs.map(SFConstant(_, typeCoeff()))

  def mult(data: SFix, coeff: SFix) = (data * coeff).truncate(typeOut)

  def add(a: SFix, b: SFix) = a + b

  def pipeline(value: SFix, nothing: Int) = value.d(1)

  dataOut.fragment.head := DoFir(dataIn.fragment.head, coeffsHard, mult, add, structure)

  autoValid()
  autoLast()
}
