package org.vorpal.kosmos.functional.optics

/**
 * Isomorphic lens: lossless both ways.
 *
 * Since `getOrNull` always succeeds for an Iso-derived Prism/Optional,
 * the `identityT` is never actually called in `modify()`.
 * It's a "this can't happen" branch.
 * Even when composed with other optics, the failure only propagates from the *other* optic, not from the Iso part.
 */
data class PIso<S, T, A, B>(
    val getter: (S) -> A,
    val reverseGetter: (B) -> T
): Getter<S, A>, ReverseGetter<B, T> {
    override fun get(s: S): A = getter(s)
    override fun reverseGet(b: B): T = reverseGetter(b)

    infix fun <C, D> andThen(other: PIso<A, B, C, D>): PIso<S, T, C, D> =
        PIso(
            getter = { s -> other.get(get(s)) },
            reverseGetter = { d -> reverseGet(other.reverseGet(d)) }
        )

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

    fun toLens(): PLens<S, T, A, B> = PLens(
        getter = getter,
        setter = { _, b -> reverseGetter(b) }
    )

    fun toPrism(): PPrism<S, T, A, B> = PPrism(
        getterOrNull = { s -> get(s) },  // Always succeeds for Iso
        reverseGetter = reverseGetter,
        identityT = { _ -> error("Iso always succeeds: this should never be called.") }
    )

    fun toOptional(): POptional<S, T, A, B> = POptional(
        getterOrNull = { s -> get(s) },  // Always succeeds for Iso
        setter = { _, b -> reverseGetter(b) },
        identityT = { _ -> error("Iso always succeeds: this should never be called.") }
    )

    fun toTraversal(): PTraversal<S, T, A, B> = PTraversal(
        getter = { s -> listOf(get(s)) },  // Focuses on exactly one element
        modify = { s, f -> reverseGet(f(get(s))) }
    )

    fun toSetter(): PSetter<S, T, A, B> =
        PSetter { s, f -> reverseGetter(f(get(s))) }
}

typealias Iso<S, A> = PIso<S, S, A, A>
