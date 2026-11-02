package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.hypercomplex.complex.RealComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.VecLike
import org.vorpal.kosmos.linear.views.TransposeMode

/**
 * Basic Linear Algebra Subprograms: the standard BLAS names for functions in [DenseMatOps] and
 * [DenseVecOps], specialized to two families of entries.
 * - `d*` functions work over [Real] (double precision real).
 * - `z*` functions work over [RealComplex] (double precision complex), where the `c` variants
 *   conjugate and the `u` variants do not.
 *
 * Both entry types are involutive rings, so the conjugate-transpose mode is available for the
 * `z*` family; over [Real] it would be the plain transpose, and the `d*` functions reject it, as
 * BLAS does.
 *
 * See [BLAS - Basic Linear Algebra Subprograms](https://www.netlib.org/blas/)
 */
object BLAS {
    // The "d*" family: Real.
    private val d = RealAlgebras.RealStarField

    // The "z*" family: RealComplex.
    private val z = ComplexAlgebras.ComplexStarAlgebra


    // ----------------------
    // Level 1: vector ops
    // ----------------------

    /** `a·x + y` over the reals. See [DenseVecOps.axpy]. */
    fun daxpy(
        a: Real,
        x: VecLike<Real>,
        y: VecLike<Real>
    ): DenseVec<Real> = DenseVecOps.axpy(d, a, x, y)

    /** `a·x` over the reals. See [DenseVecOps.scale]. */
    fun dscal(
        a: Real,
        x: VecLike<Real>
    ): DenseVec<Real> = DenseVecOps.scale(d, a, x)

    /** `x · y` over the reals. See [DenseVecOps.dot]. */
    fun ddot(
        x: VecLike<Real>,
        y: VecLike<Real>
    ): Real = DenseVecOps.dot(d, x, y)

    /** `a·x + y` over the complex numbers. See [DenseVecOps.axpy]. */
    fun zaxpy(
        a: RealComplex,
        x: VecLike<RealComplex>,
        y: VecLike<RealComplex>
    ): DenseVec<RealComplex> = DenseVecOps.axpy(z, a, x, y)

    /** `a·x` over the complex numbers. See [DenseVecOps.scale]. */
    fun zscal(
        a: RealComplex,
        x: VecLike<RealComplex>
    ): DenseVec<RealComplex> = DenseVecOps.scale(z, a, x)

    /** The unconjugated dot product `Σ xᵢyᵢ` over the complex numbers. See [DenseVecOps.dot]. */
    fun zdotu(
        x: VecLike<RealComplex>,
        y: VecLike<RealComplex>
    ): RealComplex = DenseVecOps.dot(z, x, y)

    /** The conjugated dot product `Σ conj(xᵢ) yᵢ` over the complex numbers. See [DenseVecOps.dotConjX]. */
    fun zdotc(
        x: VecLike<RealComplex>,
        y: VecLike<RealComplex>
    ): RealComplex = DenseVecOps.dotConjX(z, x, y)


    // -----------------------------
    // Level 2: matrix-vector ops
    // -----------------------------

    /**
     * `alpha·op(A)·x + beta·y` over the reals. [aOp] may not be
     * [TransposeMode.ConjugateTranspose]. See [DenseMatOps.affineMatVec].
     */
    fun dgemv(
        aOp: TransposeMode,
        alpha: Real,
        aMat: MatLike<Real>,
        xVec: VecLike<Real>,
        beta: Real,
        yVec: VecLike<Real>
    ): DenseVec<Real> {
        require(aOp != TransposeMode.ConjugateTranspose) { "Cannot use ConjugateTranspose with d* BLAS." }
        return DenseMatOps.affineMatVec(d, alpha, aOp, aMat, xVec, beta, yVec)
    }

    /** `alpha·op(A)·x + beta·y` over the complex numbers. See [DenseMatOps.affineMatVec]. */
    fun zgemv(
        aOp: TransposeMode,
        alpha: RealComplex,
        aMat: MatLike<RealComplex>,
        xVec: VecLike<RealComplex>,
        beta: RealComplex,
        yVec: VecLike<RealComplex>
    ): DenseVec<RealComplex> =
        DenseMatOps.affineMatVec(z, alpha, aOp, aMat, xVec, beta, yVec)

    /** The rank-1 update `A + alpha·x·yᵀ` over the reals. See [DenseVecOps.rank1Update]. */
    fun dger(
        alpha: Real,
        x: VecLike<Real>,
        y: VecLike<Real>,
        a: MatLike<Real>
    ): DenseMat<Real> =
        DenseVecOps.rank1Update(d, alpha, x, y, a)

    /** The rank-1 update `A + alpha·x·yᵀ` over the complex numbers. See [DenseVecOps.rank1Update]. */
    fun zgeru(
        alpha: RealComplex,
        x: VecLike<RealComplex>,
        y: VecLike<RealComplex>,
        a: MatLike<RealComplex>
    ): DenseMat<RealComplex> =
        DenseVecOps.rank1Update(z, alpha, x, y, a)

    /** The rank-1 update `A + alpha·x·y*` over the complex numbers. See [DenseVecOps.rank1UpdateConjY]. */
    fun zgerc(
        alpha: RealComplex,
        x: VecLike<RealComplex>,
        y: VecLike<RealComplex>,
        a: MatLike<RealComplex>
    ): DenseMat<RealComplex> =
        DenseVecOps.rank1UpdateConjY(z, alpha, x, y, a)

    // -----------------------------
    // Level 3: matrix-matrix ops
    // -----------------------------

    /**
     * `alpha·op(A)·op(B) + beta·C` over the reals. Neither [aOp] nor [bOp] may be
     * [TransposeMode.ConjugateTranspose]. See [DenseMatOps.affineMul].
     */
    fun dgemm(
        aOp: TransposeMode,
        bOp: TransposeMode,
        alpha: Real,
        a: MatLike<Real>,
        b: MatLike<Real>,
        beta: Real,
        c: MatLike<Real>
    ): DenseMat<Real> {
        require(aOp != TransposeMode.ConjugateTranspose) { "Cannot use ConjugateTranspose with d* BLAS." }
        require(bOp != TransposeMode.ConjugateTranspose) { "Cannot use ConjugateTranspose with d* BLAS." }
        return DenseMatOps.affineMul(d, alpha, aOp, a, bOp, b, beta, c)
    }

    /** `alpha·op(A)·op(B) + beta·C` over the complex numbers. See [DenseMatOps.affineMul]. */
    fun zgemm(
        aOp: TransposeMode,
        bOp: TransposeMode,
        alpha: RealComplex,
        a: MatLike<RealComplex>,
        b: MatLike<RealComplex>,
        beta: RealComplex,
        c: MatLike<RealComplex>
    ): DenseMat<RealComplex> =
        DenseMatOps.affineMul(z, alpha, aOp, a, bOp, b, beta, c)
}
