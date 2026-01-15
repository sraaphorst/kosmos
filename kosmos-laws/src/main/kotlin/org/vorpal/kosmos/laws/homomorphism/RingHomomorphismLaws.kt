package org.vorpal.kosmos.laws.homomorphism

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Ring homomorphism laws:
 *
 *    hom(a + b) = hom(a) + hom(b)
 *    hom(0_A) = 0_B
 *    hom(ab) = hom(a) * hom(b)
 *
 */
class RingHomomorphismLaws<A : Any, B : Any>(
    private val hom: (A) -> B,
    private val domain: Ring<A>,
    private val codomain: Ring<B>,
    private val arb: Arb<A>,
    private val eqB: Eq<B> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prB: Printable<B> = Printable.default()
    ) : LawSuite {
O
    override val name =
        "ring homomorphism ((${domain.add.op.symbol}, $domain.mul.op.symbol}} → (${codomain.add.op.symbol}), ${codomain.mul.op.symbol}))"

    override fun laws(): List<TestingLaw> = listOf(
        preservesBinaryOpLaw(
            domainOp = domain.add.op,
            codomainOp = codomain.add.op,
            hom = hom,
            arbA = arb,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "ring homomorphism: addition"
        ),
        preservesIdentityLaw(
            domainIdentity = domain.add.identity,
            codomainIdentity = codomain.add.identity,
            hom = hom,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "ring homomorphism: preserves additive identity"
        ),

        preservesBinaryOpLaw(
            domainOp = domain.mul.op,
            codomainOp = codomain.mul.op,
            hom = hom,
            arbA = arb,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "ring homomorphism: multiplication"
        )
    )
}
