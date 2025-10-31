package org.vorpal.kosmos.functional.optics

import org.vorpal.kosmos.core.Identity

/**
 * Lens Laws (should be satisfied by all valid lenses):
 *
 * 1. Get-Put: lens.set(s, lens.get(s)) == s
 *    (Setting what you get doesn't change anything)
 *
 * 2. Put-Get: lens.get(lens.set(s, a)) == a
 *    (Getting what you set retrieves the value you set)
 *
 * 3. Put-Put: lens.set(lens.set(s, a1), a2) == lens.set(s, a2)
 *    (Setting twice is the same as setting the last value)
 */

/**
 * Polymorphic getter.
 */
fun interface PGetter<in S, out A> {
    fun get(s: S): A
}

/**
 * Polymorphic setter.
 */
fun interface PSetter<in S, in B, out T> {
    fun set(s: S, b: B): T
}

/**
 * Monomorphic read-only getter helper.
 */
typealias Getter<S, A> = PGetter<S, A>

/**
 * Monomorphic write-only setter helper.
 */
typealias Setter<S, A> = PSetter<S, A, S>

fun interface ReverseGetter<in B, out T> {
    fun reverseGet(b: B): T
}

fun interface GetterOrNull<S, A> {
    fun getOrNull(s: S): A?
}

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
): PGetter<S, A>, PSetter<S, B, T> {
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

    /**
     * Compose lenses (opposite order of andThen).
     */
    infix fun <C, D> compose(other: PLens<C, D, S, T>): PLens<C, D, A, B> =
        other andThen this

    /**
     * Lens andThen Optional = Optional.
     */
    infix fun <C, D> andThen(other: POptional<A, B, C, D>): POptional<S, T, C, D> =
        POptional(
            getterOrNull = { s -> other.getOrNull(get(s)) },
            setter = { s, d ->
                val a = get(s)
                set(s, other.set(a, d))
            },
            identityT = { _ -> error("Lens always succeeds: this should never be called.") }
        )

    // For polymorphic lenses, document that conversion isn't fully supported
    // or throw an error if someone tries to use the default path.
    fun toOptional(): POptional<S, T, A, B> = POptional(
        getterOrNull = { s -> get(s) },
        setter = setter,
        identityT = { _ -> error("Lens always succeeds: this should never be called.") }
    )

    companion object {
        fun <S, T> id(): PLens<S, T, S, T> = PLens(
            getter = Identity(),
            setter = { _, t -> t }
        )
    }
}

fun <S> lens(): Lens<S, S> = Lens(getter = { it }, setter = { _, b -> b } )

/**
 * Monomorphic alias, appropriate for most use.
 */
typealias Lens<S, A> = PLens<S, S, A, A>

fun <S, A> Lens<S, A>.toOptional(): Optional<S, A> = Optional.of(
    getterOrNull = { s -> get(s) },
    setter = this::set
)

/**
 * Useful ergonomics.
 */
infix fun <S, A, B> Lens<S, A>.then(other: Lens<A, B>): Lens<S, B> = this andThen other
operator fun <S, A> Lens<S, A>.invoke(s: S): A = get(s)
fun <S, A> Lens<S, A>.setTo(a: A): (S) -> S = { s -> set(s, a) }
