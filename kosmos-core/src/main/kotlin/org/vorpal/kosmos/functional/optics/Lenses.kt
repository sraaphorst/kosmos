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

/**
 * # Common Optics Library
 *
 * Pre-built optics for common data structures like Pair, List, and Map.
 * These save you from writing the same optics over and over.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // Accessing tuple elements
 * val pair = Pair("Alice", 30)
 * Lenses.first<String, Int>().get(pair)        // "Alice"
 * Lenses.second<String, Int>().get(pair)       // 30
 *
 * // List access
 * val list = listOf(1, 2, 3)
 * Lenses.at<Int>(1).getOrNull(list)            // 2
 * Lenses.at<Int>(10).getOrNull(list)           // null (out of bounds)
 *
 * // Traversing all elements
 * Lenses.each<Int>().modify(list) { it * 2 }   // [2, 4, 6]
 *
 * // Map operations
 * val map = mapOf("a" to 1, "b" to 2)
 * Lenses.atKey<String, Int>("a").getOrNull(map)           // 1
 * Lenses.atKey<String, Int>("c").getOrNull(map)           // null
 * Lenses.atKey("a").set(map, 10)                          // {"a": 10, "b": 2}
 *
 * // Nullable values
 * val nullable: String? = "hello"
 * Lenses.nullable<String>().getOrNull(nullable)           // "hello"
 * Lenses.nullable<String>().getOrNull(null)               // null
 * ```
 */
object Lenses {
    /**
     * Identity lens that focuses on the entire structure.
     *
     * Useful as a starting point for composition or when you need a "do nothing" lens.
     *
     * Example:
     * ```kotlin
     * val id = Lenses.id<Person, Person>()
     * id.get(person)  // Returns person unchanged
     * id.set(person, newPerson)  // Returns newPerson
     * ```
     */
    fun <S, T> id(): PLens<S, T, S, T> = PLens(
        getter = Identity(),
        setter = { _, t -> t }
    )

    /**
     * Optional for accessing a list element at a specific index.
     *
     * Returns null if index is out of bounds. Setting at an out-of-bounds index
     * returns the list unchanged.
     *
     * Example:
     * ```kotlin
     * val list = listOf(10, 20, 30)
     *
     * Lenses.at<Int>(1).getOrNull(list)              // 20
     * Lenses.at<Int>(10).getOrNull(list)             // null
     *
     * Lenses.at<Int>(1).set(list, 99)                // [10, 99, 30]
     * Lenses.at<Int>(10).set(list, 99)               // [10, 20, 30] - unchanged
     *
     * Lenses.at<Int>(1).modify(list) { it * 2 }      // [10, 40, 30]
     * ```
     */
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

    /**
     * Traversal focusing on all elements in a list.
     *
     * Allows you to get all elements or transform them all at once.
     *
     * Example:
     * ```kotlin
     * val numbers = listOf(1, 2, 3, 4)
     *
     * Lenses.each<Int>().get(numbers)                 // [1, 2, 3, 4]
     * Lenses.each<Int>().modify(numbers) { it * 2 }   // [2, 4, 6, 8]
     * Lenses.each<Int>().set(numbers, 0)              // [0, 0, 0, 0]
     * ```
     */
    fun <A> each(): Traversal<List<A>, A> = Traversal(
        getter = { it },
        modify = { list, f -> list.map(f) }
    )

    /**
     * Optional for accessing a map value by key.
     *
     * Returns null if key doesn't exist. Setting a key that doesn't exist adds it to the map.
     *
     * Example:
     * ```kotlin
     * val map = mapOf("a" to 1, "b" to 2)
     *
     * Lenses.atKey<String, Int>("a").getOrNull(map)             // 1
     * Lenses.atKey<String, Int>("c").getOrNull(map)             // null
     *
     * Lenses.atKey("a").set(map, 10)                            // {"a": 10, "b": 2}
     * Lenses.atKey("c").set(map, 3)                             // {"a": 1, "b": 2, "c": 3}
     *
     * Lenses.atKey("a").modify(map) { it * 2 }                  // {"a": 2, "b": 2}
     * ```
     */
    fun <K, V> atKey(key: K): Optional<Map<K, V>, V> = Optional.of(
        getterOrNull = { it[key] },
        setter = { map, v -> map + (key to v) }
    )

    /**
     * Prism for accessing the value inside a nullable type.
     *
     * Matches only when the value is non-null.
     *
     * Example:
     * ```kotlin
     * val hasValue: String? = "hello"
     * val noValue: String? = null
     *
     * Lenses.nullable<String>().getOrNull(hasValue)             // "hello"
     * Lenses.nullable<String>().getOrNull(noValue)              // null
     *
     * Lenses.nullable<String>().modify(hasValue) { it.uppercase() }    // "HELLO"
     * Lenses.nullable<String>().modify(noValue) { it.uppercase() }     // null
     *
     * Lenses.nullable<String>().reverseGet("world")                    // "world"
     * ```
     */
    fun <A> nullable(): Prism<A?, A> = Prism(
        getterOrNull = { it },
        reverseGetter = { it },
        identityT = Identity()
    )

