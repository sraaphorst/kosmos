package org.vorpal.kosmos.functional.optics

/**
 * Most general write-only optic.
 */
data class PSetter<S, T, A, B>(
    val modify: (S, (A) -> B) -> T
): Setter<S, B, T> {
    override fun set(s: S, b: B): T = modify(s) { b }

    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        PSetter { s, f -> modify(s) { a -> other.modify(a, f) } }

    infix fun <C, D> compose(other: PSetter<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: PLens<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: POptional<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: PPrism<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: PTraversal<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this
}
