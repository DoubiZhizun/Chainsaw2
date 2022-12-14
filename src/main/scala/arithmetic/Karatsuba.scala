//package org.datenlord
//package arithmetic
//
//import dfg.{RingDag, RingPort}
//
//object Karatsuba {
//
//  def apply(width: Int, shift: Int, mode: MultiplierType, noWidthGrowth: Boolean = true) = {
//
//    val useCompressorTree = true
//
//    val baseWidth = 32
//    val stageRule = (x: Int) => if (noWidthGrowth) 2 * x else 2 * (x - 1)
//    logger.info(s"stages: ${Seq.iterate(baseWidth, 10)(stageRule).mkString(" ")} ")
//
//    def getSplit(width: Int) = {
//      val bound = Seq.iterate(baseWidth, 10)(stageRule).filter(_ < width).last
//      if (noWidthGrowth) bound else bound - 1
//    }
//
//    val golden = mode match {
//      case FullMultiplier => (data: Seq[BigInt]) => Seq(data.product)
//      case LsbMultiplier => (data: Seq[BigInt]) => Seq(data.product % (BigInt(1) << width))
//      case SquareMultiplier => (data: Seq[BigInt]) => Seq(data.head * data.head)
//    }
//
//    implicit val graph: RingDag = new RingDag(s"karatsubaGraph_$mode", golden)
//    val x = graph.addInput("bigMultX", width)
//    val y = if (mode != SquareMultiplier) graph.addInput("bigMultY", width) else null
//    val widthOut = mode match {
//      case LsbMultiplier => width
//      case _ => width * 2
//    }
//
//    def recursiveTask(width: Int, x: RingPort, y: RingPort, mode: MultiplierType): RingPort = {
//
//      val ret = if (width <= baseWidth) x.mult(y, mode)
//      else {
//        val lowWidth = getSplit(width)
//        val crossWidth = lowWidth
//        val highWidth = width - lowWidth
//        val doubleWidth = lowWidth * 2
//
//        require(x.width == width, s"${x.width} != $width")
//        if (mode != SquareMultiplier) require(y.width == width, s"${y.width} != $width")
//        val (xHigh, xLow) = x.splitAt(lowWidth)
//        val (yHigh, yLow) = if (mode != SquareMultiplier) y.splitAt(lowWidth) else (null, null)
//
//        mode match {
//          case FullMultiplier =>
//            val aPlusB = xHigh +^ xLow
//            val cPlusD = yHigh +^ yLow
//            val ac = recursiveTask(highWidth, xHigh, yHigh, FullMultiplier)
//            val bd = recursiveTask(lowWidth, xLow, yLow, FullMultiplier)
//            val all =
//              if (!noWidthGrowth) recursiveTask(crossWidth + 1, aPlusB, cPlusD, FullMultiplier)
//              else {
//                val (abMsb, abMain) = aPlusB.splitAt(crossWidth)
//                val (cdMsb, cdMain) = cPlusD.splitAt(crossWidth)
//
//                val abMuxed = abMain muxBy cdMsb
//                val cdMuxed = cdMain muxBy abMsb
//                val allTop = abMsb & cdMsb
//
//                val allMain = recursiveTask(crossWidth, abMain, cdMain, FullMultiplier)
//
//                // combine four parts of all
//                if (!useCompressorTree) {
//                  val (allMainHigh, allMainLow) = allMain.splitAt(lowWidth)
//                  val muxedSum = abMuxed +^ cdMuxed
//                  val allHighTemp = muxedSum +^ allMainHigh
//                  val (allHigh, allMid) = allHighTemp.splitAt(lowWidth)
//                  (allHigh +^ allTop) @@ (allMid @@ allMainLow)
//                }
//                else allMain +^ ((abMuxed +^ cdMuxed) << lowWidth) + (allTop << doubleWidth)
//              }
//
//            if (!useCompressorTree) {
//              val adbc = all - ac - bd
//              val full = ac @@ bd
//              val (high, low) = full.splitAt(lowWidth)
//              val highSum = high +^ adbc
//              highSum @@ low
//            }
//            else {
//              val adbc = all - ac - bd
//              bd +^ (adbc << lowWidth) + (ac << doubleWidth)
//            }
//
//          case LSB =>
//            val bd = recursiveTask(lowWidth, xLow, yLow, FULL)
//            val cb = recursiveTask(crossWidth, xLow.resize(crossWidth), yHigh.resize(crossWidth), LSB)
//            val ad = recursiveTask(crossWidth, xHigh.resize(crossWidth), yLow.resize(crossWidth), LSB)
//            if (!useCompressorTree) {
//              val partial = cb.resize(crossWidth) +:+ ad.resize(crossWidth)
//              if (lowWidth >= bd.width) logger.warn(s"problem: $lowWidth >= ${bd.width}")
//              val (high, low) = bd.splitAt(lowWidth)
//              val highSum = high +:+ partial
//              highSum @@ low
//            }
//            else bd + ((ad + cb) << lowWidth)
//
//          case SQUARE =>
//            val bd = recursiveTask(lowWidth, xLow, xLow, SQUARE)
//            val cb = recursiveTask(crossWidth, xHigh.resize(crossWidth), xLow.resize(crossWidth), FULL)
//            val ac = recursiveTask(highWidth, xHigh, xHigh, SQUARE)
//
//            if (!useCompressorTree) {
//              val full = ac @@ bd
//              if (lowWidth + 1 >= full.width) logger.warn(s"problem: ${lowWidth + 1} >= ${full.width}")
//              val (high, low) = full.splitAt(lowWidth + 1)
//              val highSum = high +:+ cb
//              highSum @@ low
//            }
//            else bd +^ (cb << (lowWidth + 1)) + (ac << doubleWidth)
//        }
//      }
//      ret.resize(if (mode == LSB) width else width * 2)
//    }
//
//    val ret: RingPort = recursiveTask(width, x, y, mode)
//    val z = graph.addOutput(s"bigMultZ_$mode", widthOut)
//    graph.addEdge(ret, z)
//    logger.info(s"$mode mult graph built")
//    graph
//  }
//
//}
