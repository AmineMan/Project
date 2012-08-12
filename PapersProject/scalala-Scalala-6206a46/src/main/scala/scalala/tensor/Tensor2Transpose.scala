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

import domain.{Domain1,Domain2};

/**
 * Implementation trait for a transposed view of an underlying Tensor2.
 *
 * @author dramage
 */
trait Tensor2TransposeLike
[@specialized(Int) K2, @specialized(Int) K1,
 @specialized(Int,Long,Float,Double,Boolean) V,
 +D2<:Domain1[K2],
 +D1<:Domain1[K1],
 +T<:Domain2[K2,K1],
 +D<:Domain2[K1,K2],
 +Coll<:Tensor2[K1,K2,V],
 +This<:Tensor2Transpose[K2,K1,V,Coll]]
extends TensorSliceLike[(K1,K2),D,(K2,K1),T,V,Coll,This]
with Tensor2Like[K2,K1,V,D2,D1,T,D,This] {
self =>

  override def domain = underlying.domain.transpose.asInstanceOf[T];
  override def size = underlying.size;

  /* final */ override def lookup(tup : (K2,K1)) = tup.swap;

  override def apply(i : K2, j : K1) = underlying.apply(j, i);

  override def t : Coll =
    underlying;
}

/**
 * Transposed view of an underlying Tensor2.
 *
 * @author dramage
 */
trait Tensor2Transpose
[@specialized(Int) K2, @specialized(Int) K1,
 @specialized(Int,Long,Float,Double,Boolean) V,
 +Coll <: Tensor2[K1,K2,V]]
extends TensorSlice[(K1,K2),(K2,K1),V,Coll]
with Tensor2[K2,K1,V]
with Tensor2TransposeLike[K2,K1,V,Domain1[K2],Domain1[K1],Domain2[K2,K1],Domain2[K1,K2],Coll,Tensor2Transpose[K2,K1,V,Coll]];


object Tensor2Transpose {
  /** Default implementation. */
  class Impl[K2, K1, V, +Coll <: Tensor2[K1,K2,V]]
  (override val underlying : Coll)
  (override implicit val scalar : Scalar[V])
  extends Tensor2Transpose[K2,K1,V,Coll];
}

