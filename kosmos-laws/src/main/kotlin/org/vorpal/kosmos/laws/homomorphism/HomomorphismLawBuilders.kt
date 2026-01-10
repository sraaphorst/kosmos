package org.vorpal.kosmos.laws.homomorphism

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Op preservation:
 *
 *    f(a ⋆ b) = f(a) * f(b)
 *
 * Works for any binary operations (magma / semigroup / monoid / group / etc).
 */
fun <A : Any, B : Any> preservesBinaryOpLaw(
    domainOp: BinOp<A>,
    codomainOp: BinOp<B>,
    hom: (A) -> B,
    arbA: Arb<A>,
    eqB: Eq<B> = Eq.default(),
    prA: Printable<A> = Printable.default(),
    prB: Printable<B> = Printable.default(),
    label: String = "homomorphism: preserves op"
): TestingLaw {
    val pairArb = TestingLaw.arbPair(arbA)
    val arrow = "${domainOp.symbol} → ${codomainOp.symbol}"

    return TestingLaw.named("$label ($arrow)") {
        checkAll(pairArb) { (a, b) ->
            val left = hom(domainOp(a, b))
            val right = codomainOp(hom(a), hom(b))

            withClue(
                buildString {
                    val sa = prA(a)
                    val sb = prA(b)
                    appendLine("Op preservation failed:")
                    appendLine("f($sa ${domainOp.symbol} $sb) = ${prB(left)}")
                    appendLine("f($sa) ${codomainOp.symbol} f($sb) = ${prB(right)}")
                }
            ) {
                check(eqB(left, right))
            }
        }
    }
}

/**
 * Simplified version for when the domain and codomain are the same.
 */
fun <A : Any> preservesBinaryOpLaw(
    domainOp: BinOp<A>,
    hom: (A) -> A,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "endomorphism: preserves op"
) = preservesBinaryOpLaw(domainOp, domainOp, hom, arb, eq, pr, pr, label)

/**
 * Op anti-preservation:
 *
 *    f(a ⋆ b) = f(b) * f(a)
 *
 * Useful for involutions / anti-homomorphisms (e.g. `(ab)* = b* a*`).
 */
fun <A : Any, B : Any> antiPreservesBinaryOpLaw(
    domainOp: BinOp<A>,
    codomainOp: BinOp<B>,
    hom: (A) -> B,
    arbA: Arb<A>,
    eqB: Eq<B> = Eq.default(),
    prA: Printable<A> = Printable.default(),
    prB: Printable<B> = Printable.default(),
    label: String = "homomorphism: anti-preserves op"
): TestingLaw {
    val pairArb = TestingLaw.arbPair(arbA)
    val arrow = "${domainOp.symbol} → ${codomainOp.symbol}"

    return TestingLaw.named("$label ($arrow)") {
        checkAll(pairArb) { (a, b) ->
            val left = hom(domainOp(a, b))
            val right = codomainOp(hom(b), hom(a))

            withClue(
                buildString {
                    val sa = prA(a)
                    val sb = prA(b)
                    appendLine("Op anti-preservation failed:")
                    appendLine("f($sa ${domainOp.symbol} $sb) = ${prB(left)}")
                    appendLine("f($sb) ${codomainOp.symbol} f($sa) = ${prB(right)}")
                }
            ) {
                check(eqB(left, right))
            }
        }
    }
}

/**
 * Simplified version for when the domain and the codomain are the same.
 */
fun <A : Any> antiPreservesBinaryOpLaw(
    domainOp: BinOp<A>,
    hom: (A) -> A,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "endomorphism: anti-preserves op"
) = antiPreservesBinaryOpLaw(domainOp, domainOp, hom, arb, eq, pr, pr, label)

/**
 * Identity preservation:
 *
 *    f(1_A) = 1_B
 */
fun <A : Any, B : Any> preservesIdentityLaw(
    domainIdentity: A,
    codomainIdentity: B,
    hom: (A) -> B,
    eqB: Eq<B> = Eq.default(),
    prA: Printable<A> = Printable.default(),
    prB: Printable<B> = Printable.default(),
    label: String = "homomorphism: preserves identity"
): TestingLaw =
    TestingLaw.named(label) {
        val left = hom(domainIdentity)

        withClue(
            buildString {
                appendLine("Identity preservation failed:")
                appendLine("f(${prA(domainIdentity)}) = ${prB(left)}")
                appendLine("Expected: ${prB(codomainIdentity)}")
            }
        ) {
            check(eqB(left, codomainIdentity))
        }
    }

/**
 * Simplified version for when the domain and the codomain are the same.
 */
fun <A : Any> preservesIdentityLaw(
    identity: A,
    hom: (A) -> A,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "endomorphism: preserves identity"
) = preservesIdentityLaw(identity, identity, hom, eq, pr, pr, label)
