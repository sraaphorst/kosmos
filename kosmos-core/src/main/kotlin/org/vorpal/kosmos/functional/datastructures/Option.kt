package org.vorpal.kosmos.functional.datastructures

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.functional.optics.Prism
import org.vorpal.kosmos.functional.core.Kind
import org.vorpal.kosmos.functional.instances.option.OptionApplicative

/**
 * The tag type for Option's "F".
 * There is exactly one instance of this type; it is only used at the type level.
 */
//object ForOption
//typealias OptionOf<A> = Kind<ForOption, A>
typealias OptionOf<A> = Kind<Option<*>, A>

/**
 * Safe downcast from `Kind<ForOption, A>` back to `Option<A>`.
 *
 * Remember that `OptionOf<A>` is an alias to `Kind<ForOption, A>`.
 *
 * Use only when you *know* the [Kind] originated from [Option] (the type system enforces this).
 */
@Suppress("UNCHECKED_CAST")
fun <A> OptionOf<A>.fix(): Option<A> = this as Option<A>


/**
 * A minimal Option ADT that participates in the HKT encoding by extending Kind<ForOption, A>.
 */
sealed class Option<out A>: OptionOf<A> {
    data class Some<A>(val value: A) : Option<A>()
    data object None : Option<Nothing>()

    companion object {
        fun <A> of(value: A?) = value?.let { Some(it) } ?: None
    }
}

inline fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
    is Option.Some -> Option.Some(f(value))
    is Option.None -> Option.None
}

inline fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
    is Option.Some -> f(value)
    is Option.None -> Option.None
}

fun <A> Option<Option<A>>.flatten(): Option<A> = when (this) {
    is Option.Some -> value
    is Option.None -> Option.None
}

inline fun <A, B> Option<A>.fold(onNone: () -> B, onSome: (A) -> B): B = when (this) {
    is Option.Some -> onSome(value)
    is Option.None -> onNone()
}

fun <A, B> Option<A>.zip(other: Option<B>): Option<Pair<A, B>> =
    flatMap { a -> other.map { b -> a to b } }

fun <A> Option<A>.getOrElse(default: () -> A): A = when (this) {
    is Option.Some -> value
    is Option.None -> default()
}

fun <A> Option<A>.getOrNull(): A? = when (this) {
    is Option.Some -> value
    is Option.None -> null
}

fun <A> Option<A>.orElse(other: Option<A>): Option<A> = when (this) {
    is Option.Some -> this
    is Option.None -> other
}

inline fun <A> Option<A>.orElseGet(other: () -> A): A = when (this) {
    is Option.Some -> value
    is Option.None -> other()
}

fun <A> Option<A>.isPresent(): Boolean = when (this) {
    is Option.Some -> true
    is Option.None -> false
}

fun <A> Option<A>.isEmpty(): Boolean =
    !isPresent()

inline fun <A> Option<A>.filter(predicate: (A) -> Boolean): Option<A> = when (this) {
    is Option.Some -> if (predicate(value)) this else Option.None
    is Option.None -> this
}

inline fun <A> Option<A>.exists(p: (A) -> Boolean): Boolean = when (this) {
    is Option.Some -> p(value)
    is Option.None -> false
}

inline fun <A> Option<A>.forAll(p: (A) -> Boolean): Boolean = when (this) {
    is Option.Some -> p(value)
    is Option.None -> true
}

/**
 * Perform an action with this Option, and then return it.
 * Allows inserting [tap] in chains of computation without altering values.
 */
inline fun <A> Option<A>.tap(f: (A) -> Unit): Option<A> = when (this) {
    is Option.Some -> f(value).let { this }
    is Option.None -> this
}

fun <A> Option<A>.toList(): List<A> = when (this) {
    is Option.Some -> listOf(value)
    is Option.None -> emptyList()
}

fun <A> Option<A>.toSet(): Set<A> = when (this) {
    is Option.Some -> setOf(value)
    is Option.None -> emptySet()
}

fun <A> Option<A>.toFiniteSet(): FiniteSet<A> = when (this) {
    is Option.Some -> FiniteSet.unordered(value)
    is Option.None -> FiniteSet.emptyUnordered()
}

fun <A> Option<A>.toFiniteSetUnordered(): FiniteSet.Unordered<A> = when (this) {
    is Option.Some -> FiniteSet.unordered(value)
    is Option.None -> FiniteSet.emptyUnordered()
}

fun <A> Option<A>.toFiniteSetOrdered(): FiniteSet.Ordered<A> = when (this) {
    is Option.Some -> FiniteSet.ordered(value)
    is Option.None -> FiniteSet.emptyOrdered()
}

