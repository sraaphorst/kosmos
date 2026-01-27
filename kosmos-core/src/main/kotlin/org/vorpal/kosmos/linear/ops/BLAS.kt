package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.algebra.structures.instances.Complex
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.VecLike

/**
 * Basic Linear Algebra Subprograms (standard names for functions in [DenseMatOps] and [DenseVecOps].)
 *
 * See [BLAS - Basic Linear Algebra Subprograms](https://www.netlib.org/blas/)
 */
object BLAS {
    // ---------------------------------
    // Double/Real “d*” family
    // These are all involutive rings
    // ---------------------------------

    // Double / Real "d*" family.
    private val d = RealAlgebras.RealStarField

    // Double / Complex "z*" family.
    private val z = ComplexAlgebras.ComplexStarAlgebra


    // ----------------------
    // Level 1: vector ops
    // ----------------------

    fun daxpy(
        a: Real,
        x: VecLike<Real>,
        y: VecLike<Real>
    ): DenseVec<Real> = DenseVecOps.axpy(d, a, x, y)

    fun dscal(
        a: Real,
        x: VecLike<Real>
    ): DenseVec<Real> = DenseVecOps.scale(d, a, x)

    fun ddot(
        x: VecLike<Real>,
        y: VecLike<Real>
    ): Real = DenseVecOps.dot(d, x, y)

    fun zaxpy(
        a: Complex,
        x: VecLike<Complex>,
        y: VecLike<Complex>
    ): DenseVec<Complex> = DenseVecOps.axpy(z, a, x, y)

    fun zscal(
        a: Complex,
        x: VecLike<Complex>
    ): DenseVec<Complex> = DenseVecOps.scale(z, a, x)

    fun zdotu(
        x: VecLike<Complex>,
        y: VecLike<Complex>
    ): Complex = DenseVecOps.dot(z, x, y)

    fun zdotc(
        x: VecLike<Complex>,
        y: VecLike<Complex>
    ): Complex = DenseVecOps.dotConjX(z, x, y)


    // -----------------------------
    // Level 2: matrix-vector ops
    // -----------------------------

    fun dgemv(
        aOp: MatOp,
        alpha: Real,
        aMat: MatLike<Real>,
        xVec: VecLike<Real>,
        beta: Real,
        yVec: VecLike<Real>
    ): DenseVec<Real> {
        require(aOp != MatOp.ConjTrans) { "Cannot use ConjTrans with d* BLAS."}
        return DenseMatOps.affineMatVec(d, alpha, aOp, aMat, xVec, beta, yVec)
    }

    fun zgemv(
        aOp: MatOp,
        alpha: Complex,
        aMat: MatLike<Complex>,
        xVec: VecLike<Complex>,
        beta: Complex,
        yVec: VecLike<Complex>
    ): DenseVec<Complex> =
        DenseMatOps.affineMatVec(z, alpha, aOp, aMat, xVec, beta, yVec)

    fun dger(
        alpha: Real,
        x: VecLike<Real>,
        y: VecLike<Real>,
        a: MatLike<Real>
    ): DenseMat<Real> =
        DenseVecOps.rank1Update(d, alpha, x, y, a) // if you expose it there

    fun zgeru(
        alpha: Complex,
        x: VecLike<Complex>,
        y: VecLike<Complex>,
        a: MatLike<Complex>
    ): DenseMat<Complex> =
        DenseVecOps.rank1Update(z, alpha, x, y, a)

    fun zgerc(
        alpha: Complex,
        x: VecLike<Complex>,
        y: VecLike<Complex>,
        a: MatLike<Complex>
    ): DenseMat<Complex> =
        DenseVecOps.rank1UpdateConjY(z, alpha, x, y, a)

    // -----------------------------
    // Level 3: matrix-matrix ops
    // -----------------------------

    fun dgemm(
        aOp: MatOp,
        bOp: MatOp,
        alpha: Real,
        a: MatLike<Real>,
        b: MatLike<Real>,
        beta: Real,
        c: MatLike<Real>
    ): DenseMat<Real> {
        require(aOp != MatOp.ConjTrans) { "Cannot use ConjTrans with d* BLAS."}
        require(bOp != MatOp.ConjTrans) { "Cannot use ConjTrans with d* BLAS."}
        return DenseMatOps.affineMul(d, alpha, aOp, a, bOp, b, beta, c)
    }

    fun zgemm(
        aOp: MatOp,
        bOp: MatOp,
        alpha: Complex,
        a: MatLike<Complex>,
        b: MatLike<Complex>,
        beta: Complex,
        c: MatLike<Complex>
    ): DenseMat<Complex> =
        DenseMatOps.affineMul(z, alpha, aOp, a, bOp, b, beta, c)
}
