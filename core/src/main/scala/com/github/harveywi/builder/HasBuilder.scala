/*
 *   shapeless-builder
 *   (c) William Harvey 2013
 *   harveywi@cse.ohio-state.edu
 *   
 *   This file is part of "shapeless-builder".
 *
 *   shapeless-builder is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   shapeless-builder is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with shapeless-builder.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.harveywi.builder

import shapeless._
import shapeless.ops.hlist._

/**
 * Enables method-chaining builders for case classes of type `CC`.
 *
 * @example {{{
 *
 * // Define a case class
 * case class Foo(x: Int, y: String, z: Char)
 *
 * // Mix the HasBuilder trait in with its companion object
 * object Foo extends HasBuilder[Foo] {
 *   // Establish the case class <=> HList isomorphism
 *   val isoContainer = createIsoContainer(apply _, unapply _)
 *
 *   // Define objects corresponding to the case class constructor parameters:
 *   // X is a required parameter of type Int
 *   object X extends Param[Int](5)
 *
 *   // Y is an optional parameter of type String with default value "5"
 *   object Y extends OptParam[String]("5")
 *
 *   // Z is an optional parameter of type Char with default value '5'
 *   object Z extends OptParam[Char]('5')
 *
 *   // Define the "fieldsContainer" by passing in an HList of the above objects.  The order of the
 *   // objects in the HList must correspond to the order of the case class constructor parameters.
 *   val fieldsContainer = createFieldsContainer(X :: Y :: Z :: HNil)
 * }
 *
 * // [...]
 *
 * // Now you can create instances of the case class by using method-chaining builder incantations
 * import Foo._
 * val test = Foo.builder.set(X, 42).set(Z, '#').build()
 *
 * // Yessssssss!
 * assert(foo == Foo(42, "5", '#'), "Nooooooooooo!")
 * }}}
 *
 * @author William Harvey
 */
trait HasBuilder[CC] extends HasBuilderParams {

  trait ParamValueExtractor[In <: HList, Out <: HList] {
    def apply(in: In): Out
  }
  
  object ParamValueExtractor {
    implicit def caseHNil: ParamValueExtractor[HNil, HNil] = {
      (HNil) => HNil
    }

    implicit def casePParam[T, O, L1 <: HList, L2 <: HList](
        implicit ev: O <:< PParam[T],
        tailExtractor: ParamValueExtractor[L1, L2]
     ): ParamValueExtractor[O :: L1, T :: L2] =  {
      (in: O :: L1) => in.head.value :: tailExtractor(in.tail)
    }
  }
  
  val gen: Generic[CC] {
    type Repr <: HList
  }
  
  /**
   * Establishes a correspondence between the `Param[_]`/`OptParam[_]` objects and the constructor parameters for `CC`.
   * The ordering of the `Param[_]`/`OptParam[_]` objects must mimic the ordering of their corresponding constructor parameters.
   *
   * The returned object is just a wrapper for an `HList`; using a wrapper allows us to tuck away the type signature of the `HList`
   * as a type variable.
   *
   * @param fieldsIn an HList of `Param[_]`/`OptParam[_]` objects ordered in accordance with the constructor parameters for `CC`
   * @return a `FieldsContainer` representing the constructor parameters for `CC`
   */
  def createFieldsContainer[L1 <: HList](fieldsIn: L1)(implicit lubConstraint: LUBConstraint[L1, Param[_]]) = new FieldsContainer {
    type L = L1
    def fields: L = fieldsIn
  }
  
  val fieldsContainer: FieldsContainer

  /**
   * Creates a new builder for instances of `CC`.
   */
  def builder = new Builder(fieldsContainer.fields)

  /**
   * A builder for instances of `CC`.
   */
  class Builder[L <: HList](val fields: L) {
    /**
     * Set the value of the constructor parameter `key` to the specified value `value`.  If you already suppled a value for parameter `key`,
     * then attempting to set a different value for `key` will result in a compiler error.
     *
     * @param key constructor parameter to set
     * @param value value used to inhabit the specified constructor parameter `key`
     * @return a new builder instance with the value of parameter `key` set to `value`
     */
    def set[V, K, T, Out <: HList](key: K, value: V)(implicit ev1: K <:< Param[T], ev2: V <:< T, selector: Selector[L, K],
      replacer: Replacer.Aux[L, K, PParam[V], (K, Out)]): Builder[Out] = {
      val newValue = new PParam[V](value)
      val newParams = replacer(fields, newValue)._2
      new Builder(newParams)
    }

    def build()
    (implicit
        paramValueExtractor: ParamValueExtractor[L, gen.Repr]
    )
    : CC = {
      val params = paramValueExtractor(fields)
      gen.from(params)
    }
  }
}
