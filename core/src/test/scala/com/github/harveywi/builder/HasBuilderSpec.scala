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

import org.scalatest._
import shapeless._

object HasBuilderSpec {
  // Simple single-parameter tests 
  case class TestInt(x: Int)
  object TestInt extends HasBuilder[TestInt] {
    val gen = Generic[TestInt]

    object X extends Param[Int]
    val fieldsContainer = createFieldsContainer(X :: HNil)
  }

  case class TestIntOptional(x: Int)
  object TestIntOptional extends HasBuilder[TestIntOptional] {
    val gen = Generic[TestIntOptional]

    object X extends OptParam[Int](8675309)
    val fieldsContainer = createFieldsContainer(X :: HNil)
  }

  // Multiple parameters test
  case class TestIntStringChar(x: Int, y: String, z: Char)
  object TestIntStringChar extends HasBuilder[TestIntStringChar] {
    val gen = Generic[TestIntStringChar]

    object X extends Param[Int]
    object Y extends Param[String]
    object Z extends Param[Char]
    val fieldsContainer = createFieldsContainer(X :: Y :: Z :: HNil)
  }

  // Multiple optional parameters
  case class TestIntStringCharOptional(x: Int, y: String, z: Char)
  object TestIntStringCharOptional extends HasBuilder[TestIntStringCharOptional] {
    val gen = Generic[TestIntStringCharOptional]

    object X extends OptParam[Int](5)
    object Y extends OptParam[String]("5")
    object Z extends OptParam[Char]('5')
    val fieldsContainer = createFieldsContainer(X :: Y :: Z :: HNil)
  }
  
  // Fun with Option[_]...
  case class TestOptionString(x: Option[String])
  object TestOptionString extends HasBuilder[TestOptionString] {
    val gen = Generic[TestOptionString]
    
    object X extends Param[Option[String]]
    val fieldsContainer = createFieldsContainer(X :: HNil)
  }
  
  case class TestOptionStringOptional(x: Option[String])
  object TestOptionStringOptional extends HasBuilder[TestOptionStringOptional] {
    val gen = Generic[TestOptionStringOptional]
    
    object X extends OptParam[Option[String]](None)
    val fieldsContainer = createFieldsContainer(X :: HNil)
  }
  
  // Some more fun with Either[_] and Option[_]
  case class TestEither(x: Either[Int, String], y: Option[Either[Int, String]])
  object TestEither extends HasBuilder[TestEither] {
    val gen = Generic[TestEither]
    
    object X extends Param[Either[Int, String]]
    object Y extends OptParam[Option[Either[Int, String]]](None)
    val fieldsContainer = createFieldsContainer(X :: Y :: HNil)
  }
}

class HasBuilderSpec extends FlatSpec with Matchers {
  import HasBuilderSpec._
  "When method chaining is not used, a builder for a case class with a single required parameter" should "generate the expected case class" in {
    val expected = TestInt(42)

    import TestInt._

    // Create a new builder for instances of TestInt
    val builder = TestInt.builder

    // The commented line below does not compile because the build() method needs a value for X
    // builder.build()

    // Set the value of parameter x
    val builderWithX = builder.set(X, 42)

    // The commented line below does not compile because, by design, you can only set each parameter value once
    // val builderWithXX = builderWithX.set(X, 100)

    // Once all the required arguments have been supplied, the build() method is ready to go
    val builderResult = builderWithX.build()

    builderResult should equal(expected)
  }

  "When method chaining is used, a builder for a case class with a single required parameter" should "generate the expected case class" in {
    import TestInt._
    val expected = TestInt(42)

    // Method chaining can be more succinct
    TestInt.builder.set(X, 42).build() should equal(expected)

    // Chaining using point-free style
    TestInt.builder set (X, 42) build () should equal(expected)
  }

  // Try out the optional parameters
  "A builder for a case class with a single optional parameter" should "correctly populate the default parameter value" in {
    val expected = TestIntOptional(8675309)
    TestIntOptional.builder.build() should equal(expected)
  }

  "A builder for a case class with a single optional parameter" should "correctly accept a user-specified parameter value" in {
    import TestIntOptional._
    val expected = TestIntOptional(42)
    TestIntOptional.builder.set(X, 42).build() should equal(expected)
  }

  // Test multiple (required) parameters
  "A builder for a case class with multiple required parameters" should "generate the expected case class" in {
    import TestIntStringChar._
    val expected = TestIntStringChar(42, "Peanuts", 'E')

    // Let's try all six permutations for fun
    val builder = TestIntStringChar.builder
    val x = 42
    val y = "Peanuts"
    val z = 'E'
    builder set (X, x) set (Y, y) set (Z, z) build () should equal(expected)
    builder set (X, x) set (Z, z) set (Y, y) build () should equal(expected)
    builder set (Y, y) set (X, x) set (Z, z) build () should equal(expected)
    builder set (Y, y) set (Z, z) set (X, x) build () should equal(expected)
    builder set (Z, z) set (X, x) set (Y, y) build () should equal(expected)
    builder set (Z, z) set (Y, y) set (X, x) build () should equal(expected)
  }

