//package org.datenlord
//package dfg
//
//import org.scalatest.flatspec.AnyFlatSpec
//
//
//class ArithmeticGraphsSynthTest extends AnyFlatSpec {
//
//  val testWidth = 377
//  val zprizeModulus = algos.ZPrizeMSM.baseModulus
//
//  def graphAdd = ArithmeticGraphs.addGraph(testWidth)
//
//  def graphSub = ArithmeticGraphs.subGraph(testWidth)
//
//  def graphFull0 = ArithmeticGraphs.karatsubaGraph(testWidth, FULL, noWidthGrowth = false)
//
//  def graphFull1 = ArithmeticGraphs.karatsubaGraph(testWidth, FULL, noWidthGrowth = true)
//
//  def graphLow0 = ArithmeticGraphs.karatsubaGraph(testWidth, LSB, false)
//
//  def graphLow1 = ArithmeticGraphs.karatsubaGraph(testWidth, LSB, true)
//
//  def graphSquare0 = ArithmeticGraphs.karatsubaGraph(testWidth, SQUARE, false)
//
//  def graphSquare1 = ArithmeticGraphs.karatsubaGraph(testWidth, SQUARE, true)
//
//  def graphMontMult = ArithmeticGraphs.montgomeryGraph(testWidth, zprizeModulus, false, false)
//
//  def graphMontSquare = ArithmeticGraphs.montgomeryGraph(testWidth, zprizeModulus, true, false)
//
//  behavior of "arithmetic graphs"
//
//  //  it should "synth for full with width growth" in VivadoSynth(graphFull0.toTransform, "graphFull0")
//  ignore should "synth for full without width growth" in VivadoSynth(graphFull1.toTransform, "graphFull1")
//  ignore should "impl for full without width growth" in VivadoImpl(graphFull1.toTransform, "graphFull1")
//  //  it should "synth for low with width growth" in VivadoSynth(graphLow0.toTransform, "graphLow0")
//  //  it should "paint for low with width growth" in graphLow0.validate().toPng("graphLow0")
//  it should "paint for low with width growth" in graphLow0.toPng("graphLow0")
//  it should "impl for low with width growth" in VivadoImpl(graphLow0.toTransform, "graphLow0")
//  ignore should "synth for low without width growth" in VivadoSynth(graphLow1.toTransform, "graphLow1")
//  ignore should "impl for low without width growth" in VivadoImpl(graphLow1.toTransform, "graphLow1")
//  //  it should "synth for square with width growth" in VivadoSynth(graphSquare0.toTransform, "graphSquare0")
//  ignore should "synth for square without width growth" in VivadoSynth(graphSquare1.toTransform, "graphSquare1")
//  ignore should "impl for square without width growth" in VivadoImpl(graphSquare1.toTransform, "graphSquare1")
//  ignore should "synth for mont mult" in VivadoSynth(graphMontMult.toTransform, "graphModularMult")
//  ignore should "synth for mont square" in VivadoSynth(graphMontSquare.toTransform, "graphModularSquare")
//
//}
