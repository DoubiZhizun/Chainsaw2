package org.datenlord
package examples

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.fsm._
import xilinx.VivadoReport

case class Addition(width: Int) extends Component {

  val a, b = in UInt (width bits)
  val c = out UInt (width + 1 bits)
  c := (a.d(1) +^ b.d(1)).d(1)

}

object Addition {





}
