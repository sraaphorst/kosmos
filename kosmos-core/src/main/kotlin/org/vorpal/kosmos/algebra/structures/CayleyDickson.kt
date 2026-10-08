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
 *
 * What the doubling preserves, given the precondition on `sigma` documented at [withSigma]:
 * - the conjugation `(a, b)* = (a*, -b)` is always an involutive anti-automorphism;
 * - if the base is commutative *and* has the trivial involution, the double is commutative and
 *   associative ([commutative]);
 * - if the base is commutative and associative (with any involution), the double is associative
 *   ([associative], [associativeSplit]): this is how ℂ, ℚ(i) and ℤ[i] double to the quaternion
 *   tier, and how ℂ doubles to the split-quaternions;
 * - if the base is merely associative, the double is alternative (ℍ ↦ 𝕆), and in general
 *   nothing more is preserved (𝕆 ↦ sedenions lose alternativity and acquire zero divisors).
 *
 * The norm of a double is `N(a, b) = (a, b)(a, b)* = N(a) + σ·N(b)`: positive definite for `σ = 1`
 * over a definite base, and isotropic (so the double has zero divisors) for `σ = -1`.
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

    /**
     * The universal property of the Cayley-Dickson construction, for *this* double:
     * the first-slot injection `a ↦ (a, 0)` is a ring monomorphism from the base into `CD<A>`.
     *
     * Use this when the doubled algebra is used as-is. When it is wrapped in a richer public
     * object (a `Field<Complex>`, a `RealNormedDivisionAlgebra<Octonion>`, ...), use the
     * companion [Companion.canonicalEmbedding] so that the morphism's codomain is that object.
     */
    fun canonicalEmbedding(): NonAssociativeRingMonomorphism<A, CD<A>> =
        canonicalEmbedding(base, this)

    companion object {
        /**
         * Constructor for the Cayley–Dickson doubling step with an arbitrary `sigma`:
         * ```text
         * (a, b)(c, d) = (ac - σ d* b, da + b c*)
         * ```
         *
         * Precondition on `sigma`. The conjugation `(a, b) ↦ (a*, -b)` is an involutive
         * anti-automorphism of the result, and the norm `N(x) = x x*` takes values in the base, only if
         * `sigma` is *central* (commutes with every element of the base) and *fixed by the base
         * involution* (`sigma* = sigma`). Both hold automatically for the safe factories:
         * - [usual] uses σ = 1 (the multiplicative identity),
         * - [split] uses σ = -1 (the additive inverse of 1),
         * - [commutative] takes any σ over a commutative base with the trivial involution.
         *
         * The general form exists for the generalized quaternion algebras `(a, b)_F`, obtained by
         * doubling twice with σ₁ = -a and σ₂ = -b over a field `F`, where `sigma` is a scalar and the
         * precondition is immediate. The precondition is not checkable here for an arbitrary base,
         * so it is the caller's responsibility; the laws are verified per instance by the test kit.
         */
        fun <A : Any> withSigma(
            base: NonAssociativeInvolutiveRing<A>,
            sigma: A
        ): CayleyDickson<A> = CayleyDickson(
            base = base,
            sigma = sigma
        )

        /**
         * The usual doubling of a *commutative and associative* involutive ring, typed as the
         * associative [InvolutiveRing] it is.
         *
         * If the base is commutative and associative, its double is associative (though no longer
         * commutative when the base involution is nontrivial). This is exactly the step
         * ℂ ↦ ℍ, ℚ(i) ↦ ℍ_ℚ, ℤ[i] ↦ ℤ⟨i, j, k⟩, and the reason the quaternion tier gets a
         * `Monoid` multiplication while the octonion tier gets only a `NonAssociativeMonoid`.
         *
         * The base's multiplication must be a [CommutativeMonoid]; this is checked at construction,
         * since associativity of the double depends on it.
         */
        fun <A : Any> associative(
            base: InvolutiveRing<A>
        ): InvolutiveRing<CD<A>> =
            associativeDouble(base, usual(base), "associative")

        /**
         * The split (`σ = -1`) doubling of a *commutative and associative* involutive ring, typed as
         * the associative [InvolutiveRing] it is.
         *
         * Associativity of the double needs only a commutative, associative base and a central `σ`
         * fixed by the involution, so it holds for `σ = -1` exactly as for `σ = 1`. This is the step
         * ℂ ↦ split-quaternions (`j² = k² = +1`), the associative counterpart of [split] in the same
         * way that [associative] is the associative counterpart of [usual].
         *
         * The base's multiplication must be a [CommutativeMonoid]; this is checked at construction.
         */
        fun <A : Any> associativeSplit(
            base: InvolutiveRing<A>
        ): InvolutiveRing<CD<A>> =
            associativeDouble(base, split(base), "associativeSplit")

        private fun <A : Any> associativeDouble(
            base: InvolutiveRing<A>,
            doubled: CayleyDickson<A>,
            factory: String
        ): InvolutiveRing<CD<A>> {
            require(base.mul is CommutativeMonoid<A>) {
                "CayleyDickson.$factory requires a commutative base: the double of a noncommutative " +
                    "associative algebra is only alternative."
            }
            return object : InvolutiveRing<CD<A>> {
                override val add = doubled.add
                override val mul: Monoid<CD<A>> = Monoid.of(
                    identity = doubled.mul.identity,
                    op = doubled.mul.op
                )
                override val conj = doubled.conj
                override val one = mul.identity

                override fun fromBigInt(n: BigInteger): CD<A> =
                    doubled.fromBigInt(n)
            }
        }

        /**
         * The usual doubling, `σ = 1`: the new unit `ℓ = (0, 1)` satisfies `ℓ² = -1`.
         */
        fun <A: Any> usual(
            base: NonAssociativeInvolutiveRing<A>,
        ): CayleyDickson<A> = withSigma(base, base.mul.identity)

        /**
         * The split doubling, `σ = -1`: the new unit `ℓ = (0, 1)` satisfies `ℓ² = +1`.
         *
         * The norm becomes `N(a, b) = N(a) - N(b)`, which is isotropic, so the result always has
         * zero divisors (for instance `(1 + ℓ)(1 - ℓ) = 0`).
         */
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
         * [doubled] must be an algebra whose operations *are* those of a Cayley-Dickson double of
         * [base] (typically a public object that wraps one and adds a reciprocal, a norm, etc.); this
         * cannot be verified structurally, and the morphism laws are checked per instance by the test
         * kit. For the raw double itself, prefer the instance method [CayleyDickson.canonicalEmbedding].
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
