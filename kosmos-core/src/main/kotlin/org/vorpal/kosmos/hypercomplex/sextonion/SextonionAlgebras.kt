package org.vorpal.kosmos.hypercomplex.sextonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
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
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras.splitQuaternionRing
import org.vorpal.kosmos.linear.instances.FixedMatAlgebras
import org.vorpal.kosmos.linear.instances.FixedVecAlgebras
import org.vorpal.kosmos.linear.ops.FixedMatOps
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2

/**
 * Algebraic structures for the sextonions, stored as pairs `(A, ν) ∈ M₂(R) ⊕ R²`.
 *
 * Main constructions:
 * - [sextonionAdditiveGroup]: the componentwise additive group.
 * - [sextonionMultiplicativeMonoid]: the (nonassociative) product
 *   `(A, ν)(B, μ) = (AB, μA + ν·adj(B))` with identity `(I, 0)`.
 * - [sextonionRing]: the sextonion ring over a commutative base ring (nonassociative,
 *   alternative), with conjugation `(A, ν) ↦ (adj A, −ν)`.
 * - [normSq]: the norm `N(A, ν) = det A`, multiplicative but degenerate.
 * - [sextonionLeftAction]: the entrywise scalar action.
 * - [sextonionModule]: `Sextonion<R>` as a free `R`-module of rank 6.
 * - [sextonionAlgebra], [sextonionStarAlgebra]: as a nonassociative `R`-(star-)algebra.
 * - [sextonionRingWithReciprocal]: the same over a field, with partial reciprocals.
 * - [basis]: the basis `E₁₁, E₁₂, E₂₁, E₂₂, n₁, n₂`.
 *
 * Morphisms:
 * - [scalarEmbedding]: `R ↪ Sextonion<R>`, `r ↦ (rI, 0)`.
 * - [mat2ToSextonionMonomorphism] (`ι`) and [sextonionMat2Retraction] (`ρ`): the split extension
 *   `S(R) = M₂(R) ⋉ J`, with `J = { (0, ν) }` square-zero and `ρ ∘ ι = id`.
 * - [splitQuaternionToSextonionMonomorphism] and [sextonionToSplitQuaternionHomomorphism] (the
 *   latter needs 2 invertible): the same through the split-quaternions.
 * - The embeddings into the Zorn vector-matrices and the split-octonions live with the bigger
 *   algebra: [org.vorpal.kosmos.hypercomplex.zorn.ZornAlgebras.sextonionToZornMonomorphism] and
 *   [org.vorpal.kosmos.hypercomplex.zorn.ZornAlgebras.sextonionToSplitOctonionMonomorphism].
 *
 * Equality and printing:
 * - [eqSextonion]: componentwise equality from an equality on the entries.
 * - [printableSextonion]: one line, `([[a, b], [c, d]], (x, y))`.
 * - [printableSextonionPretty]: two lines, the matrix aligned and the vector after it.
 *
 * Everything is built from the fixed-size 2 × 2 operations in [FixedMatOps]
 * (`matMul`, `vecMat`, `adjugate`) and the groups in [FixedMatAlgebras] and [FixedVecAlgebras].
 * Nothing here goes through the Cayley-Dickson construction or the Zorn product, so the
 * embedding into the Zorn matrices is a genuine comparison of two independent constructions.
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
     * The norm
     * ```text
     * N(A, ν) = det A,
     * ```
     * with `x·conj(x) = conj(x)·x = N(x)·1`. It is multiplicative, `N(xy) = N(x)N(y)`, but
     * degenerate: it ignores the vector part entirely, `N(A, ν) = N(A, 0)`, so it vanishes on the
     * whole ideal `V` of pairs `(0, ν)`. That is why the sextonions are not a composition algebra.
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
     * r ⊳ (A, ν) = (rA, rν).
     * ```
     * This is also multiplication by the embedded scalar `(rI, 0)` on either side:
     * ```text
     * (rI, 0)(A, ν) = (rA, ν·(rI)) = (rA, rν),   (A, ν)(rI, 0) = (rA, ν·adj(rI)) = (rA, rν).
     * ```
     * Over a field, the scalars `F·(I, 0)` are exactly the nucleus and the center.
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
     * `Sextonion<R>` as a nonassociative `R`-algebra.
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
     * The basis of `Sextonion<R>` over [base], in the order of [Sextonion.coefficients]:
     * ```text
     * E₁₁, E₁₂, E₂₁, E₂₂ = (Eᵢⱼ, 0),   n₁ = (0, (1, 0)),   n₂ = (0, (0, 1)).
     * ```
     * `E₁₁` and `E₂₂` are orthogonal idempotents with `E₁₁ + E₂₂ = 1`, and the `Eᵢⱼ` multiply as
     * matrix units. `n₁` and `n₂` span the ideal `V`: `nᵢnⱼ = 0` and `N(nᵢ) = 0`.
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<Sextonion<R>> {
        val (z, o) = base.zero to base.one
        val zeroMat = FixedMatOps.zero(base)
        val zeroVec = FixedVecAlgebras.dim2Group(base.add).identity
        return FixedMatAlgebras.basis(base).map { e -> Sextonion(e, zeroVec) } +
            listOf(Sextonion(zeroMat, Vec2(o, z)), Sextonion(zeroMat, Vec2(z, o)))
    }

    /**
     * Sextonions over a field, with reciprocals on the units.
     *
     * `x = (A, ν)` is invertible iff `N(x) = det A` is invertible in [field]; the vector part `ν`
     * plays no role. Then
     * ```text
     * x⁻¹ = conj(x) / N(x) = (A⁻¹, −ν / det A),
     * ```
     * a two-sided inverse despite nonassociativity, because the algebra is alternative.
     *
     * @param isZero decides when a norm counts as zero (exact equality by default; pass a tolerance
     *   for floating-point fields).
     */
    fun <F : Any> sextonionRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): SextonionRingWithReciprocal<F> =
        SextonionRingWithReciprocal(field, isZero)

    /**
     * The sextonion ring of [sextonionRing] over [field], with [normSq] and the partial reciprocal
     * `x⁻¹ = conj(x) / N(x)`. Build it with [sextonionRingWithReciprocal].
     */
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

    /**
     * The embedding of the 2 × 2 matrices as the matrix part of the sextonions, over any
     * commutative ring:
     * ```text
     * ι : M₂(R) → S(R),   A ↦ (A, 0).
     * ```
     * It is a unital ring homomorphism because `(A, 0)(B, 0) = (AB, 0·A + 0·adj B) = (AB, 0)` and
     * `ι(I) = (I, 0)` is the identity. It is clearly injective.
     *
     * Together with [sextonionMat2Retraction] it exhibits the sextonions as a split extension
     * ```text
     * S(R) = ι(M₂(R)) ⊕ J,   J = { (0, ν) },   J² = 0,
     * ```
     * of `M₂(R)` by the square-zero ideal `J ≅ R²`. Reading the products off the multiplication,
     * `M₂(R)` acts on `J` by
     * ```text
     * (A, 0)(0, ν) = (0, νA),   (0, ν)(A, 0) = (0, ν·adj A).
     * ```
     * Neither action is an associative module action (`A·(B·ν) = νBA`, not `νAB`), which is
     * exactly where the nonassociativity of the sextonions comes from: `J` is the two-dimensional
     * *Cayley bimodule* of `M₂(R)`, characterized by `x·n = n·x̄` for `x ∈ M₂(R)`, `n ∈ J`.
     * So `J` deliberately does not implement [org.vorpal.kosmos.algebra.structures.RSBiModule].
     *
     * @see splitQuaternionToSextonionMonomorphism for the same embedding starting from the
     *   split-quaternions.
     */
    fun <R : Any> mat2ToSextonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<Mat2<R>, Sextonion<R>> {
        val vecZero = Vec2(base.zero, base.zero)
        return NonAssociativeRingMonomorphism.of(
            domain = FixedMatAlgebras.mat2Ring(base),
            codomain = sextonionRing(base),
            map = UnaryOp { m -> Sextonion(m, vecZero) }
        )
    }

    /**
     * The projection of the sextonions onto their matrix part, over any commutative ring:
     * ```text
     * ρ : S(R) → M₂(R),   (A, ν) ↦ A.
     * ```
     * Unlike the coordinate projection from the Zorn matrices, this one is multiplicative: the
     * matrix part of `(A, ν)(B, μ)` is just `AB`, and `ρ(I, 0) = I`. So `ρ` is a surjective unital
     * ring homomorphism with
     * ```text
     * ker ρ = J = { (0, ν) },   ρ ∘ ι = id,
     * ```
     * where `ι` is [mat2ToSextonionMonomorphism]. That makes `ρ` a retraction of `S(R)` onto
     * `M₂(R)` (and `S(R)/J ≅ M₂(R)`), the Wedderburn–Malcev splitting into the semisimple part and
     * the radical.
     *
     * Kosmos has no retraction type yet; the type here is the plain ring homomorphism.
     */
    fun <R : Any> sextonionMat2Retraction(
        base: CommutativeRing<R>
    ): NonAssociativeRingHomomorphism<Sextonion<R>, Mat2<R>> =
        NonAssociativeRingHomomorphism.of(
            domain = sextonionRing(base),
            codomain = FixedMatAlgebras.mat2Ring(base),
            map = UnaryOp(Sextonion<R>::m)
        )

    /**
     * The split-quaternions inside the sextonions, over any commutative ring: the matrix
     * representation [SplitQuaternionAlgebras.splitQuaternionToRank2MatrixMonomorphism] followed by
     * [mat2ToSextonionMonomorphism],
     * ```text
     * w + xi + yj + zk ↦ ([[w − z, y − x], [x + y, w + z]], 0).
     * ```
     * Over a ring in which 2 is invertible its image is the whole matrix part `ι(M₂(R))`. Over ℤ
     * it is a proper subring of it, since `SplitQuaternion<ℤ> → M₂(ℤ)` is not onto. That is why
     * the sextonions are described as `M₂(R) ⋉ R²` and not `H′(R) ⋉ R²` in general.
     */
    fun <R : Any> splitQuaternionToSextonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<SplitQuaternion<R>, Sextonion<R>> =
        SplitQuaternionAlgebras.splitQuaternionToRank2MatrixMonomorphism(base) andThen mat2ToSextonionMonomorphism(base)

    /**
     * The retraction of the sextonions onto the split-quaternions over a field of characteristic
     * other than 2: [sextonionMat2Retraction] followed by the inverse of
     * [SplitQuaternionAlgebras.splitQuaternionMatrixIsomorphism],
     * ```text
     * ([[p, q], [r, s]], ν) ↦ (p + s)/2 + (r − q)/2·i + (q + r)/2·j + (s − p)/2·k.
     * ```
     * It is a surjective unital ring homomorphism with kernel `J = { (0, ν) }`, and it undoes
     * [splitQuaternionToSextonionMonomorphism].
     *
     * @throws IllegalArgumentException if 2 is not invertible in [field].
     */
    fun <F : Any> sextonionToSplitQuaternionHomomorphism(
        field: Field<F>
    ): NonAssociativeRingHomomorphism<Sextonion<F>, SplitQuaternion<F>> =
        sextonionMat2Retraction(field) andThen SplitQuaternionAlgebras.splitQuaternionMatrixIsomorphism(field).backward

    /**
     * The embedding of the base ring as the scalars, `r ↦ (rI, 0)`. Over a field its image is
     * the nucleus and the center of the sextonions.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<R, Sextonion<R>> {
        val z = base.zero
        val zeroVec: Vec2<R> = Vec2(z, z)
        return NonAssociativeRingMonomorphism.of(
            domain = base,
            codomain = sextonionRing(base),
            map = UnaryOp { r: R -> Sextonion(Mat2(r, z, z, r), zeroVec) }
        )
    }

    // ---------- Equality and printing ----------

    /**
     * Componentwise equality of sextonions from an equality [eqR] on the entries.
     */
    fun <R : Any> eqSextonion(eqR: Eq<R>): Eq<Sextonion<R>> {
        val eqMat = FixedMatAlgebras.eqMat2(eqR)
        val eqVec = FixedVecAlgebras.eqVec2(eqR)
        return Eq { (m1, v1), (m2, v2) -> eqMat(m1, m2) && eqVec(v1, v2) }
    }

    /**
     * A one-line rendering of a sextonion as its pair `(A, ν)`:
     * ```text
     * ([[a, b], [c, d]], (x, y))
     * ```
     * with each entry rendered by [prR].
     */
    fun <R : Any> printableSextonion(
        prR: Printable<R>
    ): Printable<Sextonion<R>> {
        val prMat = FixedMatAlgebras.printableMat2(prR)
        return Printable { (m, nu) -> "(${prMat(m)}, ${renderVec2(prR, nu)})" }
    }

    /**
     * A two-line rendering of a sextonion: the matrix part aligned as in
     * [FixedMatAlgebras.printableMat2Pretty], followed by the vector part on the second line, e.g.
     * ```text
     * ⎛  1  -2 ⎞
     * ⎝ 10   3 ⎠ ⊕ (4, -5)
     * ```
     * with each entry rendered by [prR].
     */
    fun <R : Any> printableSextonionPretty(
        prR: Printable<R>
    ): Printable<Sextonion<R>> {
        val prMat = FixedMatAlgebras.printableMat2Pretty(prR)
        return Printable { (m, nu) -> "${prMat(m)} ⊕ ${renderVec2(prR, nu)}" }
    }

    private fun <R : Any> renderVec2(prR: Printable<R>, v: Vec2<R>): String =
        "(${prR(v.x)}, ${prR(v.y)})"
}
