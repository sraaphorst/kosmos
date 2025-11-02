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

    infix fun <C, D> andThen(other: PPrism<A, B, C, D>): PPrism<S, T, C, D> =
        PPrism(
            getterOrNull = { s -> getterOrNull(s)?.let(other::getOrNull) },
            reverseGetter = { d -> reverseGet(other.reverseGet(d)) },
            identityT = identityT
        )

    infix fun <C, D> compose(other: PPrism<C, D, S, T>): PPrism<C, D, A, B> =
        other andThen this

    // PLens compose PPrism -> POptional in PLens.
    infix fun <C, D> andThen(other: PLens<A, B, C, D>): POptional<S, T, C, D> =
        POptional(
            getterOrNull = { s -> getterOrNull(s)?.let(other::get) },
            setter = { s, d -> getOrNull(s)
                ?.let { a -> reverseGet(other.set(a, d)) }
                ?: identityT(s)
            },
            identityT
        )

    infix fun <C, D> andThen(other: POptional<A, B, C, D>): POptional<S, T, C, D> =
        POptional(
            getterOrNull = { s -> getterOrNull(s)?.let(other::getOrNull) },
            setter = { s, d ->
                getOrNull(s)?.let { a ->
                    reverseGet(other.set(a, d))
                } ?: identityT(s)
            },
            identityT = identityT
        )

    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        PTraversal(
            getter = { s -> getOrNull(s)?.let { a -> other.getter(a) } ?: emptyList() },
            modify = { s, f ->
                getOrNull(s)?.let { a ->
                    reverseGet(other.modify(a, f))
                } ?: identityT(s)
            }
        )

    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    infix fun <C, D> compose(other: PIso<C, D, S, T>): PPrism<C, D, A, B> =
        other andThen this

    fun toOptional(): POptional<S, T, A, B> = POptional(
        getterOrNull = getterOrNull,
        setter = { _, b -> reverseGetter(b) },
        identityT = identityT
    )

    fun toTraversal(): PTraversal<S, T, A, B> = PTraversal(
        getter = { s -> getOrNull(s)?.let { listOf(it) } ?: emptyList() },
        modify = this::modify
    )

    fun toSetter(): PSetter<S, T, A, B> =
        PSetter(this::modify)
}

/**
 * Monomorphic Prism type.
 */
typealias Prism<S, A> = PPrism<S, S, A, A>
