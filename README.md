shapeless-builder
=======================

shapeless-builder is a [Scala](http://www.scala-lang.org) library which
takes advantage of [Miles Sabin's](https://github.com/milessabin)
[shapeless](https://github.com/milessabin/shapeless) library to endow case classes
with method-chaining builders (essentially the builder design pattern).  These
builders are type-safe and purely functional.  The Scala type system is used to ensure that
code attempting to build incomplete objects will not compile.

This work was inspired by a [blog post](http://blog.rafaelferreira.net/2008/07/type-safe-builder-pattern-in-scala.html) 
by [Rafael Ferreira](http://blog.rafaelferreira.net/).  Thanks Rafael!

The isomorphism between case classes and HLists and generous use of implicits
are the driving forces behind shapeless-builder.

This library is one part in a trilogy ([I. shapeless-serialization](https://github.com/harveywi/shapeless-serialization), 
[II. shapeless-builder](), III. shapeless-commandline (coming soon!)) of shapeless-based libraries that I recently cooked up
to both deepen my understanding of Scala and to scratch some technical itches.
I hope you find it useful and interesting!

Example 1
--------------------------------

```scala
// Define a case class
case class Foo(x: Int, y: String, z: Char)

// Mix the HasBuilder trait in with its companion object
object Foo extends HasBuilder[Foo] {
  // Establish the case class <=> HList isomorphism
  val isoContainer = createIsoContainer(apply _, unapply _)

  // Define objects corresponding to the case class constructor parameters:
  // X is a required parameter of type Int
  object X extends Param[Int]

  // Y is an optional parameter of type String with default value "5"
  object Y extends OptParam[String]("5")

  // Z is an optional parameter of type Char with default value '5'
  object Z extends OptParam[Char]('5')

  // Define the "fieldsContainer" by passing in an HList of the above objects.  The order of the
  // objects in the HList must correspond to the order of the case class constructor parameters.
  val fieldsContainer = createFieldsContainer(X :: Y :: Z :: HNil)
}

// [...]

// Now you can create instances of the case class by using method-chaining builder incantations
import Foo._
val foo = Foo.builder.set(X, 42).set(Z, '#').build()

// Yessssssss!
assert(foo == Foo(42, "5", '#'), "Nooooooooooo!")
```

Example 2
--------------------------------

```scala
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
    val isoContainer = createIsoContainer(apply _, unapply _)
    // Establish Param[_] <=> constructor parameter correspondence
    val fieldsContainer = createFieldsContainer(Brand :: Mode :: IsDouble :: Glass :: HNil)
    // That's all!
  }

  def main(args: Array[String]): Unit = {
    import OrderOfScotch._

    val order1 = OrderOfScotch.builder.set(Brand, "Takes").set(IsDouble, true).
      set(Glass, Some(Tall)).set(Mode, OnTheRocks).build()

    assert(order1 == OrderOfScotch("Takes", OnTheRocks, true, Some(Tall)),
      "Time to get out the scotch...")
  }
}
```

For more examples, see the test specifications [here](https://github.com/harveywi/shapeless-builder/tree/master/src/test/com/github/harveywi/builder).

Prerequisites
--------------------------------
This library requires Scala 2.12 and shapeless 2.3.3.

Scaladoc
--------------------------------
Scaladoc is available [here](http://www.aylasoftware.org/shapeless-builder/).

### Questions?  Comments?  Bugs?
Feel free to contact me (harveywi at cse dot ohio-state dot edu).  Thanks!

