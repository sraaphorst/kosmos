package org.vorpal.kosmos.core.finiteset

import org.vorpal.kosmos.functional.core.Kind
import kotlin.collections.emptyList

/**
 * The tag type for [FiniteSet]'s "F".
 * There is exactly one instance of this type; it is only used at the type level.
 *
 * [ForFiniteSet] is the stand-in for `F<_>`, a type constructor (container that takes one argument).
 *
 * It is a phantom-type witness.
 */
object ForFiniteSet
typealias FiniteSetOf<A> = Kind<ForFiniteSet, A>

/**
 * Safe downcast from `Kind<ForFiniteSet, A>` back to `FiniteSet<A>`.
 *
 * Remember that `FiniteSetOf<A>` is an alias to `Kind<ForFiniteSet, A>`.
 *
 * Use only when you *know* the [Kind] originated from [FiniteSet] (the type system enforces this).
 */
@Suppress("UNCHECKED_CAST")
fun <A> FiniteSetOf<A>.fix(): FiniteSet<A> = this as FiniteSet<A>


/**
 * FiniteSets must be created through their companion object builders.
 * They are primarily used for their algebraic and combinatorics role,
 * as they are more convenient than vanilla Kotlin sets for Kosmos operations
 * and algorithms.
 */
sealed interface FiniteSet<A>: FiniteSetOf<A>, Iterable<A> {
    val backing: Set<A>
    val order: List<A>
    val size: Int

    val isEmpty: Boolean
        get() = backing.isEmpty()
    val isNotEmpty: Boolean
        get() = !isEmpty

    fun toSet(): Set<A> = backing
    fun toList(): List<A> = order

    operator fun contains(element: A): Boolean = backing.contains(element)

    // Subset testing functions.
    infix fun isSubsetOf(other: FiniteSet<A>): Boolean =
        backing.all { it in other.backing }
    infix fun isProperSubsetOf(other: FiniteSet<A>): Boolean =
        isSubsetOf(other) && size < other.size
    infix fun isSupersetOf(other: FiniteSet<A>): Boolean =
        other.isSubsetOf(this)
    infix fun isProperSupersetOf(other: FiniteSet<A>): Boolean =
        other.isProperSubsetOf(this)
    infix fun isDisjointFrom(other: FiniteSet<A>): Boolean =
        backing.none { it in other.backing }
    /**
     * Map preserves both Ordered and Unordered FiniteSets, and in the case of an
     * Ordered FiniteSet, it also respects the ordering under the mapping.
     */
    fun <B> map(f: (A) -> B): FiniteSet<B> = when (this) {
        is Ordered -> {
            val mapped = order.map(f)
            val distinctOrder = mapped.distinct()
            Ordered(distinctOrder.toSet(), distinctOrder)
        }
        is Unordered -> {
            val mappedBacking = backing.map(f).toSet()
            Unordered(mappedBacking)
        }
    }

    fun filter(predicate: (A) -> Boolean): FiniteSet<A> = when (this) {
        is Ordered -> {
            val filtered = order.filter(predicate)
            Ordered(filtered.toSet(), filtered)
        }
        is Unordered -> {
            val filtered = backing.filter(predicate).toSet()
            Unordered(filtered)
        }
    }

    fun filterNot(predicate: (A) -> Boolean): FiniteSet<A> =
        filter( { !predicate(it) })

    infix fun union(other: FiniteSet<A>): FiniteSet<A> = when (this) {
        is Ordered -> {
            val combined = order + other.order
            val distinctOrder = combined.distinct()
            Ordered(distinctOrder.toSet(), distinctOrder)
        }
        is Unordered -> Unordered(backing + other.backing)
    }

    infix fun intersect(other: FiniteSet<A>): FiniteSet<A> =
        when (this) {
            is Ordered -> filter { it in other }
            is Unordered -> Unordered(backing intersect other.backing)
        }

