package org.vorpal.kosmos.functional.optics

/**
 * Sum-type focus: may fail to match.
 */
data class PPrism<S, T, A, B>(
    val getterOrNull: (S) -> A?,
    val reverseGetter: (B) -> T,
    val identityT: (S) -> T
): GetterOrNull<S, A>, ReverseGetter<B, T> {
    override fun getOrNull(s: S): A? = getterOrNull(s)
    override fun reverseGet(b: B): T = reverseGetter(b)

    fun modify(s: S, f: (A) -> B): T =
        getOrNull(s)?.let { a -> reverseGet(f(a)) } ?: identityT(s)

    fun <C, D> andThen(other: PLens<A, B, C, D>): POptional<S, T, C, D> =
        POptional(
            getterOrNull = { s -> getterOrNull(s)?.let(other::get) },
            setter = { s, d -> getOrNull(s)
                ?.let { a -> reverseGet(other.set(a, d)) }
                ?: identityT(s)
            },
            identityT
        )

    fun toOptional(): POptional<S, T, A, B> = POptional(
        getterOrNull = getterOrNull,
        setter = { _, b -> reverseGetter(b) },
        identityT = identityT
    )
}

typealias Prism<S, A> = PPrism<S, S, A, A>
