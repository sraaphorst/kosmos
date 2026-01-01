package org.vorpal.kosmos.functional.optics

import org.vorpal.kosmos.core.Identity

/**
 * # Optional
 *
 * An **Optional** focuses on a part of a structure that **might be missing**.
 * Unlike Lens (always exists) and Prism (sum type variant), Optional is for **product types**
 * where the focus could be absent - like nullable fields, map lookups, or list indices.
 *
 * ## When to Use
 *
 * Use an Optional when accessing:
 * - **Nullable fields** (`person.middleName: String?`)
 * - **Map lookups** (`map[key]` might not exist)
 * - **List indices** (`list[5]` might be out of bounds)
 * - **Optional relationships** (head of empty list, first match in search)
 *
 * ## Key Difference from Prism
 *
 * Both Optional and Prism can fail to find a focus, but:
 * - **Prism**: For **sum types** - "Is this the Right variant? The Circle case?"
 * - **Optional**: For **product types** - "Is there a value at this key? This index?"
 *
 * The internal difference: Prism has `reverseGetter` (can build the sum type), Optional uses `setter`.
 *
 * ## Examples
 *
 * ### Map Lookup
 *
 * ```kotlin
 * val map = mapOf("a" to 1, "b" to 2)
 *
 * val atKeyA = Optional.of<Map<String, Int>, Int>(
 *     getterOrNull = { it["a"] },
 *     setter = { map, value -> map + ("a" to value) }
 * )
 *
 * atKeyA.getOrNull(map)              // 1
 * atKeyA.getOrNull(emptyMap())       // null
 * atKeyA.set(map, 10)                // mapOf("a" to 10, "b" to 2)
 * atKeyA.modify(map) { it * 2 }      // mapOf("a" to 2, "b" to 2)
 * ```
 *
 * ### List Index
 *
 * ```kotlin
 * val list = listOf(1, 2, 3)
 *
 * val at1 = Optional.of<List<Int>, Int>(
 *     getterOrNull = { it.getOrNull(1) },
 *     setter = { list, value ->
 *         if (1 in list.indices) list.toMutableList().apply { this[1] = value }
 *         else list
 *     }
 * )
 *
 * at1.getOrNull(list)              // 2
 * at1.getOrNull(listOf(1))         // null (index out of bounds)
 * at1.set(list, 20)                // [1, 20, 3]
 * at1.set(listOf(1), 20)           // [1] - unchanged, index doesn't exist
 * ```
 *
 * ### Nullable Field
 *
 * ```kotlin
 * data class Person(val name: String, val middleName: String?)
 *
 * val middleNameOptional = Optional.of<Person, String>(
 *     getterOrNull = { it.middleName },
 *     setter = { person, name -> person.copy(middleName = name) }
 * )
 *
 * val john = Person("John", "Fitzgerald")
 * val alice = Person("Alice", null)
 *
 * middleNameOptional.getOrNull(john)           // "Fitzgerald"
 * middleNameOptional.getOrNull(alice)          // null
 * middleNameOptional.modify(john) { it.uppercase() }   // Person("John", "FITZGERALD")
 * middleNameOptional.modify(alice) { it.uppercase() }  // Person("Alice", null) - unchanged
 * ```
 *
 * ## Optional Laws
 *
 * Valid Optionals should satisfy:
 *
 * 1. **Get-Set**: If `getOrNull` succeeds, setting that value doesn't change the structure
 *    - `getOrNull(s)?.let { a -> set(s, a) == s }`
 *
 * 2. **Set-Get**: Getting after setting returns the value you set (if it was settable)
 *    - If set succeeds: `getOrNull(set(s, a)) == a`
 *
 * ## Composition
 *
 * - `Optional andThen Optional = Optional` - Chain optional accesses
 * - `Optional andThen Lens = Optional` - After optional access, guaranteed field
 * - `Optional andThen Prism = Optional` - After optional access, variant matching
 * - `Optional andThen Traversal = Traversal` - After optional access, multiple elements
 *
 * Also results from:
 * - `Lens andThen Optional = Optional`
 * - `Prism andThen Lens = Optional`
 * - `Prism andThen Optional = Optional`
 *
 * ## Type Parameters
 *
 * - [S]: Source type (input structure)
 * - [T]: Target type (output structure after modification)
 * - [A]: Focus type (what we try to get)
 * - [B]: Modified focus type (what we set)
 *
 * For monomorphic optionals, use [Optional]<S, A> = POptional<S, S, A, A>
 *
 * @see PLens for focuses that always exist
 * @see PPrism for sum type focuses
 */