  // Test multiple optional parameters
  "A builder for a case class with multiple optional parameters" should "generate the expected case class" in {
    import TestIntStringCharOptional._

    val builder = TestIntStringCharOptional.builder

    // Default values
    val dx = 5
    val dy = "5"
    val dz = '5'

    // Non-default values
    val x = 42
    val y = "Peanuts"
    val z = '#'

    // Just a quickie enrichment to make some of the below tests more concise
    implicit class EnrichmentForTests(expected: TestIntStringCharOptional) {
      def apply(op: TestIntStringCharOptional => Unit) = op(expected)
    }

    // No arguments omitted
    TestIntStringCharOptional(x, y, z) { expected =>
      builder set (X, x) set (Y, y) set (Z, z) build () should equal(expected)
      builder set (X, x) set (Z, z) set (Y, y) build () should equal(expected)
      builder set (Y, y) set (X, x) set (Z, z) build () should equal(expected)
      builder set (Y, y) set (Z, z) set (X, x) build () should equal(expected)
      builder set (Z, z) set (X, x) set (Y, y) build () should equal(expected)
      builder set (Z, z) set (Y, y) set (X, x) build () should equal(expected)
    }

    // x argument omitted
    TestIntStringCharOptional(dx, y, z) { expected =>
      builder set (Y, y) set (Z, z) build () should equal(expected)
      builder set (Z, z) set (Y, y) build () should equal(expected)
    }

    // y argument omitted
    TestIntStringCharOptional(x, dy, z) { expected =>
      builder set (X, x) set (Z, z) build () should equal(expected)
      builder set (Z, z) set (X, x) build () should equal(expected)
    }

    // z argument omitted
    TestIntStringCharOptional(x, y, dz) { expected =>
      builder set (X, x) set (Y, y) build () should equal(expected)
      builder set (Y, y) set (X, x) build () should equal(expected)
    }

    // x and y omitted
    TestIntStringCharOptional(dx, dy, z) { expected =>
      builder set (Z, z) build () should equal(expected)
    }

    // x and z omitted
    TestIntStringCharOptional(dx, y, dz) { expected =>
      builder set (Y, y) build () should equal(expected)
    }

    // y and z omitted
    TestIntStringCharOptional(x, dy, dz) { expected =>
      builder set (X, x) build () should equal(expected)
    }

    // All arguments omitted
    TestIntStringCharOptional(dx, dy, dz) { expected =>
      builder.build() should equal(expected)
    }
  }
  
  "A builder for a case class with a complex parameter" should "generate the expected case class" in {
    import TestOptionString._
    val builder = TestOptionString.builder
    builder.set(X, None).build() should equal(TestOptionString(None))
    builder.set(X, Some("Hello")).build() should equal(TestOptionString(Some("Hello")))
    builder.set(X, Some("Hello")).build() should not equal(TestOptionString(Some("asdf")))
  }
  
  "A builder for a case class with a complex optional parameter" should "generate the expected case class" in {
    import TestOptionStringOptional._
    val builder = TestOptionStringOptional.builder
    
    // No parameters
    builder.build() should equal(TestOptionStringOptional(None))
    
    // Set to `None`
    builder.set(X, None).build() should equal(TestOptionStringOptional(None))
    
    // Set to `Some(...)`
    builder.set(X, Some("Hello")).build() should equal(TestOptionStringOptional(Some("Hello")))
  }
  
  "A builder challenged with a conundrum involving Either and Option" should "generate the expected case classes" in {
    import TestEither._
    val builder = TestEither.builder
    
    TestEither(Left(42), None) should equal {
      builder set(X, Left(42)) build()
    }
    
    TestEither(Right("Hello"), None) should equal {
      builder set(X, Right("Hello")) build()
    }
    
    TestEither(Left(42), Some(Left(1000))) should equal {
      builder set(Y, Some(Left(1000))) set(X, Left(42)) build()
    }
    
    TestEither(Left(42), Some(Right("World"))) should equal {
      builder set(Y, Some(Right("World"))) set(X, Left(42)) build()
    }
    
    TestEither(Right("Hello"), Some(Left(1000))) should equal {
      builder set(Y, Some(Left(1000))) set(X, Right("Hello")) build()
    }
    
    TestEither(Right("Hello"), Some(Right("World"))) should equal {
      builder set(Y, Some(Right("World"))) set(X, Right("Hello")) build() 
    }
  }
}