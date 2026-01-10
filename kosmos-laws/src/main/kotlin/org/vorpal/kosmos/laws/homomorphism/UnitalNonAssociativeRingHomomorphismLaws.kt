package org.vorpal.kosmos.laws.homomorphism

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw

class UnitalNonAssociativeRingHomomorphismLaws<A : Any, B : Any>(
    private val hom: (A) -> B,
    private val domain: NonAssociativeRing<A>,
    private val codomain: NonAssociativeRing<B>,
    arb: Arb<A>,
    private val eqB: Eq<B> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prB: Printable<B> = Printable.default()
) : LawSuite {

    override val name =
        "unital non-associative ring homomorphism ((${domain.add.op.symbol}, $domain.mul.op.symbol}} → (${codomain.add.op.symbol}), ${codomain.mul.op.symbol}))"

    private val base = NonAssociativeRingHomomorphismLaws(hom, domain, codomain, arb, eqB, prA, prB)

    override fun laws(): List<TestingLaw> =
        base.laws() + listOf(
            preservesIdentityLaw(
                domainIdentity = domain.mul.identity,
                codomainIdentity = codomain.mul.identity,
                hom = hom,
                eqB = eqB,
                prA = prA,
                prB = prB,
                label = "non-associative ring homomorphism: preserves multiplicative identity"
            )
        )
}
