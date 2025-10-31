package org.vorpal.kosmos.functional.optics

/**
 * Isomorphic lens: lossless both ways.
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

    fun toLens(): PLens<S, T, A, B> = PLens(
        getter = getter,
        setter = { _, b -> reverseGetter(b) }
    )
}

typealias Iso<S, A> = PIso<S, S, A, A>
