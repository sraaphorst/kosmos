package org.vorpal.kosmos.linear.values

/**
 * A dense vector of fixed length containing elements of type [A].
 *
 * This class provides an immutable, array-backed vector representation suitable for
 * linear algebra operations. Elements are stored contiguously in memory and accessed
 * by zero-based indexing.
 *
 * Instances should be created using the companion object factory methods rather than
 * the private constructor.
 *
 * We store elements in `Array<Any?>` due to JVM type erasure; values are cast back to A on access.
 *
 * @param A the type of elements in this vector. Must be a non-nullable type.
 *
 * @constructor Creates a dense vector from the given array. This constructor is private;
 * use [of] or [tabulate] factory methods instead.
 */
class DenseVec<A : Any> private constructor(
    private val data: Array<Any?>
): VecLike<A>, Iterable<A> {
    /**
     * The number of elements in this vector.
     */
    override val size: Int
        get() = data.size

    /**
     * Returns the element at the specified index.
     *
     * @param i the zero-based index of the element to retrieve
     * @return the element at index [i]
     * @throws IndexOutOfBoundsException if [i] is out of bounds
     */
    @Suppress("UNCHECKED_CAST")
    override operator fun get(i: Int): A =
        data[i] as A

    /**
     * Converts this vector to a list containing all elements in order.
     *
     * @return a list containing all elements from index 0 to [size]-1
     */
    fun toList(): List<A> =
        List(size) { get(it) }

    /**
     * Returns a new vector obtained by applying the given function to each element.
     *
     * @param B the type of elements in the resulting vector
     * @param f the function to apply to each element
     * @return a new [DenseVec] with elements `f(this[0]), f(this[1]), ..., f(this[size-1])`
     */
    fun <B : Any> map(f: (A) -> B): DenseVec<B> =
        tabulate(size) { f(get(it)) }

    override operator fun iterator(): Iterator<A> =
        object : Iterator<A> {
            private var i = 0

            override fun hasNext(): Boolean =
                i < size

            override fun next(): A {
                if (!hasNext()) throw NoSuchElementException()
                val value = get(i)
                i += 1
                return value
            }
        }

    fun <B : Any, C : Any> zipWith(other: DenseVec<B>, f: (A, B) -> C): DenseVec<C> {
        require(size == other.size) { "size mismatch for zipWith: $size versus ${other.size}" }
        return tabulate(size) { f(get(it), other[it]) }
    }

    /**
     * Compares this vector to another object for equality.
     *
     * Two vectors are equal if they have the same size and all corresponding elements
     * are equal according to their [equals] method.
     *
     * @param other the object to compare with
     * @return true if [other] is a [DenseVec] of the same size with equal elements
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DenseVec<*>) return false
        if (size != other.size) return false
        return (0 until size).all { data[it] == other.data[it] }
    }

    /**
     * Returns a hash code value for this vector.
     *
     * The hash code is computed from the elements using a polynomial rolling hash.
     *
     * @return a hash code consistent with [equals]
     */
    override fun hashCode(): Int =
        data.indices.fold(1) { h, idx ->
            31 * h + (data[idx]?.hashCode() ?: 0)
        }

    override fun toString(): String =
        toList().toString()

    companion object {
        /**
         * Creates a dense vector from the given list of elements.
         *
         * @param A the type of elements
         * @param xs the list of elements to include in the vector
         * @return a new [DenseVec] containing the elements of [xs] in order
         */
        fun <A : Any> of(xs: List<A>): DenseVec<A> =
            DenseVec(Array(xs.size) { xs[it] })

        /**
         * Creates a dense vector from the given vararg elements.
         *
         * @param A the type of elements
         * @param xs the elements to include in the vector
         * @return a new [DenseVec] containing the given elements in order
         */
        fun <A : Any> of(vararg xs: A): DenseVec<A> =
            DenseVec(Array(xs.size) { xs[it] })

        /**
         * Creates a dense vector by applying a function to each index.
         *
         * The resulting vector has the specified size, with element at index `i`
         * computed as `f(i)`.
         *
         * @param A the type of elements
         * @param size the number of elements in the resulting vector
         * @param f the function mapping indices to elements
         * @return a new [DenseVec] of the given size with elements computed by [f]
         * @throws IllegalArgumentException if [size] is negative
         */
        fun <A : Any> tabulate(size: Int, f: (Int) -> A): DenseVec<A> {
            require(size >= 0) { "size must be nonnegative, got $size" }
            val data = arrayOfNulls<Any?>(size)
            data.indices.forEach { data[it] = f(it) }
            return DenseVec(data)
        }

        internal fun <A : Any> fromArrayUnsafe(
            data: Array<Any?>
        ): DenseVec<A> = DenseVec(data)
    }
}
