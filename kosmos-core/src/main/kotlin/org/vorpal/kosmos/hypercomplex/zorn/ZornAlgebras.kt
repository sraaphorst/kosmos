package org.vorpal.kosmos.hypercomplex.zorn

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingIsomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.NonAssociativeAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras
import org.vorpal.kosmos.hypercomplex.splitoctonion.splitOctonion
import org.vorpal.kosmos.linear.instances.FixedTupleAlgebras
import org.vorpal.kosmos.linear.ops.DenseVecOps
import org.vorpal.kosmos.linear.ops.FixedTupleOps
import org.vorpal.kosmos.linear.values.Vec3

/**
 * Algebraic structures and utilities for the Zorn vector-matrices.
 *
 * Main constructions:
 * - [zornAdditiveGroup]: the coordinatewise additive group.
 * - [zornMultiplicativeMonoid]: the (nonassociative) Zorn product with identity `(1, 0, 0, 1)`.
 * - [zornRing]: the Zorn ring over a commutative base ring (nonassociative, alternative), with
 *   conjugation `(a, v, w, d) ↦ (d, −v, −w, a)`.
 * - [normSq]: the norm `N(X) = ad − v·w`, the "determinant" of a Zorn matrix.
 * - [zornLeftAction]: the entrywise scalar action.
 * - [zornModule]: `Zorn<R>` as a free `R`-module of rank 8.
 * - [zornAlgebra], [zornStarAlgebra]: as a nonassociative `R`-(star-)algebra.
 * - [zornRingWithReciprocal]: the same over a field, with partial reciprocals.
 * - [basis]: the matrix-unit basis `E_a, V₁, V₂, V₃, W₁, W₂, W₃, E_d`.
 *
 * Real conveniences:
 * - [RealZornAlgebra]: Zorn matrices over `Real`, with numerically guarded reciprocals.
 * - [RealZornStarAlgebra]: the real nonassociative star algebra.
 * - [ZornRealVectorSpace]: the eight-dimensional real vector space.
 *
 * Morphisms:
 * - [scalarEmbedding]: `R ↪ Zorn<R>`, `r ↦ (r, 0, 0, r)`.
 * - [splitOctonionToZornMonomorphism]: `SplitOctonion<R> ↪ Zorn<R>` over any commutative ring.
 * - [splitOctonionZornIsomorphism]: `SplitOctonion<F> ≅ Zorn<F>` when 2 is invertible.
 * - [RealSplitOctonionZornIsomorphism]: `𝕆′ ≅ Zorn<Real>`, between the real conveniences.
 *
 * Printables:
 * - [printableZorn]: one line, `[[a, (v₁, v₂, v₃)], [(w₁, w₂, w₃), d]]`.
 * - [printableZornPretty]: two lines, aligned as a formal 2 × 2 matrix.
 * - [printableZornReal], [printableZornRealStrict], [printableZornRealPretty]: over `Real`.
 *
 * Unlike [SplitOctonionAlgebras], nothing here is built from the Cayley-Dickson construction: the
 * product is written directly in terms of the dot and cross products on `R³`. That makes the
 * monomorphism a genuine comparison of two independent constructions.
 */
object ZornAlgebras {

    /**
     * The coordinatewise additive group of `Zorn<R>`, built from [group] on the scalar entries and
     * from `R³` on the vector entries.
     */
    fun <R : Any> zornAdditiveGroup(
        group: AbelianGroup<R>
    ): AbelianGroup<Zorn<R>> = object : AbelianGroup<Zorn<R>> {
        private val v3Group = FixedTupleAlgebras.dim3Group(group)
        private val v3Zero = v3Group.identity

        override val identity = group.run {
            Zorn(identity, v3Zero, v3Zero, identity)
        }

        override val op = BinOp<Zorn<R>>(Symbols.PLUS) { (a1, v1, w1, d1), (a2, v2, w2, d2) ->
            group.run {
                Zorn(op(a1, a2), v3Group(v1, v2), v3Group(w1, w2), op(d1, d2))
            }
        }

        override val inverse: Endo<Zorn<R>> = Endo(Symbols.MINUS) { (a, v, w, d) ->
            group.run {
                Zorn(inverse(a), v3Group.inverse(v), v3Group.inverse(w), inverse(d))
            }
        }
    }

