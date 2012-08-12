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

import domain.{IterableDomain,Domain1,IndexDomain,CanGetDomain,CanBuildDomain2};
import generic.TensorBuilder;

import scalala.operators._;
import scalala.scalar.Scalar;
import scalala.generic.collection.CanBuildTensorFrom;

/**
 * Implementation trait for a one-axis tensor shaped as a column.
 *
 * @author dramage
 */
trait Tensor1ColLike
[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double) V,
 +D<:Domain1[K], +This<:Tensor1Col[K,V]]
extends Tensor1Like[K,V,D,This] with operators.ColOps[This] { self =>
  override def newBuilder[K2,V2:Scalar](domain : IterableDomain[K2]) = domain match {
    case that : IndexDomain =>
      mutable.Vector[V2](that).asBuilder;
    case that : Domain1[_] =>
      mutable.Tensor1Col[K2,V2](that).asBuilder;
    case _ =>
      super.newBuilder[K2,V2](domain);
  }
  
  def t : Tensor1Row[K,V] =
    new Tensor1Row.View[K,V](repr);
}

/**
 * One-axis tensor shaped as a column.
 *
 * @author dramage
 */
trait Tensor1Col[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double) V]
extends Tensor1[K,V] with Tensor1ColLike[K,V,Domain1[K],Tensor1Col[K,V]];

object Tensor1Col {
  class View[K,V](override val inner : Tensor1Row[K,V])
  extends Tensor1Proxy[K,V,Tensor1Row[K,V]] with Tensor1Col[K,V]
  with Tensor1Like[K,V,Domain1[K],View[K,V]] {
    override def repr : View[K,V] = this;
    override def t : Tensor1Row[K,V] = inner;
  }
  
  implicit def canMulTensor1ColByRow[K1,K2,V1,V2,RV,A,DA,B,DB,DThat,That]
  (implicit viewA : A=>Tensor1Col[K1,V1], viewB : B=>Tensor1Row[K2,V2],
   dA : CanGetDomain[A,DA], dB : CanGetDomain[B,DB],
   dThat : CanBuildDomain2[DA,DB,DThat],
   mul : BinaryOp[V1,V2,OpMul,RV],
   bf : CanBuildTensorFrom[A, DThat, (K1,K2), RV, That])
  : BinaryOp[A,B,OpMulColVectorBy,That]
  = new BinaryOp[A,B,OpMulColVectorBy,That] {
    override def opType = OpMulColVectorBy;
    override def apply(a : A, b : B) = {
      val builder = bf(a, dThat(a.domain.asInstanceOf[DA], b.domain.asInstanceOf[DB]));
      a.foreachNonZeroPair((i,va) => b.foreachNonZeroPair((j,vb) => builder((i,j)) = mul(va,vb)));
      builder.result;
    }
  }
}

