package org.datenlord
package zprize


import ilog.concert.IloNumVar
import ilog.cplex.IloCplex
import spinal.core._

import scala.collection.JavaConversions._
import scala.math.log


object Flatten {
  def apply(dag: Dag): Dag = {
    implicit val refDag: Dag = dag

    if (dag.vertexSet().exists(_.gen.isInstanceOf[Dag])) {
      dag.vertexSet().filter(_.gen.isInstanceOf[Dag])
        .map(_.gen) // squeeze out generators appear repeatedly
        .foreach(subGraph => Flatten(subGraph.asInstanceOf[Dag]))
    }

    var step = 0

    dag.vertexSet().filter(_.gen.isInstanceOf[Dag])
      .foreach { v =>
        step += 1
        dag.addGraphBetween(v.gen.asInstanceOf[Dag], v.sourcePorts, v.targetPorts)
        dag.removeVertex(v)
      }

    dag
  }
}
