package com.github.harveywi.builder.examples

import com.github.harveywi.builder.HasBuilder
import shapeless._

/**
 * Example demonstrating the builder pattern for a shot of scotch (see
 * http://blog.rafaelferreira.net/2008/07/type-safe-builder-pattern-in-scala.html).
 *
 * Huge thanks to Rafael Ferreira for providing this use case!
 *
 * @author William Harvey
 */
object OrderOfScotch {

  sealed abstract class Preparation
  case object Neat extends Preparation
  case object OnTheRocks extends Preparation
  case object WithWater extends Preparation

  sealed abstract class Glass
  case object Short extends Glass
  case object Tall extends Glass
  case object Tulip extends Glass

  case class OrderOfScotch(
    brand: String,
    mode: Preparation,
    isDouble: Boolean,
    glass: Option[Glass])
  object OrderOfScotch extends HasBuilder[OrderOfScotch] {
    object Brand extends Param[String]
    object Mode extends Param[Preparation]
    object IsDouble extends Param[Boolean]
    object Glass extends OptParam[Option[Glass]](None)

    // Establish HList <=> OrderOfScotch isomorphism
    val gen = Generic[OrderOfScotch]
    // Establish Param[_] <=> constructor parameter correspondence
    val fieldsContainer = createFieldsContainer(Brand :: Mode :: IsDouble :: Glass :: HNil)
    // That's all!
  }

  def main(args: Array[String]): Unit = {
    import OrderOfScotch._

    val order1 = OrderOfScotch.builder.set(Brand, "Takes").set(IsDouble, true).
      set(Glass, Some(Tall)).set(Mode, OnTheRocks).build()

    // Point-free version of the above
    val order2 = (OrderOfScotch.builder
      set (Brand, "Takes")
      set (IsDouble, true)
      set (Glass, Some(Tall))
      set (Mode, OnTheRocks)
      build ())

    assert(order1 == OrderOfScotch("Takes", OnTheRocks, isDouble = true, Some(Tall)),
      "Time to get out the scotch...")

    assert(order1 == order2, "Traditional and point-free build results should be identical")
  }
}