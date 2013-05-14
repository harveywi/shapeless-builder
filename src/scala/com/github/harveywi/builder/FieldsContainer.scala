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
 * A container storing an `HList` of builder parameters (corresponding to case class constructor parameters/fields).  
 * This is sort of a hack which helps library users to avoid manually specifying the type signature of the HList,
 * which can become unruly in practice.
 * 
 * @author William Harvey
 */
trait FieldsContainer {
	type L <: HList
	def fields: L
}