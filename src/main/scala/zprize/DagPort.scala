package org.datenlord
package zprize

import org.datenlord
import org.datenlord.Direction
import org.datenlord.zprize.DagVertex

case class DagPort(vertex: DagVertex, order: Int, direction: Direction) {

  // TODO: more utils for connection
  def :=(that: DagPort)(implicit ref: Dag): Unit = ref.addEdge(that, this)

  def relativeTime = direction match {
    case In => vertex.gen.actualInTimes(order)
    case Out => vertex.gen.actualOutTimes(order) + vertex.gen.latency
  }

  def width = direction match {
    case In => vertex.gen.inputWidths(order)
    case Out => vertex.gen.outputWidths(order)
  }

  def target(implicit ref: Dag) = direction match {
    case In => vertex
    case Out => vertex.targets.apply(order)
  }

}
