package org.vorpal.kosmos.laws.homomorphism

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Unital homomorphism laws:
 *
 *    hom(a · b) = hom(a) * hom(b)
 *    hom(1_A) = 1_B
 */
class UnitalHomomorphismLaws<A : Any, B : Any>(
    private val domain: NonAssociativeMonoid<A>,
    private val codomain: NonAssociativeMonoid<B>,
    private val hom: (A) -> B,
    private val arb: Arb<A>,
    private val eqB: Eq<B> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prB: Printable<B> = Printable.default()
) : LawSuite {
    override val name = "UnitalHomomorphism (${domain.op.symbol} → ${codomain.op.symbol})"

    override fun laws(): List<TestingLaw> = listOf(
        preservesBinaryOpLaw(
            domainOp = domain.op,
            codomainOp = codomain.op,
            hom = hom,
            arbA = arb,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "unital homomorphism: multiplicative"
        ),
        preservesIdentityLaw(
            domainIdentity = domain.identity,
            codomainIdentity = codomain.identity,
            hom = hom,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "unital homomorphism: preserves identity"
        )
    )
}