/*
 * Distributed as part of Scalala, a linear algebra library.
 *
 * Copyright (C) 2008- Daniel Ramage
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110 USA
 */
package scalala;
package tensor;
package mutable;

import domain.IndexDomain;

import scalala.scalar.Scalar;
import scalala.generic.collection._;
import scalala.operators._;

/**
 * Implementation trait for mutable VectorRow instances.
 *
 * @author dramage
 */
trait VectorRowLike[@specialized(Int,Long,Float,Double) V, +This<:VectorRow[V]]
extends tensor.VectorRowLike[V,This]
with Tensor1RowLike[Int,V,IndexDomain,This] with VectorLike[V,This] {
  override def t : VectorCol[V] =
    new VectorCol.View[V](repr);
}

/**
 * Mutable tensor.VectorRow.
 *
 * @author dramage
 */
trait VectorRow[@specialized(Int,Long,Float,Double) V]
extends tensor.VectorRow[V] with Tensor1Row[Int,V] with Vector[V]
with VectorRowLike[V,VectorRow[V]];

object VectorRow {
  def apply[V:Scalar](domain : IndexDomain) =
    dense.DenseVectorRow[V](domain);

  class View[V](override val inner : Vector[V])
  extends VectorProxy[V,Vector[V]] with tensor.VectorProxy[V,Vector[V]] with VectorRow[V] with VectorLike[V,View[V]] {
    override def repr : View[V] = this;
  }
}