data class POptional<S, T, A, B>(
    /** Try to extract the focus - returns null if not present */
    val getterOrNull: (S) -> A?,
    /** Update the focus in the structure (may create it if it doesn't exist) */
    val setter: (S, B) -> T,
    /** Return structure unchanged when focus is absent */
    val identityT: (S) -> T
): GetterOrNull<S, A>, Setter<S, B, T> {

    /** Try to get the focus - returns null if not present */
    override fun getOrNull(s: S): A? = getterOrNull(s)

    /** Set the focus in the structure */
    override fun set(s: S, b: B): T = setter(s, b)

    /**
     * Modify the focus if present, otherwise return structure unchanged.
     *
     * This is the key operation - gracefully handles the case where focus is missing.
     *
     * Example:
     * ```kotlin
     * atKey("a").modify(map) { it * 2 }  // doubles value if key exists
     * atKey("z").modify(map) { it * 2 }  // returns map unchanged if key missing
     * ```
     */
    fun modify(s: S, f: (A) -> B): T =
        getOrNull(s)?.let { a -> set(s, f(a)) } ?: identityT(s)

    /**
     * Compose two Optionals - both might fail.
     *
     * Example:
     * ```kotlin
     * val opt1 = atKey<String, Map<String, Int>>("user")      // Map<String, Map<...>> -> Map<...>?
     * val opt2 = atKey<String, Int>("age")                     // Map<String, Int> -> Int?
     * val userAge = opt1 andThen opt2                          // Map<...> -> Int?
     * ```
     */
    infix fun <C, D> andThen(other: POptional<A, B, C, D>): POptional<S, T, C, D> =
        POptional(
            getterOrNull = { s -> getOrNull(s)?.let(other::getOrNull) },
            setter = { s, d ->
                val a = getOrNull(s)
                if (a == null) identityT(s)
                else set(s, other.set(a, d))
            },
            identityT = identityT
        )

    /**
     * Compose Optional with Traversal.
     *
     * Results in Traversal - focuses on multiple elements if Optional succeeds, zero otherwise.
     */
    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        PTraversal(
            getter = { s -> getOrNull(s)?.let { a -> other.getter(a) } ?: emptyList() },
            modify = { s, f ->
                getOrNull(s)?.let { a ->
                    set(s, other.modify(a, f))
                } ?: identityT(s)
            }
        )

    /**
     * Compose Optional with Setter.
     */
    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    /**
     * Reverse composition with Iso.
     */
    infix fun <C, D> compose(other: PIso<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with another Optional.
     */
    infix fun <C, D> compose(other: POptional<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Prism.
     */
    infix fun <C, D> compose(other: PPrism<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    /**
     * Convert Optional to Traversal that focuses on 0 or 1 element.
     */
    fun toTraversal(): PTraversal<S, T, A, B> = PTraversal(
        getter = { s -> getOrNull(s)?.let { listOf(it) } ?: emptyList() },
        modify = this::modify
    )

    /**
     * Convert Optional to Setter (write-only optic).
     */
    fun toSetter(): PSetter<S, T, A, B> =
        PSetter(this::modify)

    companion object {
        /**
         * Create a monomorphic Optional.
         *
         * This is the most common way to create an Optional since it handles the
         * identity case automatically.
         *
         * Example:
         * ```kotlin
         * val atKeyA = Optional.of<Map<String, Int>, Int>(
         *     getterOrNull = { it["a"] },
         *     setter = { map, value -> map + ("a" to value) }
         * )
         * ```
         */
        fun <S, A> of(
            getterOrNull: (S) -> A?,
            setter: (S, A) -> S
        ): Optional<S, A> = POptional(
            getterOrNull = getterOrNull,
            setter = setter,
            identityT = Identity()
        )
    }
}

/**
 * Reverse composition helper - Optional with Lens.
 *
 * This requires the Lens to be monomorphic.
 */
infix fun <S, A, C, D> POptional<A, A, C, D>.compose(other: Lens<S, A>): POptional<S, S, C, D> =
    other andThen this

/**
 * Monomorphic Optional where types don't change.
 *
 * This is the most common case: `Optional<Map<String, Int>, Int>` for map lookup.
 */
typealias Optional<S, A> = POptional<S, S, A, A>