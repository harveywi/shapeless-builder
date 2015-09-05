package com.github.harveywi.builder

import shapeless._
import shapeless.syntax._
import shapeless.ops.hlist.Replacer
import shapeless.ops.hlist.Selector
import scala.language.existentials
import shapeless.ops.hlist._

object Test {
  case class TestOptionString(x: Option[String])
  object TestOptionString extends HasBuilder[TestOptionString] {
    val gen = Generic[TestOptionString]
    
    object X extends Param[Option[String]]
    val fieldsContainer = createFieldsContainer(X :: HNil)
  }
  
  import TestOptionString._
  
//    implicit def casePParamHead[T, O <: PParam[T], L <: HList, Out <: HList](
//        implicit 
//        lubConstraint: LUBConstraint[L, PParam[_]],
//        caseRest: Case.Aux[L, Out]
//        ) = at[O :: L] {
//      case head :: tail =>
//        head.value :: GetParamValueMeow(tail)
//    }
   
  def main(args: Array[String]): Unit = {

    type L = HNil
    type CL = HNil

    val fields: L = HNil

    //    implicitly[Mapper.Aux[TestIntOptional.GetParamValue.type,
    //      TestIntOptional.X.type :: HNil,
    //      Int :: HNil]]

    import shapeless.poly._
    //    TestIntOptional.GetParamValueMeow(X :: HNil)
    val woof = TestOptionString.builder.set(X, None)
//    GetParamValueMeow(42)
//    val out: Int = GetParamValueMeow(X :: HNil)


    

  }
}