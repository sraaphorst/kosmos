package org.vorpal.kosmos.algebra.poly.scope

import org.vorpal.kosmos.algebra.poly.Poly
import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.algebra.structures.HasFromBigInt
import java.math.BigInteger

/**
 * A semiring polynomial scope: provides + and * for Poly<A>, plus embedding A -> Poly<A>.
 *
 * This scope is intentionally "thin": it is a Kotlin-operator façade over your BinOp-based structures.
 */
open class PolyScope<A : Any>(
    val kit: PolyKit<A>
) {
    /** Last generator (univariate: x). */
    val x: Poly<A>
        get() = kit.x()

    /** Get a generator by index (useful for multivariate towers). */
    fun gen(i: Int): Poly<A> =
        kit.gen(i)

    val vars: List<Poly<A>>
        get() = (0 until kit.arity).map(::gen)

    /** Embed a coefficient as a constant polynomial. */
    fun c(a: A): Poly<A> =
        kit.embed(a)

    val zero: Poly<A>
        get() = kit.zeroP

    val one: Poly<A>
        get() = kit.oneP

    /* --------------------------
     * Poly-Poly ops
     * -------------------------- */

    operator fun Poly<A>.plus(
        q: Poly<A>
    ): Poly<A> =
        kit.addP(this, q)

    operator fun Poly<A>.times(
        q: Poly<A>
    ): Poly<A> =
        kit.mulP(this, q)

    /* --------------------------
     * Scalar actions: A * Poly and Poly * A
     * -------------------------- */

    operator fun A.times(
        p: Poly<A>
    ): Poly<A> =
        kit.mulP(kit.embed(this), p)

    operator fun Poly<A>.times(
        a: A
    ): Poly<A> =
        kit.mulP(this, kit.embed(a))
}

/**
 * Ring-flavored scope: adds unary minus and subtraction when the coefficient structure supports it.
 *
 * Note: PolyKit<A> stores negation as Option<Endo<A>>, so we gate these ops on that.
 */
open class PolyRingScope<A : Any>(
    kit: PolyKit<A>
) : PolyScope<A>(kit) {

    operator fun Poly<A>.unaryMinus(): Poly<A> =
        kit.neg(this)

    operator fun Poly<A>.minus(
        q: Poly<A>
    ): Poly<A> =
        this + (-q)
}

/**
 * Adds integer literal convenience (5, 3, 1, ...) by using HasFromBigInt<A>.
 *
 * This is what makes:
 *   5 * x * x + 3 * x + 1
 * compile nicely inside the scope.
 */
class PolyIntLiteralScope<A : Any>(
    kit: PolyKit<A>,
    private val fromZ: HasFromBigInt<A>
) : PolyRingScope<A>(kit) {

    fun fromBigInt(n: BigInteger): Poly<A> =
        c(fromZ.fromBigInt(n))

    fun fromInt(n: Int): Poly<A> =
        c(fromZ.fromInt(n))

    fun fromLong(n: Long): Poly<A> =
        c(fromZ.fromLong(n))

    /* --------------------------
     * Int literals interacting with Poly<A>
     * -------------------------- */

    operator fun Int.plus(p: Poly<A>): Poly<A> =
        fromInt(this) + p

    operator fun Poly<A>.plus(n: Int): Poly<A> =
        this + fromInt(n)

    operator fun Int.times(p: Poly<A>): Poly<A> =
        fromInt(this) * p

    operator fun Poly<A>.times(n: Int): Poly<A> =
        this * fromInt(n)

    operator fun Int.minus(p: Poly<A>): Poly<A> =
        fromInt(this) - p

    operator fun Poly<A>.minus(n: Int): Poly<A> =
        this - fromInt(n)
}
