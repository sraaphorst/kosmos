package org.vorpal.kosmos.functional.optics

data class PTraversal<S, T, A, B>(
    val getter: (S) -> List<A>,
    val modify: (S, (A) -> B) -> T
): Setter<S, B, T> {
    fun get(s: S): List<A> =
        getter(s)

    override fun set(s: S, b: B): T =
        modify(s) { b }

    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        PTraversal(
            getter = { s -> getter(s).flatMap { a -> other.getter(a) } },
            modify = { s, f -> modify(s) { a -> other.modify(a, f) } }
        )

    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    infix fun <C, D> compose(other: PTraversal<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: PLens<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: PIso<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: PPrism<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: POptional<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    fun toSetter(): PSetter<S, T, A, B> =
        PSetter(modify)
}

typealias Traversal<S, A> = PTraversal<S, S, A, A>
