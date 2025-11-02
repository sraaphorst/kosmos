package org.vorpal.kosmos.functional.optics

import org.vorpal.kosmos.core.Identity

/**
 * Optional focus within a product: may be missing.
 * Requires a way to treat S as T when nothing to modify.
 */
data class POptional<S, T, A, B>(
    val getterOrNull: (S) -> A?,
    val setter: (S, B) -> T,
    val identityT: (S) -> T
): GetterOrNull<S, A>, Setter<S, B, T> {
    override fun getOrNull(s: S): A? = getterOrNull(s)
    override fun set(s: S, b: B): T = setter(s, b)

    fun modify(s: S, f: (A) -> B): T =
        getOrNull(s)?.let { a -> set(s, f(a)) } ?: identityT(s)

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

    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        PTraversal(
            getter = { s -> getOrNull(s)?.let { a -> other.getter(a) } ?: emptyList() },
            modify = { s, f ->
                getOrNull(s)?.let { a ->
                    set(s, other.modify(a, f))
                } ?: identityT(s)
            }
        )

    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    infix fun <C, D> compose(other: PIso<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    infix fun <C, D> compose(other: POptional<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    // PPrism andThen POptional -> POptional in PPrism.
    infix fun <C, D> compose(other: PPrism<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    fun toTraversal(): PTraversal<S, T, A, B> = PTraversal(
        getter = { s -> getOrNull(s)?.let { listOf(it) } ?: emptyList() },
        modify = this::modify
    )

    fun toSetter(): PSetter<S, T, A, B> =
        PSetter(this::modify)

    // For the monomorphic case. This works because S = T.
    companion object {
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
 * This is the same as PLens andThen POptional.
 * This composition can only be done when the PLens is monomorphic.
 */
infix fun <S, A, C, D> POptional<A, A, C, D>.compose(other: Lens<S, A>): POptional<S, S, C, D> =
    other andThen this

typealias Optional<S, A> = POptional<S, S, A, A>
