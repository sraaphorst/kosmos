package org.vorpal.kosmos.hypercomplex.sextonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.NonAssociativeAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.hypercomplex.zorn.Zorn
import org.vorpal.kosmos.linear.instances.FixedMatAlgebras
import org.vorpal.kosmos.linear.instances.FixedVecAlgebras
import org.vorpal.kosmos.linear.ops.FixedMatOps
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.Vec3

/**
 * Algebraic structures for the sextonions, stored as pairs `(A, ν) ∈ M₂(R) ⊕ R²`.
 *
 * Main constructions:
 * - [sextonionAdditiveGroup]: the componentwise additive group.
 * - [sextonionMultiplicativeMonoid]: the (nonassociative) product
 *   `(A, ν)(B, μ) = (AB, μA + ν·adj(B))` with identity `(I, 0)`.
 * - [sextonionRing]: the sextonion ring over a commutative base ring (nonassociative,
 *   alternative), with conjugation `(A, ν) ↦ (adj A, −ν)`.
 *
 * Everything is built from the fixed-size 2 × 2 operations in [FixedMatOps]
 * (`matMul`, `vecMat`, `adjugate`) and the groups in [FixedMatAlgebras] and [FixedVecAlgebras].
 * Nothing here goes through the Cayley-Dickson construction or the Zorn product.
 */
object SextonionAlgebras {

    /**
     * The componentwise additive group of `Sextonion<R>`:
     * ```text
     * (A, ν) + (B, μ) = (A + B, ν + μ),   0 = (0, 0),   −(A, ν) = (−A, −ν),
     * ```
     * built from [group] on the entries.
     */
    fun <R : Any> sextonionAdditiveGroup(
        group: AbelianGroup<R>
    ): AbelianGroup<Sextonion<R>> = object : AbelianGroup<Sextonion<R>> {
        private val mat2Add = FixedMatAlgebras.mat2AdditiveGroup(group)
        private val vec2Add = FixedVecAlgebras.dim2Group(group)

        override val identity = Sextonion(mat2Add.identity, vec2Add.identity)

        override val op = BinOp<Sextonion<R>>(Symbols.PLUS) { (m1, v1), (m2, v2) ->
            Sextonion(mat2Add(m1, m2), vec2Add(v1, v2))
        }

        override val inverse: Endo<Sextonion<R>> = Endo(Symbols.MINUS) { (m, v) ->
            Sextonion(mat2Add.inverse(m), vec2Add.inverse(v))
        }
    }

    /**
     * The sextonion product over a commutative ring:
     * ```text
     * (A, ν)(B, μ) = (AB, μA + ν·adj(B)),
     * ```
     * with identity `(I, 0)`. Here `μA` and `ν·adj(B)` are row vectors times matrices.
     *
     * Note the order in the vector part: the right factor's vector `μ` multiplies the left
     * factor's matrix `A`. This is what makes the product bilinear but not associative. Using
     * `adj(B)` rather than `B` is what makes it alternative.
     *
     * The ring must be commutative because the adjugate is.
     */
    fun <R : Any> sextonionMultiplicativeMonoid(
        ring: CommutativeRing<R>
    ): NonAssociativeMonoid<Sextonion<R>> = object : NonAssociativeMonoid<Sextonion<R>> {
        private val vec2Add = FixedVecAlgebras.dim2Group(ring.add)

        override val identity = Sextonion(FixedMatOps.identity(ring), vec2Add.identity)

        override val op = BinOp<Sextonion<R>>(Symbols.TIMES) { (a, nu), (b, mu) ->
            FixedMatOps.run {
                Sextonion(
                    matMul(ring, a, b),
                    vec2Add(vecMat(ring, mu, a), vecMat(ring, nu, adjugate(ring, b)))
                )
            }
        }
    }

    /**
     * The sextonion ring over [base]: the product of [sextonionMultiplicativeMonoid], with the
     * conjugation
     * ```text
     * conj(A, ν) = (adj A, −ν),
     * ```
     * an involutive anti-automorphism. It is alternative but not associative. Its norm
     * `N(A, ν) = det A` is multiplicative but degenerate: it vanishes on the ideal of pairs
     * `(0, ν)`.
     */
    fun <R : Any> sextonionRing(
        base: CommutativeRing<R>
    ): NonAssociativeInvolutiveRing<Sextonion<R>> {
        val vec2Add = FixedVecAlgebras.dim2Group(base.add)
        return NonAssociativeInvolutiveRing.of(
            add = sextonionAdditiveGroup(base.add),
            mul = sextonionMultiplicativeMonoid(base),
            conj = Endo(Symbols.CONJ) { (m, v) ->
                Sextonion(FixedMatOps.adjugate(base, m), vec2Add.inverse(v))
            }
        )
    }

