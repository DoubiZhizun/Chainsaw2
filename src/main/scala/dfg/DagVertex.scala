package org.datenlord
package dfg

import spinal.core.Data
import scala.collection.JavaConversions._

class DagVertex[THard <: Data](var name: String,
                               val latency: Int,
                               val opType: OperatorType,
                               val implH: Seq[THard] => Seq[THard] = (data: Seq[THard]) => data
                              ) {

  //  def apply(portOrder: Int) = DagPort(this, portOrder)

  def in(portOrder: Int) = DagPort(this, portOrder, In)

  def out(portOrder: Int) = DagPort(this, portOrder, Out)

  def inDegree(implicit ref: Dag[THard]) = ref.inDegreeOf(this)

  def outDegree(implicit ref: Dag[THard]) = ref.outDegreeOf(this)

  def incomingEdges(implicit ref: Dag[THard]) =
    ref.incomingEdgesOf(this).toSeq

  def sources(implicit ref: Dag[THard]) =
    this.incomingEdges.sortBy(_.inOrder)
      .map(ref.getEdgeSource)

  def sourcePorts(implicit ref: Dag[THard]) =
    this.incomingEdges.sortBy(_.inOrder)
      .map(e => DagPort(ref.getEdgeSource(e), e.outOrder, Out))

  def outgoingEdges(implicit ref: Dag[THard]) =
    ref.outgoingEdgesOf(this).toSeq

  def targets(implicit ref: Dag[THard]) =
    this.outgoingEdges.sortBy(_.outOrder)
      .map(ref.getEdgeTarget)

  def targetPorts(implicit ref: Dag[THard]) =
    ref.outgoingEdgesOf(this)
      .toSeq.sortBy(_.outOrder)
      .map(e => DagPort(ref.getEdgeTarget(e), e.inOrder, In))

  def isIo(implicit ref: Dag[THard]) = ref.inputs.contains(this) || ref.outputs.contains(this)

  override def clone(): DagVertex[THard] = new DagVertex[THard](name, latency, opType, implH)

  def setName(name:String) = this.name = name
}
