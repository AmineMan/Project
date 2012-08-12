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

import domain._;
import generic.TensorBuilder;

import operators.{BinaryOp, OpAdd, OpMul}
import scalala.scalar.Scalar;
import scalala.generic.collection._;


/**
 * Implementation trait for a one-axis tensor supports methods like norm.
 *
 * @author dramage
 */
trait Tensor1Like
[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double) V,
 +D<:Domain1[K], +This<:Tensor1[K,V]]
extends TensorLike[K,V,D,This] { self =>

  /**
   * Constructs a new Tensor1 like this one based on accumulating values
   * from the given initial start value, like a foldLeft that returns all
   * intermediate results.
   */
  def accumulate[Z,TT>:This,That](z : Z)(op : (Z,V) => Z)
  (implicit zs : Scalar[Z], bf : CanBuildTensorFrom[TT,D,K,Z,That]) = {
    val builder = bf(repr, domain);
    var acc = z;
    this.foreachPair((k,v) => {
      acc = op(acc,v);
      builder(k) = acc;
    });
    builder.result;
  }

  /**
   * Returns the culumulative sum of this Tensor1.  Note that this is only
   * well defined for domains that have a natural and consistent iteration
   * order.
   */
  def cumsum[TT>:This,That](implicit bf : CanBuildTensorFrom[TT,D,K,V,That], add : BinaryOp[V,V,OpAdd,V]) : That =
    accumulate(scalar.zero)(add);

  /**
   * Returns the culumulative sum of this Tensor1.  Note that this is only
   * well defined for domains that have a natural and consistent iteration
   * order.
   */
  def cumprod[TT>:This,That](implicit bf : CanBuildTensorFrom[TT,D,K,V,That], mul : BinaryOp[V,V,OpMul,V]) : That =
    accumulate(scalar.one)(mul);

  /** Returns the k-norm of this tensor. */
  def norm(n : Double) : Double = {
    if (n == 1) {
      var sum = 0.0;
      foreachNonZeroValue(v => sum += scalar.norm(v));
      return sum;
    } else if (n == 2) {
      var sum = 0.0;
      foreachNonZeroValue(v => { val nn = scalar.norm(v); sum += nn * nn });
      return math.sqrt(sum);
    } else if (n == Double.PositiveInfinity) {
      var max = Double.NegativeInfinity;
      foreachNonZeroValue(v => { val nn = scalar.norm(v); if (nn > max) max = nn; });
      return max;
    } else {
      var sum = 0.0;
      foreachNonZeroValue(v => { val nn = scalar.norm(v); sum += math.pow(nn,n); });
      return math.pow(sum, 1.0 / n);
    }
  }

  override protected def canEqual(other : Any) : Boolean = other match {
    case that : Tensor1[_,_] => true;
    case _ => false;
  }
}

/**
 * One-axis tensor supports methods like norm
 * and inner products (dot) with other one-axis tensors.
 *
 * @author dramage
 */
trait Tensor1[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double) V]
extends Tensor[K,V] with Tensor1Like[K,V,Domain1[K],Tensor1[K,V]];

object Tensor1 {
  /** Constructs a tensor for the given domain. */
  def apply[K,V:Scalar](domain : Domain1[K]) : Tensor[K,V] =
    mutable.Tensor1.apply[K,V](domain);
}

