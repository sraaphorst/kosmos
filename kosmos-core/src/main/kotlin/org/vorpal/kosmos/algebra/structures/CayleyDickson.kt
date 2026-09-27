package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

/**
 * The Cayley-Dickson elements: a Pair over a NonAssociativeInvolutiveRing<A>.
 */
data class CD<A : Any>(val a: A, val b: A) {
    companion object {
        fun <A : Any> eq(eqA: Eq<A>): Eq<CD<A>> = Eq { x, y ->
            eqA(x.a, y.a) && eqA(x.b, y.b)
        }
    }
}

/**
 * The generic doubling step.
 *
 * It always gives (at least) a NonAssociativeInvolutiveRing<CD<A>>.
 *
 * It doesn't try to decide "this is a Field, this is only a Ring, etc."
 * We will do that per stage when we know the algebraic facts by wrapping
 * it, e.g., in a Field<Complex>.
 *
 * We include `sigma` here to be able to use the Cayley-Dickson process to build the split versions.
 */
class CayleyDickson<A : Any> private constructor(
    private val base: NonAssociativeInvolutiveRing<A>,
    val sigma: A
) : NonAssociativeInvolutiveRing<CD<A>> {

    override val add: AbelianGroup<CD<A>> = AbelianGroup.of<CD<A>>(
        identity = CD(base.add.identity, base.add.identity),
        op = BinOp(Symbols.PLUS) { (a, b), (c, d) ->
            CD(
                base.add(a, c),
                base.add(b, d)
            )
        },
        inverse = Endo(Symbols.MINUS) { (a, b) ->
            CD(
                base.add.inverse(a),
                base.add.inverse(b)
            )
        }
    )

    /**
     * We only ever use `sigma` via left multiplication.
     * - If the base is commutative (ℤ, ℚ, ℝ, ℂ, finite fields, etc.), this is never an issue.
     * - If the base is noncommutative, Cayley–Dickson behaves as expected when `sigma`lies in the center.
     */
    override val mul: NonAssociativeMonoid<CD<A>> = NonAssociativeMonoid.of(
        identity = CD(base.mul.identity, base.add.identity),
        op = BinOp<CD<A>>(Symbols.ASTERISK) { (a, b), (c, d) ->
            // (a, b)(c, d) = (ac - sigma * d* b, da + b c*)
            val ac = base.mul(a, c)
            val dbs = base.mul(base.conj(d), b)
            val sigDbs = base.mul(sigma, dbs)
            val first = base.add(ac, base.add.inverse(sigDbs))

            val da = base.mul(d, a)
            val bcs = base.mul(b, base.conj(c))
            val second = base.add(da, bcs)

            CD(first, second)
        }
    )

    override fun fromBigInt(n: BigInteger): CD<A> =
        CD(base.fromBigInt(n), base.add.identity)

    override val conj = Endo<CD<A>>(Symbols.CONJ) { (a, b) ->
        CD(base.conj(a), base.add.inverse(b))
    }

    companion object {
        /**
         * Constructor for the Cayley–Dickson doubling step.
         *
         * - `usual` uses σ = 1 (the multiplicative identity)
         * - `split` uses σ = -1 (the additive inverse of 1)
         *
         * These names correspond to the usual vs split forms over ℝ and many other bases.
         */
        fun <A : Any> withSigma(
            base: NonAssociativeInvolutiveRing<A>,
            sigma: A
        ): CayleyDickson<A> = CayleyDickson(
            base = base,
            sigma = sigma
        )

        fun <A: Any> usual(
            base: NonAssociativeInvolutiveRing<A>,
        ): CayleyDickson<A> = withSigma(base, base.mul.identity)

        fun <A: Any> split(
            base: NonAssociativeInvolutiveRing<A>
        ): CayleyDickson<A> {
            val negOne = base.add.inverse(base.mul.identity)
            return withSigma(base, negOne)
        }

        /**
         * The Cayley-Dickson doubling of a commutative ring equipped with the *trivial* involution.
         *
         * With `x* = x` on the base, the doubling formula collapses to
         * ```text
         * (a, b)(c, d) = (ac - σbd, ad + bc),
         * ```
         * i.e. writing `(a, b) = a + bx`, this is the commutative quadratic extension
         * ```text
         * R[x] / (x² + σ)
         * ```
         * which is again commutative and associative, so the result is typed as a
         * [CommutativeInvolutiveRing]. Its involution is the Cayley-Dickson conjugation
         * `a + bx ↦ a - bx`.
         *
         * The three classical choices of `σ` give:
         * - `σ = 1`:  `x² = -1` (complex type: ℂ over ℝ, the bicomplex numbers over ℂ).
         * - `σ = -1`: `x² = 1`  (split type: the split-complex numbers).
         * - `σ = 0`:  `x² = 0`  (the dual numbers).
         *
         * Contrast [usual] and [split] on a base with a *nontrivial* involution (e.g. ℂ with complex
         * conjugation), which produce noncommutative algebras such as the quaternions.
         */
        fun <A : Any> commutative(
            base: CommutativeRing<A>,
            sigma: A
        ): CommutativeInvolutiveRing<CD<A>> {
            val doubled = withSigma(CommutativeInvolutiveRing.trivial(base), sigma)
            val ring = CommutativeRing.of(
                add = doubled.add,
                mul = CommutativeMonoid.of(
                    identity = doubled.mul.identity,
                    op = doubled.mul.op
                )
            )
            return object : CommutativeInvolutiveRing<CD<A>> {
                override val add = ring.add
                override val mul = ring.mul
                override val conj = doubled.conj
                override val one = ring.one

                override fun fromBigInt(n: BigInteger): CD<A> =
                    doubled.fromBigInt(n)
            }
        }

        /**
         * The standard basis of a Cayley-Dickson double, given a basis of the base.
         *
         * If `e₀, …, e_{n-1}` is a basis of `A` (as a module over the scalars), then
         * ```text
         * (e₀, 0), …, (e_{n-1}, 0), (0, e₀), …, (0, e_{n-1})
         * ```
         * is a basis of `CD<A>`. Iterating from `ℝ` with basis `[1]` reproduces the usual
         * orderings of the tower:
         * - `ℂ`: `1, i`
         * - `ℍ`: `1, i, j, k`
         * - `𝕆`: `1, e₁, …, e₇`
         */
        fun <A : Any> doubledBasis(
            zero: A,
            baseBasis: List<A>
        ): List<CD<A>> =
            baseBasis.map { e -> CD(e, zero) } + baseBasis.map { e -> CD(zero, e) }

        /**
         * The universal property of the Cayley-Dickson construction:
         *
         * For any `A` and `CD<A>`, the first-slot injection `a ↦ (a, 0)` is a ring monomorphism where 0 is
         * the additive identity of `A`.
         *
         * It belongs with the construction itself, not with any specific instance.
         */
        fun <A : Any> canonicalEmbedding(
            base: NonAssociativeInvolutiveRing<A>,
            doubled: NonAssociativeInvolutiveRing<CD<A>>
        ): NonAssociativeRingMonomorphism<A, CD<A>> = NonAssociativeRingMonomorphism.of(
                base,
                doubled,
                UnaryOp { a -> CD(a, base.add.identity) }
            )
    }
}
