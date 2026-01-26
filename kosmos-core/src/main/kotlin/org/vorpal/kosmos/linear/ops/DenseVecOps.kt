package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.linear.instances.DenseVecKernel
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.VecLike

object DenseVecOps {

    /**
     * * Entrywise combination of two vectors under a [Semigroup].
     */
    fun <A : Any> hadamard(
        semigroup: Semigroup<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseVec<A> = DenseVecKernel.hadamard(semigroup, x, y)

    /**
     * Entrywise combination of two vectors under the multiplicative action of a [Semiring].
     */
    fun <A : Any> hadamard(
        semiring: Semiring<A>,
        vec1: VecLike<A>,
        vec2: VecLike<A>
    ): DenseVec<A> = DenseVecKernel.hadamard(semiring, vec1, vec2)

    /**
     * Vector addition (pointwise) over a semiring.
     */
    fun <A : Any> add(
        semiring: Semiring<A>,
        vec1: VecLike<A>,
        vec2: VecLike<A>
    ): DenseVec<A> = DenseVecKernel.add(semiring, vec1, vec2)

    /** Dot product over a semiring. */
    fun <A : Any> dot(
        semiring: Semiring<A>,
        vec1: VecLike<A>,
        vec2: VecLike<A>
    ): A = DenseVecKernel.dot(semiring, vec1, vec2)

    /**
     * Using multiplication by a [Semiring], scale a vector [vec] by a scalar [s].
     */
    fun <A : Any> scale(
        semiring: Semiring<A>,
        s: A,
        vec: VecLike<A>
    ): DenseVec<A> = DenseVecKernel.scale(semiring, s, vec)

    /**
     * Constant vector of entries [s] of length [n].
     */
    fun <A : Any> constantVec(
        s: A,
        n: Int
    ): DenseVec<A> = DenseVecKernel.constantVec(s, n)

    /**
     * All-zeros vector of length [n] of the additive identity of a [Semiring] repeated.
     */
    fun <A : Any> zeroVec(
        semiring: Semiring<A>,
        n: Int
    ): DenseVec<A> = DenseVecKernel.zeroVec(semiring, n)

    /**
     * All-ones vector of length [n] of the multiplicative identity of a [Semiring] repeated.
     */
    fun <A : Any> oneVec(
        semiring: Semiring<A>,
        n: Int
    ): DenseVec<A> = DenseVecKernel.oneVec(semiring, n)

    /**
     * Returns true iff x has no zero entries, thus indicating that it is Hadamard-invertible over a [Field].
     */
    fun <A : Any> isHadamardUnit(
        field: Field<A>,
        x: VecLike<A>,
    ): Boolean = DenseVecKernel.isHadamardUnit(field, x)

    /**
     * Given a vector, execute the map [f] on it.
     */
    fun <A : Any, B : Any> map(
        x: VecLike<A>,
        f: (A) -> B
    ): DenseVec<B> = DenseVecKernel.map(x, f)

    /**
     * Folds the vector beginning at the left using the function [f] beginning with the value [initial].
     */
    fun <A : Any, B : Any> foldLeft(
        x: VecLike<A>,
        initial: B,
        f: (B, A) -> B
    ): B = DenseVecKernel.foldLeft(x, initial, f)

    /**
     * Reduces the vector beginning at the left using the function [f].
     *
     * @return [Option.Some] if x is nonempty, and [Option.None] if x is empty.
     */
    fun <A : Any> reduceLeft(
        x: VecLike<A>,
        f: (A, A) -> A
    ): Option<A> = DenseVecKernel.reduceLeft(x, f)

    /**
     * Calculates the sum of the elements of the vector under the action of the map [f] in the given
     * [CommutativeMonoid].
     *
     * If the vector is empty, the additive identity is returned.
     */
    fun <A : Any, B : Any> sumOf(
        add: CommutativeMonoid<B>,
        x: VecLike<A>,
        f: (A) -> B
    ): B = DenseVecKernel.sumOf(add, x, f)

    /**
     * Calculates the sum of the elements of the vector under the action of the map [f] in the given [Semiring].
     *
     * If the vector is empty, the additive identity is returned.
     */
    fun <A : Any, B : Any> sumOf(
        semiring: Semiring<B>,
        x: VecLike<A>,
        f: (A) -> B
    ): B = DenseVecKernel.sumOf(semiring, x, f)

    /**
     * Calculates the sum of the elements of the vector in the given [CommutativeMonoid], beginning with the additive
     * identity.
     *
     * If the vector is empty, the additive identity is returned.
     */
    fun <A : Any> sum(
        add: CommutativeMonoid<A>,
        x: VecLike<A>,
    ): A = DenseVecKernel.sum(add, x)

    /**
     * Calculates the sum of the elements of the vector in the given [Semiring], beginning with the additive identity.
     *
     * If the vector is empty, the additive identity is returned.
     */
    fun <A : Any> sum(
        semiring: Semiring<A>,
        x: VecLike<A>
    ): A = DenseVecKernel.sum(semiring, x)

    /**
     * Returns true iff every element in the vector [x] satisfies [predicate].
     *
     * Note that if [x] is the empty vector, [all] vacuously returns true.
     */
    fun <A : Any> all(
        x: VecLike<A>,
        predicate: (A) -> Boolean
    ): Boolean = DenseVecKernel.all(x, predicate)

    /**
     * Returns true iff there exists an element in the vector [x] that satisfies [predicate].
     */
    fun <A : Any> any(
        x: VecLike<A>,
        predicate: (A) -> Boolean
    ): Boolean = DenseVecKernel.any(x, predicate)

    /**
     * Returns true iff none of the elements in the vector [x] satisfy [predicate].
     */
    fun <A: Any> none(
        x: VecLike<A>,
        predicate: (A) -> Boolean
    ): Boolean = !any(x, predicate)

    /**
     * axpy is used in many BLAS (basic linear algebra subprograms).
     *
     * Given a [Semiring], a constant [a], and vectors [x] and [y], calculate:
     * ```
     * ax + y
     * ```
     */
    fun <A : Any> axpy(
        semiring: Semiring<A>,
        a: A,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseVec<A> = add(semiring, scale(semiring, a, x), y)

    /**
     * Given a vector [x] over [A] and a [Comparator] over [A], determine the index of the smallest element of [x].
     *
     * If [x] is empty, [Option.None] is returned.
     */
    fun <A : Any> argmin(
        x: VecLike<A>,
        comparator: Comparator<A>
    ): Option<Int> = DenseVecKernel.argmin(x, comparator)

    /**
     * Given a vector [x] over [A] and a [TotalOrder] over [A], determine the index of the smallest element of [x].
     *
     * If [x] is empty, [Option.None] is returned.
     */
    fun <A : Any> argmin(
        x: VecLike<A>,
        totalOrder: TotalOrder<A>,
    ): Option<Int> = DenseVecKernel.argmin(x, totalOrder)

    /**
     * Given a vector [x] over [A] and a [Comparator] over [A], determine the index of the largest element of [x].
     *
     * If [x] is empty, [Option.None] is returned.
     */
    fun <A : Any> argmax(
        x: VecLike<A>,
        comparator: Comparator<A>
    ): Option<Int> = DenseVecKernel.argmax(x, comparator)

    /**
     * Given a vector [x] over [A] and a [TotalOrder] over [A], determine the index of the largest element of [x].
     *
     * If [x] is empty, [Option.None] is returned.
     */
    fun <A : Any> argmax(
        x: VecLike<A>,
        totalOrder: TotalOrder<A>
    ): Option<Int> = DenseVecKernel.argmax(x, totalOrder)

    /**
     * Given a [Semiring] and two vectors, [x] and [y], create the outer product of [x] and [y], i.e. the
     * matrix `M` where `M_ij = x[i] * y[j]` in the `Semiring`.
     */
    fun <A : Any> outerProduct(
        semiring: Semiring<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseMat<A> = DenseVecKernel.outerProduct(semiring, x, y)
}