    /**
     * The norm of the sextonion
     * ```text
     * N(m, v) = det(m).
     * ```
     * Note that it is multiplicative:
     * ```text
     * N(ST) = N(S)N(T)
     * ```
     * but degenerate:
     * ```
     * N(0, v) = 0
     * ```
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<Sextonion<R>, R> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { (m, _) ->
            FixedMatOps.det(base, m)
        }

    /**
     * The entrywise scalar action
     * ```text
     * r·(m, v) = (rm, rv)
     * ```
     * Note that the left action takes `r ↦ (rI, 0)`, which is in the nucleus and the center.
     */
    fun <R : Any> sextonionLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, Sextonion<R>> {
        val mat2LeftAction = FixedMatAlgebras.mat2LeftAction(base)
        val vec2LeftAction = FixedVecAlgebras.vec2Module(base).leftAction
        return LeftAction(Symbols.TRIANGLE_RIGHT) { r, (m, v) ->
            Sextonion(
                mat2LeftAction(r, m),
                vec2LeftAction(r, v)
            )
        }
    }

    /**
     * `Sextonion<R>` as a free `R`-module of rank 6, `M₂(R) ⊕ R² ≅ R⁴ ⊕ R² = R⁶`.
     */
    fun <R : Any> sextonionModule(
        base: CommutativeRing<R>
    ): RModule<R, Sextonion<R>> =
        RModule.of(
            scalars = base,
            add = sextonionAdditiveGroup(base.add),
            leftAction = sextonionLeftAction(base)
        )

    /**
     * `Sextionion<R>` as a nonassociative `R`-algebra.
     */
    fun <R : Any> sextonionAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeAlgebra<R, Sextonion<R>> =
        NonAssociativeAlgebra.of(
            scalars = base,
            algebraRing = sextonionRing(base),
            leftAction = sextonionLeftAction(base)
        )

    /**
     * `Sextonion<R>` as a nonassociative `R`-star algebra under the conjugation
     * ```text
     * (A, ν) ↦ (adj A, −ν)
     * ```
     */
    fun <R : Any> sextonionStarAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeStarAlgebra<R, Sextonion<R>> =
        NonAssociativeStarAlgebra.of(
            scalars = base,
            involutiveRing = sextonionRing(base),
            leftAction = sextonionLeftAction(base)
        )

    /**
     * The basis of `Sextonion<R>` over `base`, in the order:
     * ```text
     * E₁₁, E₁₂, E₂₁, E₂₂ = (Eᵢⱼ, 0)
     * n₁ = (0, (1, 0))
     * n₂ = (0, (0, 1))
     * ```
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<Sextonion<R>> {
        val (z, o) = base.zero to base.one
        val zeroMat = FixedMatOps.zero(base)
        val zeroVec = FixedVecAlgebras.vec2Module(base).add.identity
        return listOf(
            Sextonion(Mat2(o, z, z, z), zeroVec),
            Sextonion(Mat2(z, o, z, z), zeroVec),
            Sextonion(Mat2(z, z, o, z), zeroVec),
            Sextonion(Mat2(z, z, z, o), zeroVec),
            Sextonion(zeroMat, Vec2(o, z)),
            Sextonion(zeroMat, Vec2(z, o))
        )
    }

    /**
     * Sextonions over a field, with reciprocals on the units.
     * (A, v) is invertible iff A is invertible, i.e. det(A) ≠ 0.
     */
    fun <F : Any> sextonionRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): SextonionRingWithReciprocal<F> =
        SextonionRingWithReciprocal(field, isZero)

    class SextonionRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : NonAssociativeInvolutiveRing<Sextonion<F>> by sextonionRing(field),
        HasNormSq<Sextonion<F>, F>,
        HasReciprocal<Sextonion<F>> {

        private val leftAction = sextonionLeftAction(field)

        override val zero: Sextonion<F>
            get() = add.identity

        override val normSq: UnaryOp<Sextonion<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: Sextonion<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<Sextonion<F>> = Endo(Symbols.INVERSE) { s ->
            if (!hasReciprocal(s)) throw HasReciprocal.notInvertible(s, "the sextonions")
            leftAction(field.reciprocal(normSq(s)), conj(s))
        }
    }

    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<R, Sextonion<R>> {
        val z = base.zero
        val zeroVec: Vec2<R> = Vec2(z, z)
        return NonAssociativeRingMonomorphism.of(
            domain = base,
            codomain = sextonionRing(base),
            map = UnaryOp { r: R -> Sextonion(Mat2(r, z, z, r), zeroVec)}
        )
    }

    /**
     * Eqs
     */

    fun <R : Any> eqSextonion(eqR: Eq<R>): Eq<Sextonion<R>> {
        val eqMat = FixedMatAlgebras.eqMat2(eqR)
        val eqVec = FixedVecAlgebras.eqVec2(eqR)
        return Eq { (m1, v1), (m2, v2) -> eqMat(m1, m2) && eqVec(v1, v2) }
    }
}
