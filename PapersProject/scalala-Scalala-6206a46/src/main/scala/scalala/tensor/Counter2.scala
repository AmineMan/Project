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
import generic.{TensorBuilder,TensorPairsMonadic};

import scalala.scalar.Scalar;
import scalala.generic.collection._
import tensor.Counter2.Curried
;

/**
 * A map-like tensor that acts like a collection of key-value pairs where
 * the set of values may grow arbitrarily.
 *
 * @author dlwh
 */
trait Counter2Like
[K1, @specialized(Int,Long) K2, @specialized(Int,Long,Float,Double) V,
 +M1[VV] <: Curried[scala.collection.Map,K1]#Result[VV],
 +T <: Counter[K2,V],
 +This<:Counter2[K1,K2,V]]
extends Tensor2Like[K1,K2,V,SetDomain[K1],SetDomain[K2],Domain2[K1,K2],Domain2[K2,K1],This]
// TODO: this hsould work instead extends CRSTensor2Like[K1,K2,V,M1,T,This]
//with TensorPairsMonadic[(K1,K2),V,This]
{ self =>

  override def newBuilder[NK,NV:Scalar](domain : IterableDomain[NK])
  : TensorBuilder[NK,NV,Tensor[NK,NV]] = domain match {
    case that : IndexDomain =>
      super.newBuilder(that)
    case that : Domain1[_] =>
      mutable.Counter(that)(implicitly[Scalar[NV]]).asBuilder;
    case that : Domain2[_,_] =>
      mutable.Counter2(that)(implicitly[Scalar[NV]]).asBuilder;
    case _ =>
      super.newBuilder(domain);
  }

  def data : M1[_<:T];

  override def size = {
    var s = 0;
    for (m <- data.valuesIterator) {
      s += m.size
    }
    s;
  }

  override def domain : Domain2[K1,K2] = {
    new Domain2[K1,K2] {
      def _1 = new SetDomain(data.keySet)
      def _2 = new SetDomain(data.values.flatMap(_.domain).toSet);
    }
  }

  override def apply(k : K1, k2: K2) = data.get(k).map(t => t(k2)) getOrElse scalar.zero;

  override def checkKey(k : K1, k2: K2) = ();
  
  override def checkDomain(d : scalala.tensor.domain.Domain[(K1,K2)]) = ();

  def contains(k: K1) = data.contains(k);

  def contains(k1: K1, k2: K2) = data.contains(k1) && data(k1).contains(k2)


  //
  // faster implementations
  //

  override def foreachKey[U](fn : ((K1,K2)) => U) : Unit =
    for((k1,m) <- data; k2 <- m.keys) fn(k1->k2);

  override def foreachValue[U](fn : V => U) : Unit =
    valuesIterator.foreach(fn);

  override def foreachTriple[U](fn : (K1,K2,V) => U) : Unit =
    triplesIterator.foreach(triple => fn(triple._1,triple._2,triple._3));

  override def keysIterator = for ((k1,m) <- data.iterator; k2 <- m.keysIterator) yield (k1,k2);

  override def valuesIterator = for (m <- data.valuesIterator; v <- m.valuesIterator) yield v

  override def pairsIterator = for ((k1,m) <- data.iterator; (k2,v) <- m.pairsIterator) yield (k1,k2)->v;

  override def triplesIterator = for ((k1,m) <- data.iterator; (k2,v) <- m.pairsIterator) yield (k1,k2,v);
}

trait Counter2
[K1, @specialized(Int,Long) K2, @specialized(Int,Long,Float,Double) V]
extends Tensor2[K1,K2,V] with Counter2Like[K1,K2,V,Curried[scala.collection.Map,K1]#Result,Counter[K2,V],Counter2[K1,K2,V]];

object Counter2 {
  def apply[K1,K2,V:Scalar]() : mutable.Counter2[K1,K2,V] =
    mutable.Counter2();
    
  def apply[K1,K2,V:Scalar](values : (K1,K2,V)*) : mutable.Counter2[K1,K2,V] =
    mutable.Counter2(values :_ *);
    
  def apply[K1,K2,V:Scalar](values : TraversableOnce[(K1,K2,V)]) : mutable.Counter2[K1,K2,V] =
    mutable.Counter2(values);

  def apply[K1,K2,V:Scalar](domain : Domain2[K1,K2]) =
    mutable.Counter2(domain);

  def count[K1,K2](items : TraversableOnce[(K1,K2)]) : mutable.Counter2[K1,K2,Int] =
    mutable.Counter2.count(items);

  /**
   * This is just a curried version of scala.collection.Map.
   * Used to get around Scala's lack of partially applied types.
   *
   * @author dlwh
   */
  trait Curried[M[_,_],K] {
    type Result[V] = M[K,V];
  }


  implicit def canSliceRow[K1,K2,V:Scalar] : CanSliceRow[Counter2[K1,K2,V],K1,Counter[K2,V]]
  = new CanSliceRow[Counter2[K1,K2,V],K1,Counter[K2,V]] {
    val vS = implicitly[Scalar[V]];
    override def apply(from : Counter2[K1,K2,V], row : K1) = from.data(row);
  }

  implicit def canSliceCol[K1,K2,V:Scalar]: CanSliceCol[Counter2[K1,K2,V],K2,Counter[K1,V]]
  = new CanSliceCol[Counter2[K1,K2,V],K2,Counter[K1,V]] {
    val vS = implicitly[Scalar[V]];
    def apply(from: Counter2[K1, K2, V], col: K2) = new Counter[K1,V] {
      val scalar = vS;
      def data = new MapSlice(from.data,col);
    }
  }

  import scala.collection.Map
  private class MapSlice[K1,K2,V](m: Map[K1,Counter[K2,V]],col: K2) extends Map[K1,V] {
    def get(key: K1) = m.get(key).flatMap(_.get(col))

    def iterator = for( (k,inner) <- m.iterator if inner.contains(col)) yield (k,inner(col))

    def +[B1 >: V](kv: (K1, B1)) = Map(kv) ++ this;

    def -(key: K1) = Map() ++ this - key
  }

//  implicit def Counter2CanSolveCounter[K1,K2,V]
//  extends BinaryOp[Counter2[K1,K2,V],Counter[K2,V],OpSolveMatrixBy,Counter[K1,V]] {
//    override def opType = OpSolveMatrixBy;
//    override def apply(A : Counter2[K1,K2,V], V : Counter[K2,V]) = {
//      val 
//    }
}

