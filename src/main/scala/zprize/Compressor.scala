package org.datenlord
package zprize

import org.datenlord.xilinx.VivadoUtil
import spinal.core.{Bool, HertzNumber}

/** define necessary properties for a basic compressor which can be used to build a compressor tree
 */
abstract class Compressor {

  val name = getClass.getSimpleName.init

  val isFixed: Boolean // if the size is fixed, it is a GPC, otherwise, it is a row compressor

  val widthMax: Int // for delay consideration

  val widthMin: Int

  require(widthMax >= widthMin, s"The widthMax should be greater than or equal to widMin !")

  /** -------- key definitions
   * --------
   */

  /** number of bits in input columns, low to high
   */
  def inputFormat(width: Int): Seq[Int]

  /** number of bits in output columns, low to high
   */
  def outputFormat(width: Int): Seq[Int]

  /** number of CLBs
   */
  def areaCost(width: Int): Int

  /** hardware implementation, the compressor is responsible for padding zeros
   */
  def impl(bitsIn: BitHeap[Bool], width: Int): BitHeap[Bool]

  /** -------- utils
   * --------
   */

  def inputBitsCount(width: Int) = inputFormat(width).sum

  def outputBitsCount(width: Int) = outputFormat(width).sum

  def bitReduction(width: Int): Int =
    inputBitsCount(width) - outputBitsCount(width)

  def heightReduction(width: Int): Int = inputFormat(width).max - outputFormat(width).max

  def reductionRatio(width: Int): Double = inputBitsCount(width).toDouble / outputBitsCount(width)

  def reductionEfficiency(width: Int): Double = bitReduction(width).toDouble / areaCost(width)

  def utilRequirement(width: Int): VivadoUtil

  def fMaxRequirement: HertzNumber

  /** this is very beautiful, try it!
   */
  def toString(width: Int) = {
    val dotsIn    = BitHeap.getHeapFromHeights(Seq(inputFormat(width)), Seq(0), Seq(0)).toString
    val dotsOut   = BitHeap.getHeapFromHeights(Seq(outputFormat(width)), Seq(0), Seq(0)).toString
    val length    = outputFormat(width).length
    val arrowLine = s"${" " * (length / 2) * 2}\u2193"
    val shiftedDotsIn =
      dotsIn.split("\n").head + "\n" + dotsIn.split("\n").tail.map(_.padToLeft(length * 2 - 1, ' ')).mkString("\n")
    s"$shiftedDotsIn\n$arrowLine\n$dotsOut"
  }
}
