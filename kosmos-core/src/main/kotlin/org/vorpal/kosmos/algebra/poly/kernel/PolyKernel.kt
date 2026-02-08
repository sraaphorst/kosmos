package org.vorpal.kosmos.algebra.poly.kernel

import org.vorpal.kosmos.algebra.poly.Poly
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

internal object PolyKernel {
    /**
     * Given:
     * - A [Poly] over [A]
     * - An [Eq] over [A]
     * - A [zeroA] in [A] representing zero
     * find the normalization of [Poly], i.e. the representation where the coefficients of [Poly]
     * do not end in [zeroA].
     */
    fun <A : Any> normalize(
        p: Poly<A>,
        eqA: Eq<A>,
        zeroA: A
    ): Poly<A> {
        val cs = p.coeffs
        if (cs.isEmpty()) return Poly.zero()

        var last = cs.size - 1
        while (last >= 0 && eqA(cs[last], zeroA)) {
            last -= 1
        }

        if (last < 0) return Poly.zero()
        return Poly.ofUnsafe(cs.subList(0, last + 1))
    }

    /**
     * Create a constant [Poly] term where the value is simply [a].
     */
    fun <A : Any> constant(
        a: A,
        eqA : Eq<A>,
        zeroA : A
    ): Poly<A> {
        val p = Poly.ofUnsafe(listOf(a))
        return normalize(p, eqA, zeroA)
    }

    /**
     * Create a [Poly] that represents the term `x`.
     */
    fun <A : Any> x(
        zeroA: A,
        oneA: A,
        eqA: Eq<A>
    ): Poly<A> =
        normalize(Poly.ofUnsafe(listOf(zeroA, oneA)), eqA, zeroA)

    /**
     * Create a [Poly] that represents the monomial:
     * ```
     * a * x^k
     * ```
     */
    fun <A : Any> monomial(
        a: A,
        k: Int,
        eqA: Eq<A>,
        zeroA: A
    ): Poly<A> {
        require(k >= 0) { "k must be non-negative, got: $k" }
        if (k == 0) return constant(a, eqA, zeroA)

        val cs = List(k) { zeroA } + a
        val p = Poly.ofUnsafe(cs)
        return normalize(p, eqA, zeroA)
    }

    /**
     * Given two [Poly] over [A], find their normalized sum.
     */
    fun <A : Any> addPoly(
        p: Poly<A>,
        q: Poly<A>,
        addA: BinOp<A>,
        eqA: Eq<A>,
        zeroA: A
    ): Poly<A> {
        val pc = p.coeffs
        val qc = q.coeffs
        val n = maxOf(pc.size, qc.size)

        if (n == 0) return Poly.zero()

        val out = (0 until n).map { i ->
            val a = if (i < pc.size) pc[i] else zeroA
            val b = if (i < qc.size) qc[i] else zeroA
            addA(a, b)
        }

        return normalize(Poly.ofUnsafe(out), eqA, zeroA)
    }

    /**
     * Given two [Poly] over [A], find their normalized product.
     */
    fun <A : Any> mulPoly(
        p: Poly<A>,
        q: Poly<A>,
        addA: BinOp<A>,
        mulA: BinOp<A>,
        eqA: Eq<A>,
        zeroA: A
    ): Poly<A> {
        val pc = p.coeffs
        val qc = q.coeffs

        if (pc.isEmpty() || qc.isEmpty()) return Poly.zero()

        val outSize = pc.size + qc.size - 1
        val out = MutableList(outSize) { zeroA }
        pc.indices.forEach { i ->
            qc.indices.forEach { j ->
                val idx = i + j
                val prod = mulA(pc[i], qc[j])
                out[idx] = addA(out[idx], prod)
            }
        }

        return normalize(Poly.ofUnsafe(out), eqA, zeroA)
    }

    /**
     * Given a [Poly] over [A] and an [Endo] that finds the additive inverse of a coefficient,
     * calculate the negative inverse of the [Poly] and normalize it.
     */
    fun <A : Any> negPoly(
        p: Poly<A>,
        negA: Endo<A>,
        eqA: Eq<A>,
        zeroA: A
    ): Poly<A> {
        val cs = p.coeffs
        if (cs.isEmpty()) return Poly.zero()
        val out = cs.map(negA::invoke)
        return normalize(Poly.ofUnsafe(out), eqA, zeroA)
    }
}
