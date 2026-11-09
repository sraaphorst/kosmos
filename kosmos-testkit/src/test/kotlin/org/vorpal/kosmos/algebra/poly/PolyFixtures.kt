package org.vorpal.kosmos.algebra.poly

import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

/**
 * A deliberately naive model of polynomials as plain coefficient lists, written independently of
 * `PolyKernel`, so the kernel's results can be compared against it.
 *
 * Products use the defining convolution formula `(pq)ₖ = Σ_{i+j=k} pᵢ·qⱼ` with the coefficient from
 * `p` on the left, which is the right order for noncommutative coefficient rings.
 */
internal class ListPolyModel<A : Any>(
    private val coeff: Semiring<A>,
    private val eqA: Eq<A> = Eq.default()
) {
    private val zero = coeff.zero

    fun trim(cs: List<A>): List<A> =
        cs.dropLastWhile { eqA(it, zero) }

    fun add(p: List<A>, q: List<A>): List<A> =
        trim(List(maxOf(p.size, q.size)) { i ->
            coeff.add(p.getOrElse(i) { zero }, q.getOrElse(i) { zero })
        })

    fun mul(p: List<A>, q: List<A>): List<A> =
        if (p.isEmpty() || q.isEmpty()) emptyList()
        else trim(List(p.size + q.size - 1) { k ->
            (0..k)
                .filter { i -> i in p.indices && (k - i) in q.indices }
                .fold(zero) { acc, i -> coeff.add(acc, coeff.mul(p[i], q[k - i])) }
        })

    fun neg(p: List<A>, negA: (A) -> A): List<A> =
        trim(p.map(negA))

    /** `(p')ₖ = (k+1)·pₖ₊₁`, where `(k+1)·c` is `c` added to itself `k+1` times. */
    fun derivative(p: List<A>): List<A> =
        trim(p.drop(1).mapIndexed { k, c ->
            (0..k).fold(zero) { acc, _ -> coeff.add(acc, c) }
        })

    /** `Σ cᵢ·aⁱ`, with each power computed from scratch (not Horner). */
    fun eval(p: List<A>, a: A): A =
        p.indices.fold(zero) { acc, i ->
            coeff.add(acc, coeff.mul(p[i], pow(a, i)))
        }

    private fun pow(a: A, n: Int): A =
        (0 until n).fold(coeff.one) { acc, _ -> coeff.mul(acc, a) }
}

/** The degree of a polynomial, with `deg 0 = -1` standing in for `-∞`. */
internal val Poly<*>.degree: Int
    get() = coeffs.size - 1

/** `x^n` in the kit's last variable, by repeated multiplication. */
internal fun <A : Any> PolyKit<A>.xPow(n: Int): Poly<A> =
    (0 until n).fold(oneP) { acc, _ -> mulP(acc, x()) }

/**
 * The ring `ℤ/nℤ` on canonical residues `0..n-1`. For `n` composite this has zero divisors, which
 * is exactly what the polynomial tests need: `ℤ/4[x]` has `(2x)² = 0` and the non-constant unit
 * `2x + 1`.
 */
internal fun integersMod(n: Int): CommutativeRing<BigInteger> {
    val m = n.toBigInteger()
    fun canon(a: BigInteger): BigInteger = a.mod(m)

    return CommutativeRing.of(
        add = AbelianGroup.of(
            identity = BigInteger.ZERO,
            op = BinOp(Symbols.PLUS) { a, b -> canon(a + b) },
            inverse = Endo(Symbols.MINUS) { a -> canon(-a) }
        ),
        mul = CommutativeMonoid.of(
            identity = canon(BigInteger.ONE),
            op = BinOp(Symbols.ASTERISK) { a, b -> canon(a * b) }
        )
    )
}

/** Shorthand for a list of integer coefficients. */
internal fun ints(vararg cs: Int): List<BigInteger> =
    cs.map(Int::toBigInteger)
