package org.vorpal.kosmos.bridge

import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.combinatorics.Permutation
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.PermMat

/**
 * A functorial bridge between [Permutation] over the set `{0, ..., n-1}` and [PermMat].
 */
object PermutationBridge {
    fun Permutation<Int>.toPermMat(): PermMat {
        val n = domain.size
        require(domain.toSet() == (0 until n).toSet()) {
            "PermutationBridge requires domain = {0, ..., n-1}. Got: $domain"
        }
        return PermMat.of(IntArray(domain.size, this::get), copy = false)
    }

    fun PermMat.toPermutation(): Permutation<Int> =
        Permutation.of(FiniteSet.ordered(0 until size),
            (0 until size).associateWith { this[it] })

    fun <A : Any> PermMat.toDenseMat(semiring: Semiring<A>): DenseMat<A> {
        val n = size
        val zero = semiring.add.identity
        val one = semiring.mul.identity

        return DenseMat.tabulate(n, n) { r, c ->
            if (this[c] == r) one else zero
        }
    }

    fun <A : Any> MatLike<A>.toPermMat(
        zero: A,
        one: A,
        eq: Eq<A> = Eq.default()
    ): Option<PermMat> {
        if (rows != cols) return Option.None
        val n = rows

        val p = IntArray(n) { -1 }
        val seenRow = BooleanArray(n)

        var c = 0
        while (c < n) {
            var found = -1

            var r = 0
            while (r < n) {
                val a = this[r, c]
                if (!eq(a, zero) && !eq(a, one)) return Option.None
                if (eq(a, one)) {
                    if (found != -1) return Option.None
                    found = r
                }
                r += 1
            }

            if (found == -1) return Option.None
            if (seenRow[found]) return Option.None

            seenRow[found] = true
            p[c] = found
            c += 1
        }

        return Option.Some(PermMat.of(p, copy = false))
    }

    fun <A : Any> MatLike<A>.toPermMat(
        semiring: Semiring<A>,
        eq: Eq<A> = Eq.default()
    ): Option<PermMat> = toPermMat(
        zero = semiring.add.identity,
        one = semiring.mul.identity,
        eq = eq
    )
}
