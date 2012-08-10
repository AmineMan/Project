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
package generic;

import domain.CanGetDomain;
import scalala.generic.collection._
import scala.collection.generic.CanBuildFrom
import operators.HasValuesMonadic

/**
 * For comprehensions on pairs of values from an underlying tensor.  This
 * class can be implicitly viewed as a Map[K,V].
 *
 * @author dramage
 */
trait TensorPairsMonadic
[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double) V,
 +This<:Tensor[K,V]] {

  /** Underlying tensor. */
  def repr : This;
  
  /** Calls repr.foreachPair. */
  def foreach[U](fn : ((K,V)) => U) =
    repr.foreachPair((k,v) => fn((k,v)));

  /** Calls repr.size. */
  def size =
    repr.size;
  
  /** Calls repr.mapPairs. */
  def map[TT>:This,O,That](fn : ((K,V)) => O)
  (implicit bf : CanMapKeyValuePairs[TT, K, V, O, That]) : That =
    repr.mapPairs((k,v) => fn((k,v)))(bf.asInstanceOf[CanMapKeyValuePairs[Tensor[K,V],K,V,O,That]]);

  /** Calls repr.pairsIterator. */
  def iterator =
    repr.pairsIterator;

  /** Constructs a filtered view of this tensor. */
  def filter(p : ((K,V)) => Boolean) =
    withFilter(p);
  
  /** Constructs a filtered view of this tensor. */
  def withFilter(p : ((K,V)) => Boolean) =
    new TensorPairsMonadic.Filtered[K,V,This](repr, p);
}

object TensorPairsMonadic {
  /** Filtered view of the pairs in a Tensor.  Does not support map. */
  class Filtered
  [@specialized(Int,Long) K, @specialized(Int,Long,Float,Double) V, +This<:Tensor[K,V]]
  (val repr : This, p : ((K,V))=>Boolean) {
    def foreach[U](fn : ((K,V)) => U) =
      repr.foreachPair((k,v) => if (p((k,v))) fn((k,v)));
    
    def withFilter(q : ((K,V)) => Boolean) =
      new Filtered[K,V,This](repr, tup => p(tup) && q(tup));

     /** Calls repr.mapPairs. */
    def map[O,That](fn : ((K,V)) => O)
                   (implicit bf : CanBuildFrom[Iterable[(K,V)],O,That]) : That = {
      val b = bf();
      repr.foreachPair( (k,v) =>
        if(p(k -> v)) b += fn(k->v)
      )
      b.result;
    }
  }
  
  implicit def asMap[K,V,T<:Tensor[K,V]](pairs : TensorPairsMonadic[K,V,T]) = {
    new scala.collection.Map[K,V] {
      def self = pairs.repr;
      override def foreach[U](fn : ((K,V)) => U) = pairs.foreach(fn);
      override def keysIterator = self.keysIterator;
      override def valuesIterator = self.valuesIterator;
      override def contains(key : K) = self.isDefinedAt(key);
      override def apply(key : K) = self.apply(key);
      override def iterator = self.pairsIterator;
      override def get(key : K) =
        if (self.isDefinedAt(key)) Some(self.apply(key)) else None;
      override def - (key : K) =
        this.toMap.-(key);
      override def + [V1>:V](kv : (K,V1)): scala.collection.Map[K,V1] =
        this.toMap.+(kv);
    }
  }
}

