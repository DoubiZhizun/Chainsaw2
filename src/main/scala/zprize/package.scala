package org.datenlord

import breeze.linalg.{DenseVector, max}
import breeze.math.Complex
import breeze.numerics.abs
import breeze.stats.mean
import org.datenlord.matlab._

import scala.annotation.tailrec
import scala.language.postfixOps
import scala.reflect.ClassTag
import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.fsm._

package object zprize {

  @tailrec
  def gcd(a: Int, b: Int): Int = {
    val (p, q) = if (a >= b) (a, b) else (b, a)
    if (q == 0) p
    else gcd(q, p % q)
  }

  // TODO: move zprize content to the top & move FTN content to an isolated package

  /** --------
   * algo parameters
   * -------- */
  val N1 = 254
  val N2 = 254
  val foldingFactor = 2
  val parallel = N1 / foldingFactor
  val fecLength = 32 * foldingFactor

  val frameSize = N1 * 4 * 16
  val fullSize = 256 * 4 * 16
  val preambleSize = frameSize / 8

  val cpLenth = 20
  val txFrameSize = (N2 * 2 + cpLenth) * 16

  val intrlvRow = N1 / 2
  val intrlvCol = 4 * 4 * 16

  val bitAlloc = Seq.fill(N1)(4)
  val powAlloc = Seq.fill(N1)(1.0)


  def readFtnGolden[T: ClassTag](name: String) = {
    matlabEngine.eval(s"value = load('./src/main/resources/ftnGolden/${N1}_$N1/$name', 'ans').ans.Data;")
    matlabEngine.eval(s"if size(value, 1) == 101\n\tvalue=value';\nend")
    val matrix = matlabEngine.getVariable("value").asInstanceOf[Array[Array[T]]]
    matrix.transpose.flatten
  }

  /** --------
   * golden from simulink
   * -------- */
  lazy val raw = readFtnGolden[Double]("rawData").map(d => BigInt(d.toInt)).toSeq
  lazy val coded = readFtnGolden[Double]("coded").map(d => BigInt(d.toInt)).toSeq
  lazy val interleaved = readFtnGolden[Double]("interleaved").map(d => BigInt(d.toInt)).toSeq
  lazy val symbols = readFtnGolden[MComplex]("mapped").toSeq.map(_.toComplex)
  lazy val ifftOut = readFtnGolden[Double]("dataTx").toSeq.map(_ * 16) // as simulink ifft is not scaled
  logger.info(s"size = ${ifftOut.length / 101 / 16}")

  /** --------
   * hardware data types
   * -------- */
  val symbolType = ComplexFixInfo(6, 11)
  val fftType = ComplexFixInfo(6, 11)

  /** --------
   * frame formats
   * -------- */
  def buildFrame8of9(total: Int, parallel: Int): FrameFormat = {
    val matrix = (0 until total).grouped(parallel).toSeq ++ Seq.fill(total / 8)(-1).grouped(parallel).toSeq
    FrameFormat(matrix)
  }

  val rawFrameFormat = buildFrame8of9(frameSize / 2, parallel)
  val codedFrameFormat = buildFrame8of9(frameSize, parallel * 2)

  val symbolFrameFormat = {
    val compact = FrameFormat((0 until frameSize / 4).grouped(N1).toSeq)
    val interpolated = compact.interpolate(4)
    interpolated.pad(8)
  }

  val realFrameFormat = buildFrame8of9(txFrameSize, N2 / 2 + 5)
  val txFrameFormat = MatrixFormat(N2 / 2 + 5, 18 * 4)

  /** --------
   * metrics
   * -------- */
  def fftMetric(epsilon: Double): Metric =
    (yours: Seq[Any], golden: Seq[Any]) => {
      val yourV = yours.tail.map(_.asInstanceOf[Double])
      val goldenV = golden.tail.map(_.asInstanceOf[Double])
      val errorV = yourV.zip(goldenV).map { case (y, g) => abs(y - g) }
      val pass = errorV.map(_ <= epsilon)
      val grids = pass.map(if (_) "???" else " ")
      val ret = pass.forall(_ == true)
      if(!ret) println(s"${grids.grouped(N2 * 2 + 20).toSeq.map(_.mkString(" ")).mkString("\n")}")
      ret
    }

  def intrlvMetric: Metric = (yours: Seq[Any], golden: Seq[Any]) => {
    val yourV = yours.tail.map(_.asInstanceOf[BigInt])
    val goldenV = golden.tail.map(_.asInstanceOf[BigInt])
    val errorV = yourV.zip(goldenV).map { case (y, g) => y == g }
    val grids = errorV.map(if (_) "???" else " ")
    val ret = errorV.forall(_ == true)
    if(!ret) println(s"${grids.grouped(N1).toSeq.map(_.mkString(" ")).mkString("\n")}")
    ret
  }

}
