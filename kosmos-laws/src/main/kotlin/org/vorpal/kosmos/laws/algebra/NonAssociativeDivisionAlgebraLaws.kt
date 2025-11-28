package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.NonAssociativeDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.EndomorphismCommutationLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.property.NoZeroDivisorsLaw

/**
 * Laws for a NonAssociativeDivisionAlgebra<A>:
 *
 *  Structure:
 *    - (A, +, 0) is an abelian group
 *    - (A, ⋆, 1) is a unital non-associative magma (NonAssociativeMonoid)
 *    - conj is an involution and a *-anti-automorphism:
 *        (a*)* = a
 *        (a + b)* = a* + b*
 *        (ab)* = b* a*
 *        1* = 1
 *
 *  Division properties:
 *    - Every non-zero element is a unit:
 *        a ⋆ a⁻¹ = 1 = a⁻¹ ⋆ a
 *    - No zero divisors:
 *        ab = 0  ⇒  a = 0 or b = 0
 *      (tested as: for all non-zero a, b, ab ≠ 0)
 *    - Conjugation commutes with inversion:
 *        conj(a⁻¹) = (conj(a))⁻¹
 */
class NonAssociativeDivisionAlgebraLaws<A : Any>(
    private val algebra: NonAssociativeDivisionAlgebra<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋆",
    private val starSymbol: String = "*"
) {

    fun laws(): List<TestingLaw> =
        // 1. Underlying additive + multiplicative structure
        NonAssociativeAlgebraLaws(
            algebra = algebra,
            arb = arb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() +
                // 2. Involution structure (conjugation)
                InvolutiveAlgebraLaws(
                    algebra = algebra,
                    arb = arb,
                    eq = eq,
                    pr = pr,
                    addSymbol = addSymbol,
                    mulSymbol = mulSymbol,
                    starSymbol = starSymbol
                ).laws() +
                listOf(
                    // 3. Every non-zero element has a two-sided inverse (reciprocal)
                    InvertibilityLaw(
                        op = algebra.mul.op,
                        identity = algebra.mul.identity,
                        arbAll = arb,
                        eq = eq,
                        pr = pr,
                        inverseOrNull = algebra.reciprocalOrNull,
                        isUnit = { a -> !eq.eqv(a, algebra.zero) },
                        symbol = mulSymbol
                    ),

                    // 4. No zero divisors: for non-zero a, b, ab ≠ 0
                    NoZeroDivisorsLaw(
                        op = algebra.mul.op,
                        zero = algebra.zero,
                        pairArb = Arb.pair(arb, arb),
                        eq = eq,
                        pr = pr,
                        symbol = mulSymbol
                    ),

                    // 5. Conjugation commutes with inversion:
                    //      conj(a⁻¹) = (conj(a))⁻¹   for all a ≠ 0
                    EndomorphismCommutationLaw(
                        f = algebra.conj,
                        g = algebra.reciprocal,
                        arb = arb,
                        eq = eq,
                        pr = pr,
                        inDomain = { a -> !eq.eqv(a, algebra.zero) },
                        nameHint = "conjugation respects reciprocal: conj(a⁻¹) = (conj(a))⁻¹"
                    )
                )
}