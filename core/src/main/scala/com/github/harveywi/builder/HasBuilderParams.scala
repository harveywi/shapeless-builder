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

/**
 * Contains types representing the required and optional parameters needed to manufacture
 * instances of case classes using the builder pattern.
 * 
 * @author William Harvey
 */
trait HasBuilderParams {self: HasBuilder[_] =>
  /**
   * A required parameter.
   */
  trait Param[+T]
  
  /**
   * A parameter whose value has been populated.
   */
  class PParam[+T](val value: T) extends Param[T]
  
  /**
   * An optional parameter.
   */
  abstract class OptParam[+T](override val value: T) extends PParam[T](value)
}