    operator fun minus(other: FiniteSet<A>): FiniteSet<A> = when (this) {
        is Ordered -> {
            val difference = backing - other.backing
            Ordered(difference, order.filter { it in difference })
        }
        is Unordered -> Unordered(backing - other.backing)
    }

    operator fun plus(other: Iterable<A>): FiniteSet<A> = when (this) {
        is Ordered -> {
            val newElements = other.filter { it !in backing }
            val combinedOrder = order + newElements
            Ordered(combinedOrder.toSet(), combinedOrder)
        }
        is Unordered -> Unordered(backing + other)
    }

    infix fun symmetricDifference(other: FiniteSet<A>): FiniteSet<A> = when (this) {
        is Ordered -> {
            val symmetric = (backing - other.backing) + (other.backing - backing)
            val combinedOrder = order.filter { it in symmetric } +
                    other.order.filter { it in symmetric && it !in backing }
            Ordered(symmetric, combinedOrder)
        }
        is Unordered -> Unordered((backing - other.backing) + (other.backing - backing))
    }

    // Functional combinatorics operations

    /**
     * Cartesian product with another FiniteSet.
     */
    infix fun <B> cartesianProduct(other: FiniteSet<B>): FiniteSet<Pair<A, B>> = when (this) {
        is Ordered -> {
            val pairs = order.flatMap { a -> other.order.map { b -> a to b } }
            ordered(pairs)
        }
        is Unordered -> {
            val pairs = backing.flatMap { a -> other.backing.map { b -> a to b } }
            unordered(pairs)
        }
    }

    /**
     * Cartesian power: Cartesian product with itself k times.
     */
    fun cartesianPower(k: Int): FiniteSet<List<A>> = when (k) {
        0 -> singleton(emptyList())
        1 -> this.map { listOf(it) }
        else -> {
            val prev = cartesianPower(k - 1)
            prev.cartesianProduct(this).map { (xs, x) -> xs + x }
        }
    }

    /**
     * Partition the set based on a predicate.
     */
    fun partition(predicate: (A) -> Boolean): Pair<FiniteSet<A>, FiniteSet<A>> =
        Pair(filter(predicate), filterNot(predicate))

    /**
     * Group elements by a key function.
     */
    fun <K> groupBy(keySelector: (A) -> K): Map<K, FiniteSet<A>> = when (this) {
        is Ordered -> order.groupBy(keySelector).mapValues { (_, elements) -> ordered(elements) }
        is Unordered -> backing.groupBy(keySelector).mapValues { (_, elements) -> unordered(elements) }
    }

    /**
     * Check if any element matches the predicate.
     */
    fun any(predicate: (A) -> Boolean): Boolean =
        backing.any(predicate)

    /**
     * Check if all elements match the predicate.
     */
    fun all(predicate: (A) -> Boolean): Boolean =
        backing.all(predicate)

    /**
     * Check if none of the elements match the predicate.
     */
    fun none(predicate: (A) -> Boolean): Boolean =
        !any(predicate)

    /**
     * Count elements matching the predicate.
     */
    fun count(predicate: (A) -> Boolean): Int =
        backing.count(predicate)

    /**
     * Fold operation over the elements.
     */
    fun <B> fold(initial: B, operation: (B, A) -> B): B = when (this) {
        is Ordered -> order.fold(initial, operation)
        is Unordered -> backing.fold(initial, operation)
    }

    /**
     * Reduce operation over the elements.
     */
    fun reduce(operation: (A, A) -> A): A = when (this) {
        is Ordered -> order.reduce(operation)
        is Unordered -> backing.reduce(operation)
    }

    /**
     * Convert to different FiniteSet type.
     */
    fun toOrdered(): Ordered<A> = when (this) {
        is Ordered -> this
        is Unordered -> ordered(backing)
    }

    fun toUnordered(): Unordered<A> = when (this) {
        is Ordered -> unordered(backing)
        is Unordered -> this
    }

