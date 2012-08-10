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
package scalala.collection.sparse;

import org.scalacheck._
import org.scalatest._;
import org.scalatest.junit._;
import org.scalatest.prop._;
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class SparseArrayTest extends FunSuite with Checkers {
  test("Map") {
    val x = SparseArray(1,0,2,0,3,0,-1,-2,-3);
    x.compact;
    val y = x.map(_ + 1);
    assert(x.length === y.length);
    assert(x.activeLength === x.length - 3);
    assert(y.activeLength === y.length - 1);
    assert(y.toList === List(2,1,3,1,4,1,0,-1,-2));
  }
  
  test("Filter") {
    val x = SparseArray(1,0,2,0,3,0,-1,-2,-3);
    x.compact;
    assert(x.filter(_ % 2 == 1).toList === List(1,3));
    assert(x.filter(_ % 2 == 1).activeLength === 2);
    assert(x.filter(_ % 2 == 0).toList === List(0,2,0,0,-2));
    assert(x.filter(_ % 2 == 0).activeLength === 2);
    assert(x.filter(_ > 0).toList === List(1,2,3));
    assert(x.filter(_ > 0).activeLength === 3);
    assert(x.filter(_ >= 0).toList === List(1,0,2,0,3,0));
    assert(x.filter(_ >= 0).activeLength === 3);
    
    val y = SparseArray(0,1,0,0,-1,-2,-3,-5);
    y.compact;
    assert(y.filter(_ > 0).toList === List(1));
    assert(y.filter(_ >= 0).toList === List(0,1,0,0));
  }
}

