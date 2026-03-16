package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingIsomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.functional.datastructures.getOrElse
import java.math.BigInteger

/**
 * Constant-matrix structures (carrier: `{aJ}`)
 * 1) Unit group: nonzero constants under multiplication
 * 2) Field of constant matrices
 * 3) Algebra over base field
 * 4) Isomorphisms (F ≅ constant matrices)
 * 5) Statistical helper: mean projection
 */
object ConstantMatrixAlgebras {
    private fun <F : Any> requireInvertibleOrThrow(
        field: Field<F>,
        n: Int,
        value: F
    ): F = field.reciprocalOption(value).getOrElse {
        throw ArithmeticException(
            "Cannot construct constant-matrix structure: n = $n is not invertible in the base field."
        )
    }

    /**
     * The multiplicative group of nonzero constant `n×n` matrices over a base field [base].
     * - The carrier is `{aJ | a ∈ F, a ≠ 0}`, where `J` is the all-ones matrix.
     * - The identity element is `(1/n)J`.
     * - Multiplication is ordinary matrix multiplication, which is commutative on this carrier.
     * - The inverse of `aJ` is `(1/(n^2 a))J`.
     */
    class ConstantMatrixUnitGroup<F : Any>(
        base: Field<F>,
        n: Int
    ): AbelianGroup<DenseMat<F>>, MatrixDimensionality {

        init {
            DenseKernel.checkPositive(n)
        }

        override val rows = n
        override val cols = n

        private val nScalar: F = base.fromBigInt(n.toBigInteger())
        private val nInv: F = requireInvertibleOrThrow(base, n, nScalar)
        private val n2: F = base.mul(nScalar, nScalar)

        override val identity: DenseMat<F> = DenseMatKernel.constMat(nInv, n, n)

        override val op = BinOp(Symbols.ASTERISK) { x: DenseMat<F>, y: DenseMat<F> ->
            DenseMatKernel.requireSize(x, n, n)
            DenseMatKernel.requireSize(y, n, n)
            val a = DenseMatKernel.checkConstNonemptyMat(x, base.zero)
            val b = DenseMatKernel.checkConstNonemptyMat(y, base.zero)

            // Each entry is the sum of n terms of the element a from x and b from y.
            val nab = base.mul(nScalar, base.mul(a, b))
            DenseMatKernel.constMat(nab, n, n)
        }

        override val inverse = Endo(Symbols.INVERSE) { x: DenseMat<F> ->
            DenseMatKernel.requireSize(x, n, n)
            val a = DenseMatKernel.checkConstNonemptyMat(x, base.zero)
            val denom = base.mul(n2, a)
            val inv = base.reciprocalOption(denom).getOrElse {
                throw ArithmeticException(
                    "Constant matrix is not invertible in the constant-matrix multiplicative group."
                )
            }
            DenseMatKernel.constMat(inv, n, n)
        }
    }


    /**
     * The field of constant `n×n` matrices over a base field [F], with carrier:
     * ```kotlin
     * {aJ | a ∈ F}
     * ```
     * where `J` is the all-ones matrix.
     *
     * Addition is entrywise, so:
     * ```kotlin
     * (aJ) + (bJ) = (a+b)J.
     * ```
     *
     * Multiplication is matrix multiplication, and `J^2 = nJ`, so:
     * ```kotlin
     * (aJ)(bJ) = (n·a·b)J.
     * ```
     *
     * The multiplicative identity in this field is:
     * ```kotlin
     * 1_A = (1/n)J.
     * ```
     * so this is a field **in its own right**, but it is not a unital subfield of `M_n(F)`
     * unless `n=1` (because its 1 differs from the ambient identity matrix `I`).
     *
     * The multiplicative inverse of a nonzero constant matrix `aJ` is:
     * ```kotlin
     * (aJ)^{-1} = (1/(n^2 a))J.
     * ```
     *
     * Construction requires that the image of the integer `n` in `F` is invertible.
     */
    class ConstantMatrixField<F : Any>(
        private val base: Field<F>,
        private val n: Int,
    ) : Field<DenseMat<F>>, MatrixDimensionality {

        init {
            DenseKernel.checkPositive(n)
        }

        override val rows = n
        override val cols = n

        private val nScalar: F = base.fromBigInt(n.toBigInteger())
        private val nInv: F = requireInvertibleOrThrow(base, n, nScalar)
        private val n2: F = base.mul(nScalar, nScalar)

        /**
         * Extract the scalar element from a constant matrix.
         */
        internal fun extractScalar(m: DenseMat<F>): F {
            DenseMatKernel.requireSize(m, n, n)
            return DenseMatKernel.checkConstNonemptyMat(m)
        }

        /**
         * Convenience function to avoid having to call into [org.vorpal.kosmos.linear.instances.DenseMatKernel] with the size.
         */
        private fun constMat(a: F): DenseMat<F> =
            DenseMatKernel.constMat(a, n, n)

        /**
         * Convenience function to embed a scalar from the field into this field.
         * This makes the `ConstantMatrixAlgebra`'s `LeftAction` much easier.
         */
        internal fun embedScalar(r: F): DenseMat<F> =
            constMat(base.mul(r, nInv))

        override val add: AbelianGroup<DenseMat<F>> =
            AbelianGroup.of(
                identity = constMat(base.zero),
                op = BinOp(Symbols.PLUS) { x, y ->
                    val a = extractScalar(x)
                    val b = extractScalar(y)
                    constMat(base.add(a, b))
                },
                inverse = Endo(Symbols.MINUS) { x ->
                    val a = extractScalar(x)
                    constMat(base.add.inverse(a))
                }
            )

        override val mul: CommutativeMonoid<DenseMat<F>> =
            CommutativeMonoid.of(
                // identity = (1/n)J
                identity = constMat(nInv),
                op = BinOp(Symbols.ASTERISK) { x, y ->
                    val a = extractScalar(x)
                    val b = extractScalar(y)

                    // (aJ)(bJ) = (n*a*b)J
                    val nab = base.mul(nScalar, base.mul(a, b))
                    constMat(nab)
                }
            )

        override val reciprocal: Endo<DenseMat<F>> =
            Endo(Symbols.INVERSE) { x ->
                val a = extractScalar(x)

                // (aJ)^{-1} = (1/(n^2 a))J, when a != 0
                val denom = base.mul(n2, a)
                val inv = base.reciprocalOption(denom).getOrElse {
                    throw ArithmeticException(
                        "Constant matrix is not invertible in the constant-matrix field."
                    )
                }

                constMat(inv)
            }

        override fun fromBigInt(n: BigInteger): DenseMat<F> {
            // k·1_A = k * (1/n)J = (k/n)J
            val k = base.fromBigInt(n)
            val kOverN = base.mul(k, nInv)
            return constMat(kOverN)
        }
    }

