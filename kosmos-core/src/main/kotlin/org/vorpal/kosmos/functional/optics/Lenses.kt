package org.vorpal.kosmos.functional.optics

import org.vorpal.kosmos.core.Identity

/**
 * Lens Laws (should be satisfied by all valid lenses):
 *
 * 1. Get-Put: lens.set(s, lens.get(s)) == s
 *    (Setting what you get doesn't change anything)
 *
 * 2. Put-Get: lens.get(lens.set(s, a)) == a
 *    (Getting what you set retrieves the value you set)
 *
 * 3. Put-Put: lens.set(lens.set(s, a1), a2) == lens.set(s, a2)
 *    (Setting twice is the same as setting the last value)
 */

/**
 * Polymorphic getter.
 */
fun interface Getter<in S, out A> {
    fun get(s: S): A
}

/**
 * Polymorphic setter.
 */
fun interface Setter<in S, in B, out T> {
    fun set(s: S, b: B): T
}

/**
 * Monomorphic read-only getter helper.
 */
typealias MGetter<S, A> = Getter<S, A>

/**
 * Monomorphic write-only setter helper.
 */
typealias MSetter<S, A> = Setter<S, A, S>

fun interface ReverseGetter<in B, out T> {
    fun reverseGet(b: B): T
}

fun interface GetterOrNull<S, A> {
    fun getOrNull(s: S): A?
}

object Lenses {
    // Identity lens.
    fun <S, T> id(): PLens<S, T, S, T> = PLens(
        getter = Identity(),
        setter = { _, t -> t }
    )

    // Lens into a list element
    fun <A> at(index: Int): Optional<List<A>, A> = Optional(
        getterOrNull = { it.getOrNull(index) },
        setter = { list, a ->
            if (index in 0 until list.size) {
                list.toMutableList().apply { this[index] = a }
            } else {
                list
            }
        },
        identityT = Identity()
    )

    // Traversal mapping lens.
    fun <A> each(): Traversal<List<A>, A> = Traversal(
        getter = { it },
        modify = { list, f -> list.map(f) }
    )

    // Map lens.
    fun <K, V> atKey(key: K): Optional<Map<K, V>, V> = Optional.of(
        getterOrNull = { it[key] },
        setter = { map, v -> map + (key to v)}
    )

    // Nullable lens.
    fun <A> nullable(): Prism<A?, A> = Prism(
        getterOrNull = { it },
        reverseGetter = { it },
        identityT = Identity()
    )

    // Lens into a pair
    fun <A, B> first(): Lens<Pair<A, B>, A> = Lens(
        getter = { it.first },
        setter = { p, a -> p.copy(first = a) }
    )

    fun <A, B> second(): Lens<Pair<A, B>, B> = Lens(
        getter = { it.second },
        setter = { p, b -> p.copy(second = b) }
    )

    fun <A> both(): Traversal<Pair<A, A>, A> = Traversal(
        getter = { listOf(it.first, it.second) },
        modify = { pair, f -> Pair(f(pair.first), f(pair.second)) }
    )

    fun <A, B, C> triple1(): Lens<Triple<A, B, C>, A> = Lens(
        getter = { it.first },
        setter = { t, a -> t.copy(first = a) }
    )

    fun <A, B, C> triple2(): Lens<Triple<A, B, C>, B> = Lens(
        getter = { it.second },
        setter = { t, b -> t.copy(second = b) }
    )

    fun <A, B, C> triple3(): Lens<Triple<A, B, C>, C> = Lens(
        getter = { it.third },
        setter = { t, c -> t.copy(third = c) }
    )

    fun <A> allThree(): Traversal<Triple<A, A, A>, A> = Traversal(
        getter = { listOf(it.first, it.second, it.third) },
        modify = { triple, f -> Triple(f(triple.first), f(triple.second), f(triple.third)) }
    )
}
