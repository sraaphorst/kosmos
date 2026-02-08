package org.vorpal.kosmos.algebra.poly.kit

import org.vorpal.kosmos.algebra.poly.Poly
import org.vorpal.kosmos.algebra.poly.kernel.PolyKernel
import org.vorpal.kosmos.algebra.poly.polyEq
import org.vorpal.kosmos.algebra.poly.structures.polyCommutativeRing
import org.vorpal.kosmos.algebra.poly.structures.polyRing
import org.vorpal.kosmos.algebra.poly.structures.polySemiring
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.functional.datastructures.Option

/**
 * Bundle for working with polynomials over coefficients [A].
 *
 * - [coeff]: the coefficient semiring (rings/fields fit here too)
 * - [poly]: the induced semiring structure on `Poly<A>`.
 * - [embed]: canonical monomorphism `A -> Poly<A>` (constants)
 * - [gens]: polynomial generators (variables) as `Poly<A>`
 *
 * Multivariates are modelled by iterated extension: R[x][y][z] ...
 */
class PolyKit<A : Any> internal constructor(
    val coeff: Semiring<A>,
    val eqA: Eq<A>,
    val poly: Semiring<Poly<A>>,
    val negA: Option<Endo<A>>,
    val varNames: List<String>,
    val gens: List<Poly<A>>
) {
    val vars: List<Poly<A>>
        get() = gens

    val zeroA: A
        get() = coeff.zero

    val oneA: A
        get() = coeff.one

    val zeroP: Poly<A>
        get() = poly.zero

    val oneP: Poly<A>
        get() = poly.one

    val addP: BinOp<Poly<A>>
        get() = poly.add.op

    val mulP: BinOp<Poly<A>>
        get() = poly.mul.op

    fun neg(p: Poly<A>): Poly<A> = when (negA) {
        is Option.Some -> PolyKernel.negPoly(p, negA.value, eqA, zeroA)
        else -> error("No additive inverse available: coefficient structure is not a ring")
    }

    /**
     * These allow binding properties like:
     * ```
     * val x = gen(0)
     * val y = gen(1)
     * ```
     */
    fun gen(i: Int): Poly<A> = gens[i]
    fun name(i: Int): String = varNames[i]
    val arity: Int
        get() = gens.size

    /**
     * Canonical embedding `A -> Poly<A>` as constants, normalized.
     */
    fun embed(a: A): Poly<A> =
        PolyKernel.constant(a, eqA, zeroA)

    /**
     * Convenience: the last adjoined variable (univariate: "x").
     */
    fun x(): Poly<A> =
        gens.last()

    /**
     * Extend by adjoining a new variable name, producing a kit for (Poly<A>)[name].
     *
     * This is the standard "tower" representation of multivariate polynomials:
     * ```
     * R[x][y][z]...
     * ```
     * Old generators are lifted as constants in the new variable.
     */
    fun extend(
        name: String,
        addSymbol: Option<String> = Option.None,
        mulSymbol: Option<String> = Option.None
    ): PolyKit<Poly<A>> {
        val eqPolyA = polyEq(eqA)

        // The new coefficient structure is the current polynomial structure:
        val coeff2: Semiring<Poly<A>> = poly

        // Build the polynomial structure over the new coefficients:
        val poly2: Semiring<Poly<Poly<A>>> = when (coeff2) {
            is CommutativeRing<*> ->
                (coeff2 as CommutativeRing<Poly<A>>).polyCommutativeRing(eqPolyA, addSymbol, mulSymbol)
            is Ring<*> ->
                (coeff2 as Ring<Poly<A>>).polyRing(eqPolyA, addSymbol, mulSymbol)
            else ->
                coeff2.polySemiring(eqPolyA, addSymbol, mulSymbol)
        }

        // Coefficient negation info for the new kit (coeff type is Poly<A>):
        val neg2: Option<Endo<Poly<A>>> = when (coeff2) {
            is Ring<*> -> Option.Some((coeff2 as Ring<Poly<A>>).add.inverse)
            else -> Option.None
        }

        // Lift old generators as constants in the new variable:
        val zeroCoeff2: Poly<A> = Poly.zero()
        val liftedGens: List<Poly<Poly<A>>> = gens.map { g ->
            PolyKernel.constant(g, eqPolyA, zeroCoeff2)
        }

        // New variable over coefficients Poly<A>:
        // Reads very nicely: y = 0 + 1·y
        val newVar = PolyKernel.x(zeroCoeff2, oneP, eqPolyA)

        return PolyKit(
            coeff2, eqPolyA, poly2, neg2, varNames + name, liftedGens + newVar
        )
    }

    companion object {
        /**
         * Univariate kit from a Semiring.
         */
        fun <A : Any> fromSemiring(
            coeff: Semiring<A>,
            eqA: Eq<A>,
            name: String = "x",
            addSymbol: Option<String> = Option.None,
            mulSymbol: Option<String> = Option.None
        ): PolyKit<A> {
            val poly = coeff.polySemiring(eqA, addSymbol, mulSymbol)
            val x = PolyKernel.x(coeff.zero, coeff.one, eqA)

            return PolyKit(
                coeff, eqA, poly, Option.None, listOf(name), listOf(x)
            )
        }

        /**
         * Univariate kit from a Ring.
         */
        fun <A : Any> fromRing(
            coeff: Ring<A>,
            eqA: Eq<A>,
            name: String = "x",
            addSymbol: Option<String> = Option.None,
            mulSymbol: Option<String> = Option.None
        ): PolyKit<A> {
            val poly = coeff.polyRing(eqA, addSymbol, mulSymbol)
            val x = PolyKernel.x(coeff.zero, coeff.one, eqA)

            return PolyKit(
                coeff, eqA, poly, Option.Some(coeff.add.inverse), listOf(name), listOf(x)
            )
        }

        /**
         * Univariate kit from a CommutativeRing.
         */
        fun <A : Any> fromCommutativeRing(
            coeff: CommutativeRing<A>,
            eqA: Eq<A>,
            name: String = "x",
            addSymbol: Option<String> = Option.None,
            mulSymbol: Option<String> = Option.None
        ): PolyKit<A> {
            val poly = coeff.polyCommutativeRing(eqA, addSymbol, mulSymbol)
            val x = PolyKernel.x(coeff.zero, coeff.one, eqA)

            return PolyKit(
                coeff, eqA, poly, Option.Some(coeff.add.inverse), listOf(name), listOf(x)
            )
        }
    }
}
