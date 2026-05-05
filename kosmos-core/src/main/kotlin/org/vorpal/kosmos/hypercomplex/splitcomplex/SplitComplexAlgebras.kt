package org.vorpal.kosmos.hypercomplex.splitcomplex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.ComplexPrintable
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * Algebraic structures and utilities for split-complex numbers.
 *
 * Main constructions:
 * - [splitCayleyDicksonDouble]: generic split Cayley-Dickson doubling.
 * - [splitComplexRing]: ordinary split-complex ring over a commutative base ring.
 * - [splitComplexLeftAction]: scalar action `r ⊳ (a + bj) = (ra) + (rb)j`.
 * - [splitComplexModule]: `SplitComplex<R>` as a free `R`-module of rank 2.
 * - [splitComplexAlgebra]: `SplitComplex<R>` as an `R`-algebra.
 * - [splitComplexStarAlgebra]: `SplitComplex<R>` as an `R`-star-algebra (with conjugation).
 *
 * Real conveniences:
 * - [RealSplitComplexRing]: split-complex ring over `Real`.
 * - [RealSplitComplexStarAlgebra]: split-complex star algebra over `Real`.
 * - [SplitComplexRealVectorSpace]: two-dimensional real vector space.
 *
 * Vector spaces and modules:
 * - [SplitComplexRealVectorSpace]: the two-dimensional vector space of split-complex numbers over the real numbers.
 *
 * Morphisms:
 * - [scalarEmbedding]: embeds `R` into `SplitComplex<R>`.
 * - [splitComplexToRank2MatrixMonomorphism]: embeds `a + bj` as `[[a, b], [b, a]]`.
 */
object SplitComplexAlgebras {
    /**
     * The j-element of the split complex algebra, which depends on the base ring.
     *
     * Note that the 0 and 1 elements are accessible via the ring instance.
     */
    fun <R : Any> hyperbolicUnit(
        base: CommutativeRing<R>
    ): SplitComplex<R> =
        splitComplex(
            re = base.zero,
            hy = base.one
        )

    /**
     * The j-element of the split complex algebra over the Reals.
     */
    val j: SplitComplex<Real> =
        hyperbolicUnit(RealAlgebras.RealField)

    /**
     * The split Cayley-Dickson double of a base non-associative involutive ring.
     *
     * No associativity or commutativity claim is made here. For the algebra-over-a-ring
     * structure on `SplitComplex<R>`, see [splitComplexAlgebra] and [splitComplexStarAlgebra].
     */
    fun <R : Any> splitCayleyDicksonDouble(
        base: NonAssociativeInvolutiveRing<R>
    ): NonAssociativeInvolutiveRing<SplitComplex<R>> =
        CayleyDickson.split(base)

    /**
     * The ordinary split-complex ring over a commutative base ring.
     *
     * By not using the generic Cayley-Dickson construction here, we avoid requiring
     * an involution on the base: rationals, finite fields, etc. can all be used
     * directly.
     *
     * Multiplication is:
     * ```text
     * (a + bj)(c + dj) = (ac + bd) + (ad + bc)j
     * ```
     * so `j² = 1`.
     *
     * The involution is:
     * ```text
     * conj(a + bj) = a - bj
     * ```
     */
    fun <R : Any> splitComplexRing(
        base: CommutativeRing<R>
    ): CommutativeInvolutiveRing<SplitComplex<R>> = object : CommutativeInvolutiveRing<SplitComplex<R>> {
        override val zero: SplitComplex<R> =
            splitComplex(
                re = base.zero,
                hy = base.zero
            )

        override val one: SplitComplex<R> =
            splitComplex(
                re = base.one,
                hy = base.zero
            )

        override val add: AbelianGroup<SplitComplex<R>> =
            AbelianGroup.of(
                identity = zero,
                op = BinOp(Symbols.PLUS) { (re1, hy1), (re2, hy2) ->
                    splitComplex(
                        re = base.add(re1, re2),
                        hy = base.add(hy1, hy2)
                    )
                },
                inverse = Endo(Symbols.MINUS) { x ->
                    splitComplex(
                        re = base.add.inverse(x.re),
                        hy = base.add.inverse(x.hy)
                    )
                }
            )

        override val mul: CommutativeMonoid<SplitComplex<R>> =
            CommutativeMonoid.of(
                identity = one,
                op = BinOp(Symbols.ASTERISK) { (re1, hy1), (re2, hy2) ->
                    val ac = base.mul(re1, re2)
                    val bd = base.mul(hy1, hy2)
                    val ad = base.mul(re1, hy2)
                    val bc = base.mul(hy1, re2)

                    splitComplex(
                        re = base.add(ac, bd),
                        hy = base.add(ad, bc)
                    )
                }
            )

        override val conj: Endo<SplitComplex<R>> =
            Endo(Symbols.CONJ) { (re, hy) ->
                splitComplex(
                    re = re,
                    hy = base.add.inverse(hy)
                )
            }

        override fun fromBigInt(n: BigInteger): SplitComplex<R> =
            splitComplex(
                re = base.fromBigInt(n),
                hy = base.zero
            )
    }

