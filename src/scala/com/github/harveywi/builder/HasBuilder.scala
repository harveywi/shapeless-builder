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
  /**
   * Represents the isomorphism between `CC` and an `HList`.  Initialize it using the `createIsoContainer` helper method.
   */
  val isoContainer: IsoContainer[CC]

  /**
   * Represents the correspondence between the `Param[_]`/`OptParam[_]` objects and the constructor parameters for case class `CC`.
   * The ordering of the `Param[_]`/`OptParam[_]` objects must mimic the ordering of their corresponding constructor parameters.
   *
   * Initialize it using the `createFieldsContainer` helper method.
   */
  val fieldsContainer: FieldsContainer

  /**
   * Helper method for initializing the `isoContainer` field.
   */
  def createIsoContainer = new CreateIsoContainer

  /**
   * Exists only to help with the type inference of `createIsoContainer`.  Using this trick,
   * the HList type signature can be captures without having to manually specify it.
   */
  class CreateIsoContainer {
    /**
     * Captures the isomorphism between an HList and `CC` when the constructor for `CC` takes a single argument.
     *
     * @param apply the `apply` method for case class `CC`
     * @param unapply the `unapply` method for case class `CC`
     * @return An `IsoContainer` representing the isomorphism between `CC` and a suitably-typed `HList`
     */
    def apply[T](apply: T => CC, unapply: CC => Option[T]) = new IsoContainer[CC] {
      type L = T :: HNil
      val iso = Iso.hlist(apply, unapply)
    }

    /**
     * Captures the isomorphism between an HList and `CC` when the constructor for `CC` takes multiple arguments.
     *
     * @param apply the `apply` method for case class `CC`
     * @param unapply the `unapply` method for case class `CC`
     * @return An `IsoContainer` representing the isomorphism between `CC` and a suitably-typed `HList`
     */
    def apply[C, T <: Product, L1 <: HList](apply: C, unapply: CC => Option[T])(implicit fhl: FnHListerAux[C, L1 => CC], hl: HListerAux[T, L1]) = new IsoContainer[CC] {
      type L = L1
      val iso = Iso.hlist(apply, unapply)
    }

    /**
     * Captures the isomorphism between an HList and `CC` when the constructor for `CC` takes a single argument.
     *
     * Sometimes the compiler is not sure which of the `CreateIsoContainer.apply` methods should be used (e.g. when the `CC`
     * constructor has a single parameter, but it is a subtype of `Product`).  One way around this is to populate the type
     * parameter to the `apply[T]` method.  Another way is to just use this `apply1` method.
     *
     * @param apply the `apply` method for case class `CC`
     * @param unapply the `unapply` method for case class `CC`
     * @return An `IsoContainer` representing the isomorphism between `CC` and a suitably-typed `HList`
     */
    def apply1[T](app: T => CC, unapp: CC => Option[T]) = apply[T](app, unapp)
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
    def fields = fieldsIn
  }

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
    def set[V, K, W, Out <: HList](key: K, value: V)(implicit ev1: K <:< Param[W], ev2: V <:< W, selector: Selector[L, K],
      replacer: ReplacerAux[L, K, PParam[W], Out]): Builder[Out] = {
      val newValue = new PParam[W](value)
      val (oldParam, newParams) = replacer(fields, newValue)
      new Builder(newParams)
    }

    /**
     * Generates a new instance of `CC` with constructor parameters populated according to the `set` invocations.  If required `Param[_]` values
     * are missing, then attempting to call this method will result in a compiler error.
     *
     * @return an instance of `CC` designed according to the `Param[_]`/`OptParam[_]` values
     */
    def build[CL <: HList, T <: HList, U <: HList]()(implicit lubConstraint: LUBConstraint[L, PParam[_]],
      constMapper: ConstMapperAux[L, L, CL],
      transposer: TransposerAux[L :: CL :: HNil, T],
      ma: MapperAux[Tuples.tupled.type, T, U],
      mb: MapperAux[GetParamValue.type, U, isoContainer.L]): CC = {
      val fieldzes = fields.mapConst(fields)
      val trans = (fields :: fieldzes :: HNil).transpose
      val zipped = trans.map(Tuples.tupled)

      val hlist = zipped.map(GetParamValue)
      isoContainer.iso.from(hlist)
    }
  }

  /**
   * Polymorphic function which extracts the value from a populated parameter.
   */
  object GetParamValue extends Poly1 {
    implicit def caseOptParam[V, W, L <: HList](implicit selector: Selector[L, W], ev: W <:< OptParam[V]) = at[(W, L)] {
      case (param, fields) =>
        fields.select[W].value
    }

    implicit def casePParam[V, L <: HList](implicit selector: Selector[L, PParam[V]]) = at[(PParam[V], L)] {
      case (param, fields) =>
        fields.select[PParam[V]].value
    }
  }
}
