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

import domain.IndexDomain;

/**
 * Implementation trait for proxies to a Vector instance.
 *
 * @author dramage
 */
trait VectorProxyLike[@specialized(Int,Long,Float,Double) V, Inner<:Vector[V], +This<:Vector[V]]
extends Tensor1ProxyLike[Int,V,IndexDomain,Inner,This] with VectorLike[V,This] {
  override def length = inner.length;
}

/**
 * Proxy to a Vector instance.
 *
 * @author dramage
 */
trait VectorProxy[@specialized(Int,Long,Float,Double) V, Inner<:Vector[V]]
extends Tensor1Proxy[Int,V,Inner] with Vector[V] with VectorProxyLike[V,Inner,VectorProxy[V,Inner]];
