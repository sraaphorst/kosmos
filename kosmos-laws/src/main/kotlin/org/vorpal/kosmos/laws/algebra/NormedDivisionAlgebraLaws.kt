package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.homomorphism.preservesBinaryOpLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * Laws for [NormedDivisionAlgebra]<N, A>:
 * - [NonAssociativeDivisionAlgebraLaws] on A
 * - Multiplicativity of normSq: N(xy) = N(x) * N(y)
 *
 * Optionally (if [zeroN] is provided):
 * - Definiteness: N(x) = 0_N  ⇔  x = 0_A
 */
class NormedDivisionAlgebraLaws<N : Any, A : Any>(
    private val algebra: NormedDivisionAlgebra<N, A>,
    private val arbA: Arb<A>,
    private val mulN: BinOp<N>,
    private val eqA: Eq<A> = Eq.default(),
    private val eqN: Eq<N> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prN: Printable<N> = Printable.default(),
    private val zeroN: N? = null
) : LawSuite {

    private val normDesc = "N[${algebra.normSq.symbol}]"

    override val name = suiteName(
        "NormedDivisionAlgebra",
        algebra.add.op.symbol,
        algebra.mul.op.symbol,
        algebra.conj.symbol,
        algebra.reciprocal.symbol,
        normDesc
    )

    private val divAlgLaws = NonAssociativeDivisionAlgebraLaws(algebra, arbA, eqA, prA)

    private val structureLaws: List<TestingLaw> = buildList {
        // N(xy) = N(x) * N(y)
        add(
            preservesBinaryOpLaw(
                domainOp = algebra.mul.op,
                codomainOp = mulN,
                hom = algebra.normSq::invoke,
                arbA = arbA,
                eqB = eqN,
                prA = prA,
                prB = prN,
                label = "normSq multiplicative"
            )
        )

        // Optional: definiteness (requires a chosen 0_N)
        if (zeroN != null) {
            add(
                TestingLaw.named("normSq definite: N(a)=0 ⇔ a=0") {
                    checkAll(arbA) { a ->
                        val isNormZero = eqN(algebra.normSq(a), zeroN)
                        val isVecZero = eqA(a, algebra.zero)

                        check(isNormZero == isVecZero) {
                            "Definiteness failed:\n" +
                                "a=${prA(a)}\n" +
                                "N(a)=${prN(algebra.normSq(a))}\n" +
                                "N(a)==0? $isNormZero   a==0? $isVecZero"
                        }
                    }
                }
            )
        }
    }

    override fun laws(): List<TestingLaw> =
        divAlgLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        divAlgLaws.fullLaws() + structureLaws
}