package org.datenlord
package arithmetic

import algos.{EcGroup, EcPointProj}
import dfg.{RingDag, RingPort}

import cc.redberry.rings.scaladsl._
import cc.redberry.rings

object PAdd {

  def apply(): RingDag = {

    val k = 377
    val M = algos.ZPrizeMSM.baseModulus

    implicit val ec: EcGroup = algos.ZPrizeMSM.ec
    val golden = (dataIn: Seq[BigInt]) => {
      val Seq(x0, y0, z0, x1, y1, z1) = dataIn
      val p0 = EcPointProj(x0, y0, z0)
      val p1 = EcPointProj(x1, y1, z1)
      val ret = ec.paddHomo(p0, p1)
      Seq(ret.x, ret.y, ret.z).map(_.toBigInt)
    }

    implicit val graph: RingDag = new RingDag("PAddGraph", golden)

    val letters = Seq("x", "y", "z")
    val digits = Seq("0", "1")
    val inputs = Seq.tabulate(3, 2)((i, j) => graph.addInput(letters(i) + digits(j), k)).flatten
    val Seq(x1, x2, y1, y2, z1, z2) = inputs

    val outputs = letters.map(letter => graph.addOutput(letter + "2", k))
    val Seq(x3, y3, z3) = outputs

    val barrett = Barrett377(k, M).asRingOp(_)

    def multiply(x: RingPort, y: RingPort) = barrett(Seq(x, y)).head

    def add(x: RingPort, y: RingPort) = x

    def subtract(x: RingPort, y: RingPort) = x

    implicit class localOps(x: RingPort) {
      def *(y: RingPort) = Barrett377(k, M).asRingOp(Seq(x, y)).head

      def +(y: RingPort) = x

      def -(y: RingPort) = x

      def times3 = x
    }

    val m00 = multiply(x1, x2) // x1x2
    val m01 = multiply(y1, y2) // y1y2
    val m02 = multiply(z1, z2) // z1z2
    val a00 = add(x1, y1) // x1+y1
    val a01 = add(x2, y2) // x2+y2
    val a02 = add(x1, z1) // x1+z1
    val a03 = add(x2, z2) // x2+z2
    val a04 = add(y1, z1) // y1+z1
    val a05 = add(y2, z2) // y2+z2

    logger.info("stage 0 done")

    // stage1
    val a10 = add(m00, m01) // x1x2 + y1y2
    val a11 = add(m00, m02) // x1x2 + z1z2
    val a12 = add(m01, m02) // y1y2 + z1z2
    val m10 = multiply(a00, a01) // x1x2 + y1y2 + x1y2 + x2y1
    val m11 = multiply(a02, a03) // x1x2 + z1z2 + x1z2 + x2z1
    val m12 = multiply(a04, a05) // y1y2 + z1z2 + y1z2 + y2z1

    logger.info("stage 1 done")

    // stage 2
    val tri20 = m02.times3 // 3z1z2
    val s20 = subtract(m10, a10) // x1y2 + x2y1
    val s21 = subtract(m11, a11) // x1z2 + x2z1
    val s22 = subtract(m12, a12) // y1z2 + y2z1

    logger.info("stage 2 done")

    // stage 3
    val tri30 = m00.times3 // 3x1x2
    val a30 = add(m01, tri20) // y1y2 + 3z1z2
    val s30 = subtract(m01, tri20) // y1y2 - 3z1z2
    val tri31 = s21.times3 // 3(x1z2 + x2z1)

    logger.info("stage 3 done")

    // stage 4
    val m40 = multiply(s30, s20) // (y1y2 - 3z1z2)(x1y2 + y2x1)
    val m41 = multiply(tri31, s22) // 3(x1z2 + x2z1)(y1z2 + y2z1)
    val m42 = multiply(a30, s30) // (y1y2 + 3z1z2)(y1y2 - 3z1z2
    val m43 = multiply(tri30, tri31) // 9(x1x2)(x1z2 + x2z1)
    val m44 = multiply(a30, s22) // (y1y2 + 3z1z2)(y1z2 + y2z1)
    val m45 = multiply(tri30, s20) // 3x1x2(x1y2 + x2y1)

    logger.info("stage 4 done")

    // stage 5
    val s50 = subtract(m40, m41)
    val a50 = add(m42, m43)
    val a51 = add(m44, m45)

    logger.info("stage 5 done")

    x3 := s50
    y3 := a50
    z3 := a51

    graph
  }

}
