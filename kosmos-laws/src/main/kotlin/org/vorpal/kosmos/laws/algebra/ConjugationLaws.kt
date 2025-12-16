package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.homomorphism.antiPreservesBinaryOpLaw
import org.vorpal.kosmos.laws.homomorphism.preservesBinaryOpLaw
import org.vorpal.kosmos.laws.homomorphism.preservesIdentityLaw
import org.vorpal.kosmos.laws.property.InvolutionLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * Conjugation laws:
 *
 *    conj(conj(x)) = x
 *    conj(x + y) = conj(x) + conj(y)
 *    conj(x * y) = conj(y) * conj(x)
 *    conj(0) = 0
 *    conj(1) = 1
 */
class ConjugationLaws<A : Any>(
    private val conj: Endo<A>,
    private val add: NonAssociativeMonoid<A>,
    private val mul: NonAssociativeMonoid<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.Companion.default(),
    private val pr: Printable<A> = Printable.Companion.default()
): LawSuite {

    override val name = suiteName("Conjugation", conj.symbol, add.op.symbol, mul.op.symbol)

    override fun laws(): List<TestingLaw> = listOf(
        InvolutionLaw(conj, arb, eq, pr),
        preservesBinaryOpLaw(add.op, conj::invoke, arb, eq, pr, "conj preserves addition"),
        antiPreservesBinaryOpLaw(mul.op, conj::invoke, arb, eq, pr, "conj anti-preserves multiplication"),
        preservesIdentityLaw(add.identity, conj::invoke, eq, pr, "conj preserves additive identity"),
        preservesIdentityLaw(mul.identity, conj::invoke, eq, pr, "conj preserves multiplicative identity")
    )
}