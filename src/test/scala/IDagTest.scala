package neurocat
package test

import idag._
import minitest._
import typeclasses._
import shapeless.{HNil, ::}
import algebra.ring.AdditiveSemigroup
import org.nd4j.linalg.factory.Nd4j
import singleton.ops._
import neurocat.nd4j._
import shapeless.labelled._
import shapeless._ ; import syntax.singleton._ ; import record._
import cats.Show
import cats.implicits._


object IDagTest extends SimpleTestSuite {

  val dsl = new DiffDagDsl[Double, Lambda[out[p, a, b] => ND4JAlgebra[out]]]
  with nd4j.DiffDsl[Double, Lambda[out[p, a, b] => ND4JAlgebra[out]]] {}
  import dsl._

  test("idag0") {

    // val idDag = id[Double] 
    // val idDagGradA = idDag.gradA
    import nd4j.L2._

    // val net = compose(id[Mat[Double, 3 x 1]], id[Mat[Double, 3 x 1]])

    // val idF = idDag.compile(Compiler)
    // val idGradAF = idDagGradA.compile(Compiler)

    // assertEquals(idF(HNil, 1.234), 1.234)
    // assertEquals(idGradAF(HNil, (1.234, 1.234)), 1.234)

    def nnetlayer[InR <: XInt : SafeInt, OutR <: XInt : SafeInt] =
      (weightMat["s", InR, 1, OutR] >>> sigmoid) ||
      (weightMat["t", InR, 1, OutR] >>> sigmoid)

    val n = nnetlayer[3, 4]

    val learnCompiler = LearnCompiler(learnRate = 0.006)

    // // Out is there to help a bit the compiler which is limited :(
    type Out[p, a, b] = Dag[p, a, b, Double, ND4JAlgebra]
    val learner = n.compile[Out](learnCompiler)

    val finalFn = learner.compile(ND4JCompiler)

    val SW = "s" ->> Mat.randomD2[Double, 4 x 3](min = 0.0, max = 1.0)
    val TW = "t" ->> Mat.randomD2[Double, 4 x 3](min = 0.0, max = 1.0)

    val x = Mat.columnVector[Double, 3](Array(0.0, 1.0, 0.0))
    val y = Mat.columnVector[Double, 4](Array(1.0, 0.0, 0.0, 0.0))

    val (l, r) = finalFn(SW :: TW :: HNil, (x, x))

    println(s"r:(${l.show}, ${r.show}")
    assertEquals(1, 1)
          

    // trait Trainer[
    //   DataSet[row, nb <: XInt]
    // ] {
    //   def train[P, In, Out, NbSamples <: XInt](
    //     learner: ParametrisedFunction[P, (In, Out), P]
    //   )(
    //     initParams: P
    //   , trainingData: DataSet[(In, Out), NbSamples]
    //   ): P
    // }

    // def naive[
    //   DataSet[row, nb <: XInt]
    // ](implicit rowTr: RowTraversable[DataSet]): Trainer[DataSet] =
    //   new Trainer[DataSet] {      
    //     def train[P, In, Out, NbSamples <: XInt](
    //       learner: ParametrisedFunction[P, (In, Out), P]
    //     )(
    //       initParams: P
    //     , trainingData: DataSet[(In, Out), NbSamples]
    //     ): P = {
    //       var params = initParams

    //       rowTr.foreachRow(trainingData) {
    //         case (inRow, outRow) =>
    //           params = learner(params, (inRow, outRow))
    //       }

    //       params
    //     }
    //   }

    val trainX = DataSet[4](Mat.fromArrays[Double, 4 x 3](Array(
      Array(0.0, 1.0, 0.0)
    , Array(0.0, 1.0, 0.0)
    , Array(0.0, 1.0, 0.0)
    , Array(0.0, 1.0, 0.0)
    )))

    val trainY = DataSet[4](Mat.fromArrays[Double, 4 x 4](Array(
      Array(1.0, 0.0, 0.0, 0.0)
    , Array(1.0, 0.0, 0.0, 0.0)
    , Array(1.0, 0.0, 0.0, 0.0)
    , Array(1.0, 0.0, 0.0, 0.0)
    )))

    val trainXXYY = ProductDataSet(ProductDataSet(trainX, trainX), ProductDataSet(trainY, trainY))

    import DataSet._

    val learner2 = n.gradP.compile[Out](learnCompiler)
    val finalFn2 = learner2.compile(ND4JCompiler)
    val p = Trainer.naive[DataSet].train(finalFn2)(SW :: TW :: HNil, trainXXYY)

    val (p1 : Mat[Double, 4 x 3], p2 : Mat[Double, 4 x 3]) = (p.head, p.tail.head)
    println(s"p1:${p1.show} p2:${p2.show}")
    assertEquals(2, 2)
  }

}