    /**
     * Norm-like quadratic form for a generic commutative ring:
     * ```text
     * N(a + bj) = a² - b²
     * ```
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<SplitComplex<R>, R> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { z ->
            base.add(
                base.mul(z.re, z.re),
                base.add.inverse(base.mul(z.hy, z.hy))
            )
        }

    /**
     * Norm-like quadratic form for the Reals.
     */
    val splitComplexNormSq: UnaryOp<SplitComplex<Real>, Real> =
        normSq(RealAlgebras.RealField)

    /**
     * The componentwise scalar action of a commutative base ring on `SplitComplex<R>`:
     * ```text
     * r ⊳ (a + bj) = (r·a) + (r·b)j
     * ```
     */
    fun <R : Any> splitComplexLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, SplitComplex<R>> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { r, (re, hy) ->
            splitComplex(
                re = base.mul(r, re),
                hy = base.mul(r, hy)
            )
        }

    /**
     * `SplitComplex<R>` as a free `R`-module of rank 2.
     *
     * Carrier addition is taken from [splitComplexRing]; the action is [splitComplexLeftAction].
     */
    fun <R : Any> splitComplexModule(
        base: CommutativeRing<R>
    ): RModule<R, SplitComplex<R>> =
        RModule.of(
            scalars = base,
            add = splitComplexRing(base).add,
            leftAction = splitComplexLeftAction(base)
        )

    /**
     * `SplitComplex<R>` as an `R`-algebra.
     *
     * Combines the commutative ring structure from [splitComplexRing] with the scalar
     * action [splitComplexLeftAction]. Multiplication is `R`-bilinear by construction.
     */
    fun <R : Any> splitComplexAlgebra(
        base: CommutativeRing<R>
    ): Algebra<R, SplitComplex<R>> =
        Algebra.of(
            scalars = base,
            algebraRing = splitComplexRing(base),
            leftAction = splitComplexLeftAction(base)
        )

    /**
     * `SplitComplex<R>` as an `R`-star-algebra.
     *
     * The involution is the standard split-complex conjugation `a + bj ↦ a - bj`,
     * supplied by [splitComplexRing].
     */
    fun <R : Any> splitComplexStarAlgebra(
        base: CommutativeRing<R>
    ): StarAlgebra<R, SplitComplex<R>> =
        StarAlgebra.of(
            scalars = base,
            involutiveRing = splitComplexRing(base),
            leftAction = splitComplexLeftAction(base)
        )

    object RealSplitComplexRing :
        CommutativeInvolutiveRing<SplitComplex<Real>> by splitComplexRing(RealAlgebras.RealField)

