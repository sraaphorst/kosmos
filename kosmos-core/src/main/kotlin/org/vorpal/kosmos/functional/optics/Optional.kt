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
): GetterOrNull<S, A>, PSetter<S, B, T> {
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

    infix fun <C, D> compose(other: POptional<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    /**
     * This is the same as Lens andThen Optional.
     */
    infix fun <C, D> compose(other: PLens<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

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

typealias Optional<S, A> = POptional<S, S, A, A>
