package org.vorpal.kosmos.core.multiset

import org.vorpal.kosmos.core.finiteset.FiniteSet

/**
 * A multiset is a set of elements with multiplicity, i.e. the number of times an element occurs.
 * Unlike a standard [FiniteSet], a multiset allows for elements to occur multiple times.
 */
interface Multiset<T : Any> {
    /**
     * The set of elements that occur in this multiset.
     */
    val support: FiniteSet<T>

    /**
     * The number of times an element occurs in this multiset.
     */
    fun multiplicity(element: T): Int

    /**
     * The number of elements in this multiset.
     */
    val size: Int
        get() = support.fold(0) { acc, t -> acc + multiplicity(t) }
    val isEmpty: Boolean
        get() = support.isEmpty
    val isNotEmpty: Boolean
        get() = !isEmpty

    operator fun plus(other: Multiset<T>): Multiset<T> = of(
        (support union other.support).associateWith {
            multiplicity(it) + other.multiplicity(it)
        }.filterValues { it > 0 }
    )

    fun filter(predicate: (T) -> Boolean): Multiset<T> = of(
        support.filter(predicate).associateWith(::multiplicity)
    )

    operator fun contains(element: T): Boolean =
        multiplicity(element) > 0

    infix fun max(other: Multiset<T>): Multiset<T> = of(
        (support union other.support).associateWith {
            maxOf(multiplicity(it), other.multiplicity(it))
        }
    )

    infix fun min(other: Multiset<T>): Multiset<T> = of(
        (support union other.support).associateWith {
            minOf(multiplicity(it), other.multiplicity(it))
        }.filterValues { it > 0 }
    )

    infix fun isSubmultisetOf(other: Multiset<T>): Boolean =
        support.all { multiplicity(it) <= other.multiplicity(it) }

    infix fun isSupermultisetOf(other: Multiset<T>): Boolean =
        other isSubmultisetOf this

    infix fun isSubsetOf(other: FiniteSet<T>): Boolean =
        support.all { it in other && multiplicity(it) == 1 }

    infix fun isSupersetOf(other: FiniteSet<T>): Boolean =
        other.all { it in this }

    companion object {
        fun <T : Any> of(
            map: Map<T, Int>
        ): Multiset<T> = object : Multiset<T> {
            init {
                require(map.values.all { it > 0 }) {
                    "Multiplicity of elements in Multiset cannot be zero or negative"
                }
            }
            override val support: FiniteSet<T> = FiniteSet.unordered(map.keys)
            override fun multiplicity(element: T) = map[element] ?: 0
        }

        fun <T : Any> of(
            collection: Collection<T>
        ): Multiset<T> = of(collection.groupingBy { it }.eachCount().mapValues { it.value })

        fun <T : Any> singleton(element: T): Multiset<T> = of(mapOf(element to 1))
        fun <T : Any> empty(): Multiset<T> = of(emptyMap())
    }
}

fun <T : Any> Collection<T>.toMultiset(): Multiset<T> =
    Multiset.of(this)

fun <T : Any> FiniteSet<T>.toMultiset() = Multiset.of(
    associateWith { 1 }
)

fun <T : Any> FiniteSet<T>.isSubsetOf(other: Multiset<T>): Boolean =
    other isSupersetOf this
fun <T : Any> FiniteSet<T>.isSupersetOf(other: Multiset<T>): Boolean =
    other isSubsetOf this