    /**
     * The split-complex star algebra over the Reals.
     *
     * Mirrors `ComplexAlgebras.ComplexStarAlgebra`, but note that `SplitComplex<Real>`
     * is *not* a field (it has zero divisors) and its norm form is indefinite, so we
     * deliberately omit `RealNormedDivisionAlgebra` / `HasNormSq` here. Use
     * [splitComplexNormSq] directly when an indefinite quadratic form is wanted.
     */
    object RealSplitComplexStarAlgebra :
        CommutativeInvolutiveRing<SplitComplex<Real>> by RealSplitComplexRing,
        StarAlgebra<Real, SplitComplex<Real>> {

        override val zero: SplitComplex<Real> = RealSplitComplexRing.zero
        override val one: SplitComplex<Real> = RealSplitComplexRing.one

        override val scalars = RealAlgebras.RealField

        override val conj: Endo<SplitComplex<Real>> = RealSplitComplexRing.conj

        override val leftAction: LeftAction<Real, SplitComplex<Real>> =
            splitComplexLeftAction(RealAlgebras.RealField)
    }

    object SplitComplexRealVectorSpace : FiniteVectorSpace<Real, SplitComplex<Real>> {
        override val scalars = RealAlgebras.RealField
        override val add = RealSplitComplexRing.add
        override val dimension = 2
        override val leftAction: LeftAction<Real, SplitComplex<Real>> =
            RealSplitComplexStarAlgebra.leftAction
    }

    /**
     * A base ring embedding from the ring R to SplitComplex<R>.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): RingMonomorphism<R, SplitComplex<R>> {
        val split = splitComplexRing(base)
        return RingMonomorphism.of(
            domain = base,
            codomain = split,
            map = UnaryOp { r -> splitComplex(re = r, hy = base.zero) }
        )
    }

    /**
     * The matrix embedding into M₂(R).
     *
     * Note that:
     * ```text
     * x + yj ↪ [[x, y], [y, x]]
     * ```
     */
    fun <R : Any> splitComplexToRank2MatrixMonomorphism(
        base: CommutativeRing<R>
    ): RingMonomorphism<SplitComplex<R>, DenseMat<R>> =
        quadraticRank2MatrixEmbedding(
            domain = splitComplexRing(base),
            coefficientRing = base,
            s = base.one,
            t = base.zero,
            coeffs = { (re, hy) -> re to hy }
        )

    /**
     * Eqs
     */
    fun <R : Any> eqSplitComplex(eqR: Eq<R>): Eq<SplitComplex<R>> =
        CD.eq(eqR)

    /**
     * Eq specifically based on a strict Real comparison.
     */
    val eqSplitComplexStrict: Eq<SplitComplex<Real>> =
        CD.eq(RealAlgebras.eqRealStrict)

    /**
     * Eq specifically based on a Real approximation.
     */
    val eqSplitComplex: Eq<SplitComplex<Real>> =
        CD.eq(RealAlgebras.eqRealApprox)

    /**
     * Printables
     */
    fun <R : Any> printableSplitComplex(
        signed: SignedOps<R>,
        zero: R,
        one: R,
        prR: Printable<R>,
        eqR: Eq<R>
    ): Printable<SplitComplex<R>> =
        ComplexPrintable.complexLikePrintable(
            signed = signed,
            zero = zero,
            one = one,
            re = { it.re },
            im = { it.hy },
            basis = Symbols.HYPERBOLIC_J,
            prA = prR,
            eqA = eqR
        )

    /**
     * For Real.
     */
    val printableSplitComplexReal: Printable<SplitComplex<Real>> =
        printableSplitComplex(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prR = RealAlgebras.printableReal,
            eqR = RealAlgebras.eqRealApprox
        )

    val printableSplitComplexStrict: Printable<SplitComplex<Real>> =
        printableSplitComplex(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prR = RealAlgebras.printableRealStrict,
            eqR = RealAlgebras.eqRealStrict
        )

    val printableSplitComplexPretty: Printable<SplitComplex<Real>> =
        printableSplitComplex(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prR = RealAlgebras.printableRealPretty,
            eqR = RealAlgebras.eqRealApprox
        )
}
