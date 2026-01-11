package org.vorpal.kosmos.laws.homomorphism

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw

class NonAssociativeRingHomomorphismLaws<A : Any, B : Any>(
    private val hom: (A) -> B,
    private val domain: NonAssociativeRing<A>,
    private val codomain: NonAssociativeRing<B>,
    private val arb: Arb<A>,
    private val eqB: Eq<B> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prB: Printable<B> = Printable.default()
) : LawSuite {

    override val name =
        "non-associative ring homomorphism ((${domain.add.op.symbol}, ${domain.mul.op.symbol}) → (${codomain.add.op.symbol}, ${codomain.mul.op.symbol}))"

    override fun laws(): List<TestingLaw> = listOf(
        preservesBinaryOpLaw(
            domainOp = domain.add.op,
            codomainOp = codomain.add.op, hom,
            arbA = arb,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "non-associative ring homomorphism: addition"
        ),
        preservesIdentityLaw(
            domainIdentity = domain.add.identity,
            codomainIdentity = codomain.add.identity,
            hom = hom,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "non-associative ring hom: preserves additive identity"
        ),
        preservesBinaryOpLaw(
            domainOp = domain.mul.op,
            codomainOp = codomain.mul.op, hom,
            arbA = arb,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "non-associative ring hom: multiplication"
        )
    )
}