    /**
     * A FiniteSet that is Ordered with a guaranteed stable ordering.
     */
    class Ordered<A> internal constructor(
        override val backing: Set<A>,
        override val order: List<A>
    ) : FiniteSet<A>, AbstractList<A>() {
        override val size
            get() = order.size

        override fun iterator(): Iterator<A> = order.iterator()
        override operator fun contains(element: A): Boolean = element in backing
        override operator fun get(index: Int): A = order[index]

        /**
         * Find the first element matching a predicate.
         */
        fun find(predicate: (A) -> Boolean): A? = order.find(predicate)

        // Additional list-like operations.
        override fun indexOf(element: A): Int = order.indexOf(element)
        override fun lastIndexOf(element: A): Int = order.lastIndexOf(element)

        /**
         * Get a sublist as an Ordered FiniteSet.
         */
        fun subSet(fromIndex: Int, toIndex: Int): Ordered<A> {
            val subList = order.subList(fromIndex, toIndex)
            return Ordered(subList.toSet(), subList)
        }

        /**
         * Reverse the order.
         */
        fun reversed(): Ordered<A> =
            Ordered(backing, order.reversed())

        /**
         * Take first n elements.
         */
        fun take(n: Int): Ordered<A> {
            val taken = order.take(n)
            return Ordered(taken.toSet(), taken)
        }

        /**
         * Take elements while predicate holds.
         */
        fun takeWhile(predicate: (A) -> Boolean): Ordered<A> {
            val taken = order.takeWhile(predicate)
            return Ordered(taken.toSet(), taken)
        }

        /**
         * Drop first n elements.
         */
        fun drop(n: Int): Ordered<A> {
            val remaining = order.drop(n)
            return Ordered(remaining.toSet(), remaining)
        }

        /**
         * Drop elements while predicate holds.
         */
        fun dropWhile(predicate: (A) -> Boolean): Ordered<A> {
            val remaining = order.dropWhile(predicate)
            return Ordered(remaining.toSet(), remaining)
        }

        /**
         * Zip with another ordered set.
         */
        fun <B> zip(other: Ordered<B>): Ordered<Pair<A, B>> {
            val zipped = order.zip(other.order)
            return ordered(zipped)
        }

        /**
         * Zip with indices.
         */
        fun zipWithIndex(): Ordered<Pair<A, Int>> {
            val count = ordered(0 until size)
            return ordered(order.zip(count))
        }

        /**
         * Window operations.
         */
        fun windowed(size: Int, step: Int = 1, partialWindows: Boolean = false): List<Ordered<A>> =
            order.windowed(size, step, partialWindows).map { ordered(it) }

        /**
         * Chunked operations.
         */
        fun chunked(size: Int): List<Ordered<A>> =
            order.chunked(size).map { ordered(it) }

        override fun equals(other: Any?): Boolean =
            other is Ordered<*> && this.order == other.order
        override fun hashCode(): Int = order.hashCode()
    }

    /**
     * A FiniteSet with its iteration order deliberately unspecified.
     */
    class Unordered<A> internal constructor(
        override val backing: Set<A>
    ) : FiniteSet<A> {
        override val order = backing.toList()
        override val size = backing.size
        override fun iterator(): Iterator<A> = backing.iterator()
        override operator fun contains(element: A): Boolean = element in backing

        // We want set semantics and not list semantics in Unordered, i.e. {1, 2} == {2, 1}, so
        // we need to override equals and hashCode to respect this, ignoring order.
        override fun equals(other: Any?): Boolean =
            other is Unordered<*> && this.backing == other.backing
        override fun hashCode(): Int = backing.hashCode()
    }