    /**
     * Lens focusing on the first element of a Pair.
     *
     * Example:
     * ```kotlin
     * val pair = Pair("Alice", 30)
     * Lenses.first<String, Int>().get(pair)           // "Alice"
     * Lenses.first<String, Int>().set(pair, "Bob")    // Pair("Bob", 30)
     * ```
     */
    fun <A, B> first(): Lens<Pair<A, B>, A> = Lens(
        getter = { it.first },
        setter = { p, a -> p.copy(first = a) }
    )

    /**
     * Lens focusing on the second element of a Pair.
     *
     * Example:
     * ```kotlin
     * val pair = Pair("Alice", 30)
     * Lenses.second<String, Int>().get(pair)          // 30
     * Lenses.second<String, Int>().set(pair, 31)      // Pair("Alice", 31)
     * ```
     */
    fun <A, B> second(): Lens<Pair<A, B>, B> = Lens(
        getter = { it.second },
        setter = { p, b -> p.copy(second = b) }
    )

    /**
     * Traversal focusing on both elements of a **homogeneous** pair.
     *
     * Only works when both elements have the same type.
     *
     * Example:
     * ```kotlin
     * val pair = Pair(1, 2)
     *
     * Lenses.both<Int>().get(pair)                     // [1, 2]
     * Lenses.both<Int>().modify(pair) { it * 2 }       // Pair(2, 4)
     * Lenses.both<Int>().set(pair, 0)                  // Pair(0, 0)
     * ```
     */
    fun <A> both(): Traversal<Pair<A, A>, A> = Traversal(
        getter = { listOf(it.first, it.second) },
        modify = { pair, f -> Pair(f(pair.first), f(pair.second)) }
    )

    /**
     * Lens focusing on the first element of a Triple.
     *
     * Example:
     * ```kotlin
     * val triple = Triple("Alice", 30, listOf("Fluffy", "Fido"))
     * Lenses.triple1<String, Int, List<String>>().get(triple)         // "Alice"
     * Lenses.triple1<String, Int, List<String>>().set(triple, "Alex") // Triple("Alex", 30, ["Fluffy", "Fido"])
     * ```
     */
    fun <A, B, C> triple1(): Lens<Triple<A, B, C>, A> = Lens(
        getter = { it.first },
        setter = { t, a -> t.copy(first = a) }
    )

    /**
     * Lens focusing on the second element of a Triple.
     *
     * Example:
     * ```kotlin
     * val triple = Triple("Alice", 30, listOf("Fluffy", "Fido"))
     * Lenses.triple2<String, Int, List<String>>().get(triple)     // 30
     * Lenses.triple2<String, Int, List<String>>().set(triple, 31) // Triple("Alice", 31, ["Fluffy", "Fido"])
     * ```
     */
    fun <A, B, C> triple2(): Lens<Triple<A, B, C>, B> = Lens(
        getter = { it.second },
        setter = { t, b -> t.copy(second = b) }
    )

    /**
     * Lens focusing on the third element of a Triple.
     *
     * Example:
     * ```kotlin
     * val triple = Triple("Alice", 30, listOf("Fluffy", "Fido"))
     * Lenses.triple3<String, Int, List<String>>().get(triple)    // ["Fluffy", "Fido"]
     * Lenses.triple3<String, Int, List<String>>().set(triple, listOf("Fluffy", "Fido", "Felix")) // Triple("Alice", 30, ["Fluffy", "Fido", "Felix"])
     * ```
     */
    fun <A, B, C> triple3(): Lens<Triple<A, B, C>, C> = Lens(
        getter = { it.third },
        setter = { t, c -> t.copy(third = c) }
    )

    /**
     * Traversal focusing on all three elements of a **homogeneous** triple.
     *
     * Only works when all elements have the same type.
     *
     * Example:
     * ```kotlin
     * val triple = Triple(3, 2, 1)
     *
     * Lenses.allThree<Int>().get(triple)                // [3, 2, 1]
     * Lenses.allThree<Int>().modify(triple) { it * 2 }  // Triple(6, 4, 2)
     * Lenses.allThree<Int>().set(triple, 0)             // Triple(0, 0, 0)
     * ```
     */
    fun <A> allThree(): Traversal<Triple<A, A, A>, A> = Traversal(
        getter = { listOf(it.first, it.second, it.third) },
        modify = { triple, f -> Triple(f(triple.first), f(triple.second), f(triple.third)) }
    )
}
