package org.vorpal.kosmos.categories

class FiniteSet<A> private constructor(
    private val backing: Set<A>,
    private val order: List<A>
) : Iterable<A> {
    val size: Int get() = backing.size
    fun toSet(): Set<A> = backing
    fun toList(): List<A> = order
    override fun iterator(): Iterator<A> = order.iterator()
    operator fun contains(a: A): Boolean = backing.contains(a)

    companion object {
        // FiniteSets must be created through the companion object methods.
        fun <A> of(elements: Iterable<A>, comparator: Comparator<A>? = null): FiniteSet<A> {
            val s = LinkedHashSet<A>()
            elements.forEach { e -> require(s.add(e)) { "Duplicate element in FiniteSet: $e" } }
            val ord = if (comparator == null) s.toList() else s.toList().sortedWith(comparator)
            return FiniteSet(s, ord)
        }

        fun <A> ofSet(set: Set<A>, comparator: Comparator<A>? = null): FiniteSet<A> =
            of(set.asIterable(), comparator)

        fun <A> empty(): FiniteSet<A> = of(emptyList())
        fun <A> singleton(a: A): FiniteSet<A> = of(listOf(a))

        fun <A : Comparable<A>> sortedOf(elements: Iterable<A>) =
            of(elements, Comparator.naturalOrder<A>())
    }
}