    /**
     * The Zorn product over a commutative ring:
     * ```text
     * (a₁, v₁, w₁, d₁)(a₂, v₂, w₂, d₂) =
     *     (a₁a₂ + v₁·w₂,  a₁v₂ + d₂v₁ + w₁ × w₂,  a₂w₁ + d₁w₂ − v₁ × v₂,  d₁d₂ + w₁·v₂),
     * ```
     * with identity `(1, 0, 0, 1)`. It is alternative, but not associative and not commutative.
     * Without the cross-product terms this would be ordinary 2 × 2 matrix multiplication with the
     * dot product pairing the off-diagonal entries.
     */
    fun <R : Any> zornMultiplicativeMonoid(
        ring: CommutativeRing<R>
    ): NonAssociativeMonoid<Zorn<R>> = object : NonAssociativeMonoid<Zorn<R>> {
        override val identity = ring.run {
            val vec3z = Vec3(zero, zero, zero)
            Zorn(one, vec3z, vec3z, one)
        }

        private val mod3 = FixedTupleAlgebras.vec3Module(ring)
        private val dot = { v: Vec3<R>, w: Vec3<R> -> DenseVecOps.dot(ring, v, w) }
        private val cross = { v: Vec3<R>, w: Vec3<R> -> FixedTupleOps.cross(ring, v, w) }
        private val scale = { s: R, v: Vec3<R> -> mod3.leftAction(s, v) }

        override val op = BinOp<Zorn<R>>(Symbols.TIMES) { (a1, v1, w1, d1), (a2, v2, w2, d2) ->
            ring.run {
                Zorn(
                    add(mul(a1, a2), dot(v1, w2)),
                    mod3.add(mod3.add(scale(a1, v2), scale(d2, v1)), cross(w1, w2)),
                    mod3.add(mod3.add(scale(a2, w1), scale(d1, w2)), mod3.add.inverse(cross(v1, v2))),
                    add(mul(d1, d2), dot(w1, v2))
                )
            }
        }
    }

    /**
     * The Zorn vector-matrix ring over [base]: the product of [zornMultiplicativeMonoid], with the
     * conjugation
     * ```text
     * conj(a, v, w, d) = (d, −v, −w, a),
     * ```
     * an involutive anti-automorphism. It is alternative, flexible and Moufang, but not
     * associative, and it is isomorphic to the split-octonions whenever 2 is invertible in [base]
     * (see [splitOctonionZornIsomorphism]).
     */
    fun <R : Any> zornRing(
        base: CommutativeRing<R>
    ): NonAssociativeInvolutiveRing<Zorn<R>> {
        val mod3 = FixedTupleAlgebras.vec3Module(base)
        return NonAssociativeInvolutiveRing.of(
            add = zornAdditiveGroup(base.add),
            mul = zornMultiplicativeMonoid(base),
            conj = Endo(Symbols.CONJ) { (a, v, w, d) ->
                Zorn(
                    d,
                    mod3.add.inverse(v),
                    mod3.add.inverse(w),
                    a
                )
            }
        )
    }

