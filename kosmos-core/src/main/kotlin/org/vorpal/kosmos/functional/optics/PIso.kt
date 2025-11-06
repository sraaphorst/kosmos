package org.vorpal.kosmos.functional.optics

/**
 * # Isomorphism (Iso)
 *
 * An **Iso** represents a **lossless, bidirectional transformation** between two types.
 * Think of it as a perfect conversion where you can go back and forth without losing any information.
 *
 * ## When to Use
 *
 * Use an Iso when you have two representations of the same data:
 * - Converting between equivalent data structures (List ↔ Vector)
 * - Encoding/decoding (String ↔ Base64)
 * - Different coordinate systems (Cartesian ↔ Polar)
 * - Wrapping/unwrapping newtypes (UserId(Int) ↔ Int)
 *
 * ## Key Properties
 *
 * An Iso guarantees:
 * 1. **Round-trip identity**: `reverseGet(get(s)) == s`
 * 2. **No information loss**: Every S maps to exactly one A, and vice versa
 * 3. **Perfect conversion**: Both directions always succeed
 *
 * ## Examples
 *
 * ```kotlin
 * // String to List<Char> and back
 * val stringToList = PIso<String, String, List<Char>, List<Char>>(
 *     getter = { it.toList() },
 *     reverseGetter = { it.joinToString("") }
 * )
 *
 * stringToList.get("hello")              // ['h', 'e', 'l', 'l', 'o']
 * stringToList.reverseGet(listOf('h', 'i'))  // "hi"
 *
 * // Temperature: Celsius ↔ Fahrenheit
 * val celsiusToFahrenheit = PIso<Double, Double, Double, Double>(
 *     getter = { c -> c * 9/5 + 32 },
 *     reverseGetter = { f -> (f - 32) * 5/9 }
 * )
 * ```
 *
 * ## Composition
 *
 * Iso is the **most powerful optic** and can compose with any other optic:
 * - `Iso andThen Iso = Iso`
 * - `Iso andThen Lens = Lens`
 * - `Iso andThen Prism = Prism`
 * - `Iso andThen Optional = Optional`
 * - `Iso andThen Traversal = Traversal`
 *
 * ## Optics Hierarchy
 *
 * ```
 * Iso (most powerful - bidirectional, always succeeds)
 *  ├─> Lens (always succeeds reading)
 *  └─> Prism (may fail reading - for sum types)
 *       └─> Optional (may fail reading - for product types)
 *            └─> Traversal (focuses on 0+ elements)
 *                 └─> Setter (write-only, most general)
 * ```
 *
 * ## Type Parameters
 *
 * - [S]: Source type (input)
 * - [T]: Target type (output after reverse transformation)
 * - [A]: Focus type (what we get)
 * - [B]: Modified focus type (what we set back)
 *
 * For simple cases where types don't change, use [Iso]<S, A> = PIso<S, S, A, A>
 *
 * @see PLens for focusing on parts that always exist
 * @see PPrism for focusing on sum type variants
 */
data class PIso<S, T, A, B>(
    /** Extracts focus A from structure S */
    val getter: (S) -> A,
    /** Constructs structure T from focus B (inverse transformation) */
    val reverseGetter: (B) -> T
): Getter<S, A>, ReverseGetter<B, T> {
    override fun get(s: S): A = getter(s)
    override fun reverseGet(b: B): T = reverseGetter(b)

    /**
     * A PIso is reversible: we can completely reverse the parameters to create another PIso in the
     * other direction by swapping the behaviors of get and reverseGet.
     */
    fun inverse(): PIso<B, A, T, S> = PIso(
        getter = reverseGetter,
        reverseGetter = getter
    )

    /**
     * Compose two Isos sequentially.
     *
     * Creates a new Iso that applies this Iso, then the other.
     *
     * Example:
     * ```kotlin
     * val stringToChars: Iso<String, List<Char>> = ...
     * val charsToInts: Iso<List<Char>, List<Int>> = ...
     * val stringToInts = stringToChars andThen charsToInts
     * ```
     */
    infix fun <C, D> andThen(other: PIso<A, B, C, D>): PIso<S, T, C, D> =
        PIso(
            getter = { s -> other.get(get(s)) },
            reverseGetter = { d -> reverseGet(other.reverseGet(d)) }
        )

    /**
     * Compose two Isos in reverse order.
     * `a compose b` is equivalent to `b andThen a`.
     */
    infix fun <C, D> compose(other: PIso<C, D, S, T>): PIso<C, D, A, B> =
        other andThen this

    infix fun <C, D> andThen(other: PLens<A, B, C, D>): PLens<S, T, C, D> =
        toLens() andThen other

    infix fun <C, D> andThen(other: PPrism<A, B, C, D>): PPrism<S, T, C, D> =
        PPrism(
            getterOrNull = { s -> other.getOrNull(get(s)) },
            reverseGetter = { d -> reverseGet(other.reverseGet(d)) },
            identityT = { s -> reverseGet(other.identityT(get(s))) }
        )

    infix fun <C, D> andThen(other: POptional<A, B, C, D>): POptional<S, T, C, D> =
        POptional(
            getterOrNull = { s -> other.getOrNull(get(s)) },
            setter = { s, d -> reverseGet(other.set(get(s), d)) },
            identityT = { s -> reverseGet(other.identityT(get(s))) }
        )

    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        toLens() andThen other

    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    /**
     * Convert this Iso to a Lens.
     *
     * Since Iso is bidirectional, it can always act as a Lens
     * (which only needs to read and write, not reverse).
     */
    fun toLens(): PLens<S, T, A, B> = PLens(
        getter = getter,
        setter = { _, b -> reverseGetter(b) }
    )

    /**
     * Convert this Iso to a Prism.
     *
     * Since Iso always succeeds, the resulting Prism's `getOrNull` never returns null.
     */
    fun toPrism(): PPrism<S, T, A, B> = PPrism(
        getterOrNull = { s -> get(s) },  // Always succeeds for Iso
        reverseGetter = reverseGetter,
        identityT = { _ -> error("Iso always succeeds: this should never be called.") }
    )

    /**
     * Convert this Iso to an Optional.
     *
     * Since Iso always succeeds, the resulting Optional's `getOrNull` never returns null.
     */
    fun toOptional(): POptional<S, T, A, B> = POptional(
        getterOrNull = { s -> get(s) },  // Always succeeds for Iso
        setter = { _, b -> reverseGetter(b) },
        identityT = { _ -> error("Iso always succeeds: this should never be called.") }
    )

    /**
     * Convert this Iso to a Traversal focusing on exactly one element.
     */
    fun toTraversal(): PTraversal<S, T, A, B> = PTraversal(
        getter = { s -> listOf(get(s)) },
        modify = { s, f -> reverseGet(f(get(s))) }
    )

    /**
     * Convert this Iso to a Setter (write-only optic).
     */
    fun toSetter(): PSetter<S, T, A, B> =
        PSetter { s, f -> reverseGetter(f(get(s))) }
}

/**
 * Monomorphic Iso where structure and focus types don't change.
 *
 * This is the most common case and easier to work with.
 */
typealias Iso<S, A> = PIso<S, S, A, A>