    companion object {
        fun <A> ordered(elements: Iterable<A>): Ordered<A> {
            val distinctElements = elements.distinct()
            return Ordered(distinctElements.toSet(), distinctElements.distinct())
        }

        fun <A> ordered(vararg elements: A): Ordered<A> =
            ordered(elements.asIterable())

        fun <A : Comparable<A>> sorted(elements: Iterable<A>): Ordered<A> {
            val set = elements.toSet()
            return Ordered(set, set.sorted())
        }

        fun <A> sortedWith(elements: Iterable<A>, comparator: Comparator<A>): Ordered<A> {
            val set = elements.toSet()
            return Ordered(set, set.sortedWith(comparator))
        }

        fun <A> unordered(elements: Iterable<A>): Unordered<A> =
            Unordered(elements.toSet())

        fun <A> unordered(vararg elements: A): Unordered<A> =
            unordered(elements.asIterable())

        //
        fun <A> empty(): Ordered<A> = emptyOrdered()
        fun <A> emptyUnordered(): Unordered<A> = unordered(emptyList())
        fun <A> emptyOrdered(): Ordered<A> = ordered(emptyList())

        fun <A> singleton(a: A): FiniteSet<A> = ordered(listOf(a))

        // Convenience builders.
        fun <A> of(vararg elements: A): Ordered<A> = ordered(*elements)

        /**
         * Build a FiniteSet using a builder pattern.
         */
        inline fun <A> buildOrdered(builderAction: MutableList<A>.() -> Unit): Ordered<A> {
            val builder = mutableListOf<A>()
            builder.builderAction()
            return ordered(builder)
        }

        inline fun <A> buildUnordered(builderAction: MutableSet<A>.() -> Unit): Unordered<A> {
            val builder = mutableSetOf<A>()
            builder.builderAction()
            return unordered(builder)
        }

        /**
         * Generate range-based finite sets.
         */
        fun rangeOrdered(start: Int, endInclusive: Int): Ordered<Int> = ordered(start..endInclusive)
        fun rangeOrdered(start: Long, endInclusive: Long): Ordered<Long> = ordered(start..endInclusive)
        fun <T : Comparable<T>> range(start: T, endInclusive: T, step: (T) -> T): Ordered<T> {
            val elements = generateSequence(start) { current ->
                val next = step(current)
                if (next <= endInclusive) next else null
            }
            return ordered(elements.toList())
        }

        /**
         * Generate a finite set from a sequence.
         */
        fun <A> fromSequenceUnordered(sequence: Sequence<A>): Unordered<A> = unordered(sequence.asIterable())
        fun <A> fromSequenceOrdered(sequence: Sequence<A>): Ordered<A> = ordered(sequence.asIterable())
    }
}

// Extension functions for better interoperability
fun <A> Iterable<A>.toUnorderedFiniteSet(): FiniteSet.Unordered<A> = FiniteSet.unordered(this)
fun <A> Iterable<A>.toOrderedFiniteSet(): FiniteSet.Ordered<A> = FiniteSet.ordered(this)
fun <A> Array<A>.toUnorderedFiniteSet(): FiniteSet.Unordered<A> = FiniteSet.unordered(this.asIterable())
fun <A> Array<A>.toOrderedFiniteSet(): FiniteSet.Ordered<A> = FiniteSet.ordered(this.asIterable())
fun <A> Sequence<A>.toUnorderedFiniteSet(): FiniteSet.Unordered<A> = FiniteSet.unordered(this.asIterable())
fun <A> Sequence<A>.toOrderedFiniteSet(): FiniteSet.Ordered<A> = FiniteSet.ordered(this.asIterable())

// Operator overloads for nicer syntax
operator fun <A> FiniteSet<A>.plus(element: A): FiniteSet<A> = this + listOf(element)
operator fun <A> FiniteSet<A>.minus(element: A): FiniteSet<A> = this - FiniteSet.singleton(element)

// Functional combinators: this is, in essence, flatMap, but it collides with Kotlin's flatMap on
// iterators, and wreaks havoc when imported. Most of the time, it should not even be necessary.
inline fun <A, B> FiniteSet<A>.bind(transform: (A) -> FiniteSet<B>): FiniteSet<B> = when (this) {
    is FiniteSet.Ordered ->
        FiniteSet.ordered(order.flatMap { transform(it).order })
    is FiniteSet.Unordered ->
        FiniteSet.unordered( backing.flatMap { transform(it).backing })
}
