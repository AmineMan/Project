
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

import scalar.Scalar;

import domain.{IterableDomain,IndexDomain};

/**
 * Implementation trait for a Vector view of a slice of keys from a Tensor.
 *
 * @author dramage
 */
trait VectorSliceLike
[@specialized(Int,Long) K, +D<:IterableDomain[K],
 @specialized(Int,Long,Float,Double,Boolean) V,
 +Coll<:Tensor[K,V],
 +This<:VectorSlice[K,V,Coll]]
extends Tensor1SliceLike[K, D, Int, IndexDomain, V, Coll, This]
with VectorLike[V, This];

/**
 * A Vector view of a slice of keys from a Tensor.
 *
 * @author dramage
 */
trait VectorSlice
[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double,Boolean) V,
 +Coll<:Tensor[K, V]]
extends Tensor1Slice[K,Int,V,Coll] with Vector[V]
with VectorSliceLike[K, IterableDomain[K], V, Coll, VectorSlice[K, V, Coll]];

object VectorSlice {
  abstract class FromKeySeq[K, V, +Coll <: Tensor[K, V]]
  (override val underlying : Coll, keys : Seq[K])
  (implicit override val scalar : Scalar[V])
  extends VectorSlice[K, V, Coll] {
    override def lookup(key : Int) = {
      checkKey(key);
      keys(key);
    }
    override def length = keys.size;
  }
}

/**
 * Implementation trait for a VectorRow view of a slice of keys from a Tensor.
 *
 * @author dramage
 */
trait VectorRowSliceLike
[@specialized(Int,Long) K, +D<:IterableDomain[K],
 @specialized(Int,Long,Float,Double,Boolean) V,
 +Coll<:Tensor[K,V],
 +This<:VectorRowSlice[K,V,Coll]]
extends VectorSliceLike[K, D, V, Coll, This]
with VectorRowLike[V, This];

/**
 * A VectorRow view of a slice of keys from a Tensor.
 *
 * @author dramage
 */
trait VectorRowSlice
[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double,Boolean) V,
 +Coll<:Tensor[K, V]]
extends VectorSlice[K,V,Coll] with VectorRow[V]
with VectorRowSliceLike[K, IterableDomain[K], V, Coll, VectorRowSlice[K, V, Coll]];

object VectorRowSlice {
  class FromKeySeq[K, V:Scalar, +Coll<:Tensor[K, V]](underlying : Coll, keys : Seq[K])
  extends VectorSlice.FromKeySeq[K,V,Coll](underlying, keys)
  with VectorRowSlice[K,V,Coll] {
    override def toDense = {
      val rv = dense.DenseVectorRow.zeros[V](length);
      rv := repr;
      rv;
    }
  }
}

/**
 * Implementation trait for a VectorCol view of a slice of keys from a Tensor.
 *
 * @author dramage
 */
trait VectorColSliceLike
[@specialized(Int,Long) K, +D<:IterableDomain[K],
 @specialized(Int,Long,Float,Double,Boolean) V,
 +Coll<:Tensor[K,V],
 +This<:VectorColSlice[K,V,Coll]]
extends VectorSliceLike[K, D, V, Coll, This]
with VectorColLike[V, This];

/**
 * A VectorCol view of a slice of keys from a Tensor.
 *
 * @author dramage
 */
trait VectorColSlice
[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double,Boolean) V,
 +Coll<:Tensor[K, V]]
extends VectorSlice[K,V,Coll] with VectorCol[V]
with VectorColSliceLike[K, IterableDomain[K], V, Coll, VectorColSlice[K, V, Coll]];

object VectorColSlice {
  class FromKeySeq[K, V:Scalar, +Coll<:Tensor[K, V]](underlying : Coll, keys : Seq[K])
  extends VectorSlice.FromKeySeq[K,V,Coll](underlying, keys)
  with VectorColSlice[K,V,Coll] {
    override def toDense = {
      val rv = dense.DenseVectorCol.zeros[V](length);
      rv := repr;
      rv;
    }
  }
}

