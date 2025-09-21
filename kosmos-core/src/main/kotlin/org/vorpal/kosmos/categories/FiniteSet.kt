package org.vorpal.kosmos.categories

/**
 * FiniteSets must be created through their companion object builders.
 */
sealed class FiniteSet<A> (
    protected val backing: Set<A>,
    protected val order: List<A>
) : Iterable<A> {
    val size: Int get() = backing.size
    fun toSet(): Set<A> = backing
    fun toList(): List<A> = order
    override fun iterator(): Iterator<A> = order.iterator()
    operator fun contains(a: A): Boolean = a in backing

    fun <B> map(f: (A) -> B): FiniteSet<B> =
        Ordered(order.map(f).toSet(), order.map(f).distinct())

    fun filter(predicate: (A) -> Boolean): FiniteSet<A> =
        Ordered(backing.filter(predicate).toSet(), order.filter(predicate))

    infix fun union(other: FiniteSet<A>): FiniteSet<A> =
        ordered(order + other.order)

    infix fun intersect(other: FiniteSet<A>): FiniteSet<A> =
        filter { it in other }

    operator fun minus(other: FiniteSet<A>): Ordered<A> {
        val otherSet = other.toSet()
        return Ordered(backing - otherSet, order.filter { it !in otherSet })
    }

    operator fun plus(other: Iterable<A>): Ordered<A> {
        val combined = order + other.filter { it !in backing }
        return Ordered(combined.toSet(), combined)
    }

    infix fun symmetricDifference(other: FiniteSet<A>): Ordered<A> {
        val symmetric = (backing - other.backing) + (other.backing - backing)
        val combinedOrder = order.filter { it in symmetric } +
                other.order.filter { it in symmetric && it !in backing }
        return Ordered(symmetric, combinedOrder)
    }

    /**
     * A FiniteSet that is Ordered with a guaranteed stable ordering.
     */
    class Ordered<A> internal constructor(
        backing: Set<A>,
        order: List<A>
    ) : FiniteSet<A>(backing, order) {
        operator fun get(index: Int): A = order[index]
        fun indexOf(element: A): Int = order.indexOf(element)
    }

    /**
     * A FiniteSet with its iteration order unspecified.
     */
    class Unordered<A> internal constructor(
        backing: Set<A>
    ) : FiniteSet<A>(backing, backing.toList())

    companion object {
        fun <A> ordered(elements: Iterable<A>): Ordered<A> {
            val order = elements.distinct()
            return Ordered(order.toSet(), order)
        }

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

        /**
         * An empty FiniteSet of a given type.
         */
        fun <A> empty(): Ordered<A> = ordered(emptyList())

        /**
         * A FiniteSet containing a single element. Necessarily ordered.
         */
        fun <A> singleton(a: A): FiniteSet<A> = ordered(listOf(a))
    }
}
