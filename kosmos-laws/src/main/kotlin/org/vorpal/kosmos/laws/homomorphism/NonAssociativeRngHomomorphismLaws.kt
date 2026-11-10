package org.vorpal.kosmos.laws.homomorphism

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.asNodes

class NonAssociativeRngHomomorphismLaws<A : Any, B : Any>(
    private val hom: (A) -> B,
    private val domain: NonAssociativeRing<A>,
    private val codomain: NonAssociativeRing<B>,
    private val arb: Arb<A>,
    private val eqB: Eq<B> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prB: Printable<B> = Printable.default()
) : StructuredLawSuite {

    override val name =
        "non-associative ring homomorphism ((${domain.add.op.symbol}, ${domain.mul.op.symbol}) → (${codomain.add.op.symbol}, ${codomain.mul.op.symbol}))"

    override val binding = LawBinding.of(this, hom, domain, codomain, arb, eqB, prA, prB)

    override fun children(mode: LawMode): List<LawNode> = listOf(
        preservesBinaryOpLaw(
            domainOp = domain.add.op,
            codomainOp = codomain.add.op,
            hom = hom,
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
            codomainOp = codomain.mul.op,
            hom = hom,
            arbA = arb,
            eqB = eqB,
            prA = prA,
            prB = prB,
            label = "non-associative ring hom: multiplication"
        )
    ).asNodes()
}