    /**
     * The norm, the "determinant" of a Zorn matrix:
     * ```text
     * N(a, v, w, d) = ad − v·w.
     * ```
     * `X·conj(X) = conj(X)·X = N(X)·1`, and `N` is multiplicative, `N(XY) = N(X)N(Y)`: the Zorn
     * ring is a composition algebra. Under [splitOctonionToZornMonomorphism] it is the split norm
     * of signature `(4, 4)`.
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<Zorn<R>, R> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { (a, v, w, d) ->
            base.run { add(mul(a, d), add.inverse(DenseVecOps.dot(base, v, w))) }
        }

    /**
     * The entrywise scalar action
     * ```text
     * r·(a, v, w, d) = (ra, rv, rw, rd).
     * ```
     * This is also multiplication by the embedded scalar `(r, 0, 0, r)`, on either side: the
     * scalars lie in the nucleus and the center.
     */
    fun <R : Any> zornLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, Zorn<R>> {
        val mod3 = FixedTupleAlgebras.vec3Module(base)
        return LeftAction(Symbols.TRIANGLE_LEFT) { r, (a, v, w, d) ->
            base.run {
                Zorn(mul(r, a), mod3.leftAction(r, v), mod3.leftAction(r, w), mul(r, d))
            }
        }
    }

    /**
     * `Zorn<R>` as a free `R`-module of rank 8, `R ⊕ R³ ⊕ R³ ⊕ R`.
     */
    fun <R : Any> zornModule(
        base: CommutativeRing<R>
    ): RModule<R, Zorn<R>> =
        RModule.of(
            scalars = base,
            add = zornAdditiveGroup(base.add),
            leftAction = zornLeftAction(base)
        )

    /**
     * `Zorn<R>` as a nonassociative `R`-algebra.
     */
    fun <R : Any> zornAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeAlgebra<R, Zorn<R>> =
        NonAssociativeAlgebra.of(
            scalars = base,
            algebraRing = zornRing(base),
            leftAction = zornLeftAction(base)
        )

    /**
     * `Zorn<R>` as a nonassociative `R`-star-algebra under the conjugation
     * `(a, v, w, d) ↦ (d, −v, −w, a)`.
     */
    fun <R : Any> zornStarAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeStarAlgebra<R, Zorn<R>> =
        NonAssociativeStarAlgebra.of(
            scalars = base,
            involutiveRing = zornRing(base),
            leftAction = zornLeftAction(base)
        )