    /**
     * The 1-dimensional [F]-algebra of constant `n×n` matrices `aJ`, over a base field [F].
     *
     * The underlying ring is [fieldOfConstantMatrices], whose multiplicative identity is `(1/n)J`.
     *
     * Scalars act via the unital embedding:
     * ```kotlin
     * ι(r) = r · 1_A = (r/n)J
     * ```
     * and the action is left multiplication by `ι(r)`:
     * ```kotlin
     * r ⊳ X = ι(r) · X
     * ```
     *
     * For constant matrices `X = aJ`, this simplifies to `(r ⊳ X) = (ra)J`.
     */
    class ConstantMatrixAlgebra<F : Any>(
        private val baseField: Field<F>,
        private val n: Int,
    ) : Algebra<F, DenseMat<F>> {

        init {
            DenseKernel.checkPositive(n)
        }

        private val fieldOfConstantMatrices: ConstantMatrixField<F> = ConstantMatrixField(
            base = baseField,
            n = n
        )

        override val scalars: CommutativeRing<F>
            get() = baseField
        override val add = fieldOfConstantMatrices.add
        override val mul = fieldOfConstantMatrices.mul
        override val leftAction: LeftAction<F, DenseMat<F>> =
            LeftAction { r, x -> mul(fieldOfConstantMatrices.embedScalar(r), x) }
    }

    /**
     * Isomorphism between a field over [F] and `n×n` constant matrices `aJ`, over [F].
     */
    fun <F : Any> fieldIso(
        baseField: Field<F>,
        n: Int,
    ): RingIsomorphism<F, DenseMat<F>> {

        val cmf = ConstantMatrixField(baseField, n)
        val nScalar = baseField.fromBigInt(n.toBigInteger())

        val forward: RingHomomorphism<F, DenseMat<F>> =
            RingHomomorphism.of(
                domain = baseField,
                codomain = cmf,
            ) { cmf.embedScalar(it) }

        val backward: RingHomomorphism<DenseMat<F>, F> =
            RingHomomorphism.of(
                domain = cmf,
                codomain = baseField,
            ) { baseField.mul(nScalar, cmf.extractScalar(it)) }

        return RingIsomorphism.of(
            forward = forward,
            backward = backward
        )
    }

    /**
     * Replace the data vector [x] by its sample mean.
     */
    fun <F : Any> meanProject(
        field: Field<F>,
        x: DenseVec<F>
    ): DenseVec<F> {
        var sum = field.zero
        var i = 0
        while (i < x.size) {
            sum = field.add(sum, x[i])
            i += 1
        }

        val nScalar = field.fromBigInt(x.size.toBigInteger())
        val nInv = field.reciprocalOption(nScalar).getOrElse {
            throw ArithmeticException(
                "Cannot compute mean projection: vector size ${x.size} is not invertible in the field."
            )
        }

        val mean = field.mul(nInv, sum)
        return DenseVec.tabulate(x.size) { mean }
    }
}
