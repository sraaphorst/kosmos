package org.vorpal.kosmos.functional.optics

import org.vorpal.kosmos.core.Identity

/**
 * A polymorphic lens: allows the focus type to change under update.
 *
 * Type parameters:
 * - [S]: Source type (input structure before modification)
 * - [T]: Target type (output structure after modification)
 * - [A]: Focus type before modification (what we view)
 * - [B]: Focus type after modification (what we set)
 *
 * For monomorphic lenses where types don't change, use [Lens]<S, A>
 * which is equivalent to PLens<S, S, A, A>.
 */
class PLens<S, T, A, B>(
    private val getter: (S) -> A,
    private val setter: (S, B) -> T
): Getter<S, A>, Setter<S, B, T> {
    override fun get(s: S): A = getter(s)
    override fun set(s: S, b: B): T = setter(s, b)

    fun modify(s: S, f: (A) -> B): T =
        set(s, f(get(s)))

    /**
     * Apply this lens and then another lens.
     */
    infix fun <C, D> andThen(other: PLens<A, B, C, D>): PLens<S, T, C, D> =
        PLens(
            getter = { s -> other.get(get(s)) },
            setter = { s, d -> set(s, other.set(get(s), d)) }
        )

    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        PTraversal(
            getter = { s -> other.getter(get(s)) },
            modify = { s, f -> set(s, other.modify(get(s), f)) }
        )

    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    /**
     * Compose lenses (opposite order of andThen).
     */
    infix fun <C, D> compose(other: PLens<C, D, S, T>): PLens<C, D, A, B> =
        other andThen this

    // PPrism andThen PLens -> POptional in PPrism.
    infix fun <C, D> compose(other: PPrism<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    // PIso andThen PLens -> PLens in PIso.
    infix fun <C, D> compose(other: PIso<C, D, S, T>): PLens<C, D, A, B> =
        other andThen this

    fun toTraversal(): PTraversal<S, T, A, B> = PTraversal(
        getter = { s -> listOf(get(s)) },
        modify = { s, f -> modify(s, f) }
    )

    fun toSetter(): PSetter<S, T, A, B> =
        PSetter(this::modify)
}

/**
 * Monomorphic alias, appropriate for most use.
 */
typealias Lens<S, A> = PLens<S, S, A, A>

/**
 * This conversion can only be done when the PLens is monomorphic.
 */
fun <S, A> Lens<S, A>.toOptional(): Optional<S, A> = Optional.of(
    getterOrNull = { s -> get(s) },
    setter = this::set
)

/**
 * Useful ergonomics.
 */
operator fun <S, A> Lens<S, A>.invoke(s: S): A = get(s)
fun <S, A> Lens<S, A>.setTo(a: A): (S) -> S = { s -> set(s, a) }

/**
 * This composition can only be done when the PLens is monomorphic.
 */
infix fun <S, A, C, D> Lens<S, A>.andThen(other: POptional<A, A, C, D>): POptional<S, S, C, D> =
    POptional(
        getterOrNull = { s -> other.getOrNull(get(s)) },
        setter = { s, d ->
            val a = get(s)
            set(s, other.set(a, d))
        },
        identityT = Identity()
    )