inline fun <A, L> Option<A>.toEither(onNone: () -> L): Either<L, A> = when (this) {
    is Option.Some -> Either.Right(value)
    is Option.None -> Either.Left(onNone())
}

inline fun <A, L> Option<A>.toIor(ifNone: () -> L): Ior<L, A> = when (this) {
    is Option.Some -> Ior.Right(value)
    is Option.None -> Ior.Left(ifNone())
}

inline fun <A, L, R> Option<A>.toIor(ifNone: () -> L, ifSome: (A) -> R): Ior<L, R> = when (this) {
    is Option.Some -> Ior.Right(ifSome(value))
    is Option.None -> Ior.Left(ifNone())
}

object Options {
    /**
     * Puts an `A` inside an [Option] via [Option].
     */
    fun <A> just(a: A): Option<A> =
        OptionApplicative.just(a).fix()

    /**
     * Convenience function to
     */
    fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
//        a.flatMap { a -> b.map { b -> f(a, b) } }
//        f.liftOption()(a, b)
        OptionApplicative.map2(a, b, f).fix()

//    fun <A, B, C, D> map3(a: Option<A>, b: Option<B>, c: Option<C>, f: (A, B, C) -> D): Option<D> =
//        a.flatMap { a -> b.flatMap { b -> c.map { c -> f(a, b, c) } } }
//        f.liftOption().invoke(a, b, c)
    fun <A, B, C, D> map3(a: Option<A>, b: Option<B>, c: Option<C>, f: (A, B, C) -> D): Option<D> =
        OptionApplicative.map3(a, b, c, f).fix()

    fun <A, B> ((A) -> B).liftOption(): (Option<A>) -> Option<B> =
//        { it.map(this) }
        { oa -> OptionApplicative.map(oa, this).fix() }

    fun <A, B, C> ((A, B) -> C).liftOption(): (Option<A>, Option<B>) -> Option<C> =
//        { ea, eb -> ea.flatMap { a -> eb.map { b -> this(a, b) } } }
        {oa, ob -> map2(oa, ob, this) }

    fun <A, B, C, D> ((A, B, C) -> D).liftOption(): (Option<A>, Option<B>, Option<C>) -> Option<D> =
//        { ea, eb, ec -> ea.flatMap { a -> eb.flatMap { b -> ec.map { c -> this(a, b, c) } } } }
        { oa, ob, oc -> map3(oa, ob, oc, this) }


    fun <A> sequence(xs: Collection<Option<A>>): Option<List<A>> {
        tailrec fun aux(iter: Iterator<Option<A>> = xs.iterator(),
                        acc: MutableList<A> = mutableListOf()): Option<List<A>> =
            if (!iter.hasNext()) Option.Some(acc.toList())
            else when (val result = iter.next()) {
                is Option.Some -> {
                    acc.add(result.value)
                    aux(iter, acc)
                }
                is Option.None -> Option.None
            }
        return aux()
    }

    fun <A, B> traverse(xs: Collection<A>, f: (A) -> Option<B>): Option<List<B>> {
        tailrec fun aux(iter: Iterator<A> = xs.iterator(),
                        opts: MutableList<B> = mutableListOf()): Option<List<B>> {
            if (!iter.hasNext()) return Option.Some(opts.toList())
            val head = iter.next()
            return when (val result = f(head)) {
                is Option.None -> Option.None
                is Option.Some -> {
                    opts.add(result.value)
                    aux(iter, opts)
                }
            }
        }
        return aux()
    }

    /**
     * Catches exceptions and wraps the result in an [Option].
     *
     * Only catches [Exception] and its subclasses.
     * Fatal errors ([OutOfMemoryError], [StackOverflowError], etc.) are allowed to
     * propagate to ensure proper failure handling.
     *
     * To catch all possible [Throwable]s, use [catchesAll] instead.
     */
    inline fun <A> catches(a: () -> A): Option<A> =
        try {
            Option.Some(a())
        } catch (_: Exception) {
            Option.None
        }

    /**
     * Catches all throwables including [Error]s.
     *
     * WARNING: Use only in specific scenarios like testing or
     * when you genuinely need to handle fatal errors.
     *
     * In normal application code, prefer [catches], which excludes fatal errors.
     */
    inline fun <A> catchesAll(a: () -> A): Option<A> =
        try {
            Option.Some(a())
        } catch (_: Throwable) {
            Option.None
        }
}

object OptionOptics {
    fun <A> some(): Prism<Option<A>, A> = Prism(
        getterOrNull = { it.getOrNull() },
        reverseGetter = { Option.Some(it) },
        identityT = Identity()
    )
}