    /**
     * The matrix-unit basis of `Zorn<R>` over [base], in the order
     * ```text
     * E_a = (1, 0, 0, 0),  V₁, V₂, V₃ = (0, eᵢ, 0, 0),  W₁, W₂, W₃ = (0, 0, eᵢ, 0),  E_d = (0, 0, 0, 1).
     * ```
     * `E_a` and `E_d` are orthogonal idempotents with `E_a + E_d = 1`; all eight basis elements have
     * norm 0.
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<Zorn<R>> {
        val (o, l) = base.zero to base.one
        val zeroVec = Vec3(o, o, o)
        val units = listOf(Vec3(l, o, o), Vec3(o, l, o), Vec3(o, o, l))
        return listOf(Zorn(l, zeroVec, zeroVec, o)) +
            units.map { e -> Zorn(o, e, zeroVec, o) } +
            units.map { e -> Zorn(o, zeroVec, e, o) } +
            listOf(Zorn(o, zeroVec, zeroVec, l))
    }

    /**
     * Zorn matrices over a field, with reciprocals on the units.
     *
     * `X` is invertible iff `N(X) = ad − v·w` is invertible in [field], and then
     * ```text
     * X⁻¹ = conj(X) / N(X),
     * ```
     * a two-sided inverse despite nonassociativity, because the algebra is alternative.
     * [isZero] decides when a norm counts as zero; over `Real` a numerical test should be supplied
     * (see [RealZornAlgebra]).
     */
    fun <F : Any> zornRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): ZornRingWithReciprocal<F> =
        ZornRingWithReciprocal(field, isZero)

    class ZornRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : NonAssociativeInvolutiveRing<Zorn<F>> by zornRing(field),
        HasNormSq<Zorn<F>, F>,
        HasReciprocal<Zorn<F>> {

        private val leftAction = zornLeftAction(field)

        override val zero: Zorn<F>
            get() = add.identity

        override val normSq: UnaryOp<Zorn<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: Zorn<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<Zorn<F>> = Endo(Symbols.INVERSE) { x ->
            if (!hasReciprocal(x)) throw HasReciprocal.notInvertible(x, "the Zorn vector-matrices")
            leftAction(field.reciprocal(normSq(x)), conj(x))
        }
    }

    /**
     * The Zorn vector-matrices over the reals, a model of `𝕆′`.
     *
     * Reciprocals follow the floating-point policy of [HasReciprocal]: `X` is invertible iff `N(X)`
     * is finite and not approximately zero.
     */
    object RealZornAlgebra :
        NonAssociativeInvolutiveRing<Zorn<Real>> by zornRing(RealAlgebras.RealField),
        HasNormSq<Zorn<Real>, Real>,
        HasReciprocal<Zorn<Real>> {

        private val guarded = zornRingWithReciprocal(
            field = RealAlgebras.RealField,
            isZero = UnaryOp { n -> !n.isFinite() || RealAlgebras.eqRealApprox(n, 0.0) }
        )

        override val zero: Zorn<Real>
            get() = add.identity

        override val normSq: UnaryOp<Zorn<Real>, Real> = guarded.normSq

        override fun hasReciprocal(a: Zorn<Real>): Boolean =
            guarded.hasReciprocal(a)

        override val reciprocal: Endo<Zorn<Real>> = guarded.reciprocal

        /**
         * The matrix-unit basis `E_a, V₁, V₂, V₃, W₁, W₂, W₃, E_d`.
         */
        val basis: List<Zorn<Real>> =
            basis(RealAlgebras.RealField)
    }

    /**
     * The Zorn nonassociative star algebra over the reals.
     */
    object RealZornStarAlgebra :
        NonAssociativeInvolutiveRing<Zorn<Real>> by RealZornAlgebra,
        NonAssociativeStarAlgebra<Real, Zorn<Real>> {

        override val zero: Zorn<Real> = RealZornAlgebra.zero
        override val one: Zorn<Real> = RealZornAlgebra.one

        override val scalars = RealAlgebras.RealField

        override val leftAction: LeftAction<Real, Zorn<Real>> =
            zornLeftAction(RealAlgebras.RealField)
    }

    /**
     * `Zorn<Real>` as an eight-dimensional real vector space; the scalars act entrywise.
     */
    object ZornRealVectorSpace : FiniteVectorSpace<Real, Zorn<Real>> {
        override val scalars = RealAlgebras.RealField
        override val add = RealZornAlgebra.add
        override val dimension = 8
        override val leftAction: LeftAction<Real, Zorn<Real>> =
            RealZornStarAlgebra.leftAction
    }

    /**
     * The embedding of the base ring as the scalars, `r ↦ (r, 0, 0, r)`.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<R, Zorn<R>> {
        val zeroVec = Vec3(base.zero, base.zero, base.zero)
        return NonAssociativeRingMonomorphism.of(
            domain = base,
            codomain = zornRing(base),
            map = UnaryOp { r: R -> Zorn(r, zeroVec, zeroVec, r) }
        )
    }

    /**
     * The split-octonions as Zorn matrices over any commutative ring. Writing
     * `o = w + x·e₁ + y·e₂ + z·e₃ + u·e₄ + v·e₅ + s·e₆ + t·e₇`, with `X = (x, y, z)` and
     * `Y = (v, s, t)`,
     * ```text
     * φ(o) = (w + u,  X − Y,  −(X + Y),  w − u).
     * ```
     * `φ` is a unital ring homomorphism, commutes with conjugation, and sends the split norm
     * `w² + |X|² − u² − |Y|²` to `ad − v·w`.
     *
     * It is injective whenever 2 is not a zero divisor in [base], and an isomorphism when 2 is
     * invertible (see [splitOctonionZornIsomorphism]). Over ℤ it is injective but not surjective:
     * every image has `a ≡ d (mod 2)`, so for example `(1, 0, 0, 0)` is not in the image. Over 𝔽₂
     * it is not injective, since `φ(1) = φ(e₄) = (1, 0, 0, 1)`.
     */
    fun <R : Any> splitOctonionToZornMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<SplitOctonion<R>, Zorn<R>> =
        NonAssociativeRingMonomorphism.of(
            domain = SplitOctonionAlgebras.splitOctonionRing(base),
            codomain = zornRing(base),
            map = toZorn(base)
        )

    /**
     * The isomorphism `SplitOctonion<F> ≅ Zorn<F>` over a field in which 2 is invertible: the map
     * of [splitOctonionToZornMonomorphism], with inverse
     * ```text
     * (a, v, w, d) ↦ w = (a + d)/2,   u = (a − d)/2,   X = (v − w)/2,   Y = −(v + w)/2.
     * ```
     *
     * @throws IllegalArgumentException if 2 is not invertible in [field].
     */
    fun <F : Any> splitOctonionZornIsomorphism(
        field: Field<F>
    ): NonAssociativeRingIsomorphism<SplitOctonion<F>, Zorn<F>> {
        val two = field.add(field.one, field.one)
        require(field.hasReciprocal(two)) {
            "The split-octonions are isomorphic to the Zorn vector-matrices only when 2 is invertible in F."
        }
        val half = field.reciprocal(two)
        fun halfOf(a: F): F = field.mul(half, a)
        fun minus(a: F, b: F): F = field.add(a, field.add.inverse(b))

        val splitOctonions = SplitOctonionAlgebras.splitOctonionRing(field)
        val zorns = zornRing(field)

        return NonAssociativeRingIsomorphism.of(
            forward = NonAssociativeRingHomomorphism.of(
                domain = splitOctonions,
                codomain = zorns,
                map = toZorn(field)
            ),
            backward = NonAssociativeRingHomomorphism.of(
                domain = zorns,
                codomain = splitOctonions,
                map = UnaryOp(Symbols.PHI) { (a, v, w, d) ->
                    // X = (v − w)/2 and Y = −(v + w)/2, coordinate by coordinate.
                    fun xOf(i: Int): F = halfOf(minus(v[i], w[i]))
                    fun yOf(i: Int): F = halfOf(field.add.inverse(field.add(v[i], w[i])))
                    splitOctonion(
                        w = halfOf(field.add(a, d)),
                        x = xOf(0), y = xOf(1), z = xOf(2),
                        u = halfOf(minus(a, d)),
                        v = yOf(0), s = yOf(1), t = yOf(2)
                    )
                }
            )
        )
    }

    /**
     * The isomorphism `𝕆′ ≅ Zorn<Real>` of [splitOctonionZornIsomorphism], between
     * [SplitOctonionAlgebras.RealSplitOctonionAlgebra] and [RealZornAlgebra].
     */
    object RealSplitOctonionZornIsomorphism : NonAssociativeRingIsomorphism<SplitOctonion<Real>, Zorn<Real>> {
        private val generic = splitOctonionZornIsomorphism(RealAlgebras.RealField)

        override val domain = SplitOctonionAlgebras.RealSplitOctonionAlgebra
        override val codomain = RealZornAlgebra
        override val map: UnaryOp<SplitOctonion<Real>, Zorn<Real>> = generic.map
        override val backward: NonAssociativeRingHomomorphism<Zorn<Real>, SplitOctonion<Real>> =
            NonAssociativeRingHomomorphism.of(
                domain = RealZornAlgebra,
                codomain = SplitOctonionAlgebras.RealSplitOctonionAlgebra,
                map = generic.backward.map
            )
    }

    /**
     * Eqs
     */
    fun <R : Any> eqZorn(eqR: Eq<R>): Eq<Zorn<R>> =
        Eq { (a1, v1, w1, d1), (a2, v2, w2, d2) ->
            fun eqVec(p: Vec3<R>, r: Vec3<R>) = eqR(p.x, r.x) && eqR(p.y, r.y) && eqR(p.z, r.z)
            eqR(a1, a2) && eqVec(v1, v2) && eqVec(w1, w2) && eqR(d1, d2)
        }

    /**
     * Strict equality on real Zorn matrices.
     */
    val eqZornStrict: Eq<Zorn<Real>> =
        eqZorn(RealAlgebras.eqRealStrict)

    /**
     * Approximate equality on real Zorn matrices.
     */
    val eqZorn: Eq<Zorn<Real>> =
        eqZorn(RealAlgebras.eqRealApprox)

    /**
     * Printables
     */

    /**
     * A one-line rendering of a Zorn matrix as its formal 2 × 2 array, row by row:
     * ```text
     * [[a, (v₁, v₂, v₃)], [(w₁, w₂, w₃), d]]
     * ```
     * with each entry rendered by [prR].
     */
    fun <R : Any> printableZorn(
        prR: Printable<R>
    ): Printable<Zorn<R>> =
        Printable { (a, v, w, d) ->
            "[[${prR(a)}, ${renderVec3(prR, v)}], [${renderVec3(prR, w)}, ${prR(d)}]]"
        }

    /**
     * A two-line rendering of a Zorn matrix with its columns aligned, e.g.
     * ```text
     * ⎛          1  (2, 0, -3) ⎞
     * ⎝ (0, -1, 4)          -5 ⎠
     * ```
     * with each entry rendered by [prR].
     */
    fun <R : Any> printableZornPretty(
        prR: Printable<R>
    ): Printable<Zorn<R>> =
        Printable { (a, v, w, d) ->
            val (topLeft, topRight) = prR(a) to renderVec3(prR, v)
            val (bottomLeft, bottomRight) = renderVec3(prR, w) to prR(d)
            val leftWidth = maxOf(topLeft.length, bottomLeft.length)
            val rightWidth = maxOf(topRight.length, bottomRight.length)
            "⎛ ${topLeft.padStart(leftWidth)}  ${topRight.padStart(rightWidth)} ⎞\n" +
                "⎝ ${bottomLeft.padStart(leftWidth)}  ${bottomRight.padStart(rightWidth)} ⎠"
        }

    /**
     * One-line real Zorn matrices, with entries rendered by [RealAlgebras.printableReal].
     */
    val printableZornReal: Printable<Zorn<Real>> =
        printableZorn(RealAlgebras.printableReal)

    /**
     * One-line real Zorn matrices, with entries rendered by [RealAlgebras.printableRealStrict].
     */
    val printableZornRealStrict: Printable<Zorn<Real>> =
        printableZorn(RealAlgebras.printableRealStrict)

    /**
     * Two-line real Zorn matrices, with entries rendered by [RealAlgebras.printableRealPretty].
     */
    val printableZornRealPretty: Printable<Zorn<Real>> =
        printableZornPretty(RealAlgebras.printableRealPretty)

    private fun <R : Any> renderVec3(prR: Printable<R>, v: Vec3<R>): String =
        "(${prR(v.x)}, ${prR(v.y)}, ${prR(v.z)})"

    /**
     * The map underlying [splitOctonionToZornMonomorphism].
     */
    private fun <R : Any> toZorn(
        base: CommutativeRing<R>
    ): UnaryOp<SplitOctonion<R>, Zorn<R>> {
        val mod3 = FixedTupleAlgebras.vec3Module(base)
        return UnaryOp(Symbols.PHI) { o ->
            base.run {
                val x = Vec3(o.x, o.y, o.z)
                val y = Vec3(o.v, o.s, o.t)
                Zorn(
                    add(o.w, o.u),
                    mod3.add(x, mod3.add.inverse(y)),
                    mod3.add.inverse(mod3.add(x, y)),
                    add(o.w, add.inverse(o.u))
                )
            }
        }
    }
}
