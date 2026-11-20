package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.exists
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.NormSqDefiniteLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * Laws for [NormedDivisionAlgebra]<N, A>:
 * - [NonAssociativeDivisionAlgebraLaws] on A
 * - Multiplicativity of normSq:
 * ```
 * N(xy) = N(x) * N(y)
 *```
 * Optionally, if [zeroN] = `Some(0_N)`:
 * - Definiteness:
 * ```
 * N(x) = 0_N ⇔ x = 0_A
 * ```
 * In the future, we may want to include:
 * - NormSqFromConjugationLaw
 * - NormSqNegationInvariantLaw
 * This depends on the kinds of algebras represented by NormedDivisionAlgebras:
 * - For classical composition algebras, yes.
 * - For future exotic algebras that may come up, possibly not.
 */
class NormedDivisionAlgebraLaws<N : Any, A : Any>(
    private val algebra: NormedDivisionAlgebra<N, A>,
    private val arbA: Arb<A>,
    private val mulN: BinOp<N>,
    private val eqA: Eq<A> = Eq.default(),
    private val eqN: Eq<N> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prN: Printable<N> = Printable.default(),
    private val zeroN: Option<N> = Option.None
) : StructuredLawSuite {

    private val normDesc = "N[${algebra.normSq.symbol}]"

    override val name = suiteName(
        "NormedDivisionAlgebra",
        algebra.add.op.symbol,
        algebra.mul.op.symbol,
        algebra.conj.symbol,
        algebra.reciprocal.symbol,
        normDesc
    )

    override val binding = LawBinding.of(this, algebra, arbA, mulN, eqA, eqN, prA, prN, zeroN)

    private val divAlgLaws =
        NonAssociativeDivisionAlgebraLaws(algebra, arbA, eqA, prA)

    private val structureLaws: List<TestingLaw> = buildList {
        add(
            NormSqMultiplicativeLaw(
                mulA = algebra.mul.op,
                normSq = algebra.normSq,
                mulN = mulN,
                arbA = arbA,
                eqN = eqN,
                prA = prA,
                prN = prN
            )
        )

        zeroN.exists {
            add(
                NormSqDefiniteLaw(
                    normSq = algebra.normSq,
                    zeroA = algebra.zero,
                    zeroN = it,
                    arbA = arbA,
                    eqA = eqA,
                    eqN = eqN,
                    prA = prA,
                    prN = prN
                )
            )
        }
    }

    override fun children(mode: LawMode): List<LawNode> =
        listOf(divAlgLaws.node(mode)) +
            structureLaws.asNodes()

}
