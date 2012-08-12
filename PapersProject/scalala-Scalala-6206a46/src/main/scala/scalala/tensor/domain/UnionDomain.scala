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
package domain;

/**
 * Trait that represents the union of two domains.
 *
 * @author dramage
 */
trait UnionDomainLike[@specialized(Int,Long) A, +This<:IterableDomain[A]]
extends IterableDomainLike[A, This] {
  def a : IterableDomain[A];
  def b : IterableDomain[A];
  
  override def size = {
    var s = 0;
    foreach(k => { s += 1; })
    s
  }

  override def foreach[O](fn : A=>O) = {
    a.foreach(fn);
    b.foreach(k => if (!a.contains(k)) fn(k));
  }

  override def iterator =
    a.iterator ++ b.iterator.filterNot(a.contains);

  override def contains(key : A) : Boolean =
    a.contains(key) || b.contains(key);

  override def equals(other : Any) = other match {
    case that : UnionDomainLike[_,_] => this.a == that.a && this.b == that.b;
    case _ => super.equals(other);
  }
}

