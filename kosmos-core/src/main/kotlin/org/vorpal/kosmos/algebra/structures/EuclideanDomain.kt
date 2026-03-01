package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.BinaryOp
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.FracNormalizer
import org.vorpal.kosmos.core.relations.StrictOrder
import org.vorpal.kosmos.core.relations.instances.IntRelations
import java.math.BigInteger

/**
 * TODO: EuclideanDomains were created to be used with the Wheels as per #97.
 *       They still lack the necessary LawSuite implementation as well as some Specs to test the implementation.
 *       Issue #110 details what needs to be done to make them a first-class citizen of Kosmos.
 */
interface EuclideanDomain<A : Any, M : Any>: IntegralDomain<A> {
    val divRem: BinaryOp<A, A, Pair<A, A>>

    /** Euclidean function `δ: A → M` with a strict order on `M` (typically `M = ℕ` with `δ(0) = 0`). */
    val measure: EuclidMeasure<A, M>

    companion object {
        fun <A : Any, M : Any> of(
            add: AbelianGroup<A>,
            mul: CommutativeMonoid<A>,
            divRem: BinaryOp<A, A, Pair<A, A>>,
            measure: EuclidMeasure<A, M>
        ): EuclideanDomain<A, M> = object : EuclideanDomain<A, M> {
            override val add: AbelianGroup<A> = add
            override val mul: CommutativeMonoid<A> = mul
            override val divRem: BinaryOp<A, A, Pair<A, A>> = divRem
            override val measure: EuclidMeasure<A, M> = measure
        }

        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: CommutativeMonoid<A>,
            divRem: BinaryOp<A, A, Pair<A, A>>,
            measureOp: UnaryOp<A, BigInteger>
        ): EuclideanDomain<A, BigInteger> = of(add, mul, divRem, EuclidMeasure.ofZ(measureOp))
    }
}

interface EuclidMeasure<A : Any, M : Any> {
    val measureOp: UnaryOp<A, M>
    val measureOrder: StrictOrder<M>

    companion object {
        fun <A : Any, M : Any> of(
            measureOp: UnaryOp<A, M>,
            measureOrder: StrictOrder<M>): EuclidMeasure<A, M> =
            object : EuclidMeasure<A, M> {
                override val measureOp = measureOp
                override val measureOrder = measureOrder
            }

        fun <A : Any> ofZ(
            measureOp: UnaryOp<A, BigInteger>
        ): EuclidMeasure<A, BigInteger> =
            of(measureOp, IntRelations.IntTotalStrict)
    }
}

fun <A : Any> EuclideanDomain<A, *>.div() : BinOp<A> =
    BinOp(Symbols.SLASH) { a, b -> divRem(a, b).first }

fun <A : Any> EuclideanDomain<A, *>.rem() : BinOp<A> =
    BinOp(Symbols.MOD) { a, b -> divRem(a, b).second }

tailrec fun <A : Any> EuclideanDomain<A, *>.gcd(a: A, b: A, eqA: Eq<A> = Eq.default()): A {
    if (eqA(b, zero)) return a
    val (_, r) = divRem(a, b)
    return if (eqA(r, zero)) b else gcd(b, r, eqA)
}

/**
 * The extended Euclidean algorithm result, namely 'a' and 'b' such that:
 * - `gcd = gcd(a, b)`, i.e. `gcd | a` and `gcd | b`, and if `c | a` and `c | b`, then `c | gcd`.
 * - Furthermore, `x, y` are solutions (not necessarily unique) of the equation `ax + by = gcd`.
 */
data class ExtendedGcdResult<A : Any>(val a: A, val b: A, val gcd: A, val x: A, val y: A)

fun <A : Any> EuclideanDomain<A, *>.extendedGcd(a: A, b: A, eqA: Eq<A> = Eq.default()): ExtendedGcdResult<A> {
    var oldR = a
    var r = b
    var oldS = one
    var s = zero
    var oldT = zero
    var t = one

    while (!eqA(r, zero)) {
        val (q, newR) = divRem(oldR, r)

        val newS = add(oldS, add.inverse(mul(q, s)))
        val newT = add(oldT, add.inverse(mul(q, t)))

        oldR = r
        r = newR

        oldS = s
        s = newS

        oldT = t
        t = newT
    }

    return ExtendedGcdResult(a, b, oldR, oldS, oldT)
}

fun <A : Any> EuclideanDomain<A, *>.lcm(a: A, b: A, eqA: Eq<A> = Eq.default()): A {
    val gcdab = gcd(a, b, eqA)
    val prod = mul(a, b)
    val (q, r) = divRem(prod, gcdab)
    require(eqA(r, zero)) { "lcm requires exact division: (a * b) % gcd(a, b) must be 0" }
    return q
}

fun <A : Any> EuclideanDomain<A, *>.toFracNormalizer(
    eqA: Eq<A> = Eq.default()
): FracNormalizer<A> = object : FracNormalizer<A> {
    override fun normalize(n: A, d: A): Pair<A, A> {
        // Carlström-wheel conventions:
        //   0/0 -> bottom
        //   n/0 (n != 0) -> infinity (1/0)
        if (eqA(d, zero)) {
            return if (eqA(n, zero)) {
                Pair(zero, zero)
            } else {
                Pair(one, zero)
            }
        }

        // Reduce by gcd.
        val g = gcd(n, d, eqA)

        val (nq, nr) = divRem(n, g)
        require(eqA(nr, zero)) { "normalize requires exact division: n % gcd(n,d) must be 0" }

        val (dq, dr) = divRem(d, g)
        require(eqA(dr, zero)) { "normalize requires exact division: d % gcd(n,d) must be 0" }

        return Pair(nq, dq)
    }
}
