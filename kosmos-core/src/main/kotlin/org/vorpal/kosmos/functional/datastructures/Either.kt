package org.vorpal.kosmos.functional.datastructures

import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.functional.optics.Prism

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    companion object {
        fun <L> left(value: L): Either<L, Nothing> = Left(value)
        fun <R> right(value: R): Either<Nothing, R> = Right(value)
    }
}

fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> = when (this) {
    is Either.Left -> this
    is Either.Right -> Either.Right(f(value))
}

fun <E, F, A> Either<E, A>.mapLeft(f: (E) -> F): Either<F, A> = when (this) {
    is Either.Left -> Either.Left(f(value))
    is Either.Right -> this
}

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> = when (this) {
    is Either.Left -> this
    is Either.Right -> f(value)
}

fun <E, F, A> Either<E, A>.flatMapLeft(f: (E) -> Either<F, A>): Either<F, A> = when (this) {
    is Either.Left -> f(value)
    is Either.Right -> this
}

fun <E, A, F, B> Either<E, A>.bimap(f: (E) -> F, g:  (A) -> B): Either<F, B> = when (this) {
    is Either.Left -> Either.Left(f(value))
    is Either.Right -> Either.Right(g(value))
}

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> = when (this) {
    is Either.Left -> f()
    is Either.Right -> this
}

fun <R> Either<*, R>.getOrElse(default: () -> R): R = when (this) {
    is Either.Left -> default()
    is Either.Right -> value
}

fun <R> Either<*, R>.getOrNull(): R? = when (this) {
    is Either.Left -> null
    is Either.Right -> value
}

fun <L> Either<L, *>.getLeftOrNull(): L? = when (this) {
    is Either.Left -> value
    is Either.Right -> null
}

fun <R> Either<Throwable, R>.getOrThrow(): R = when (this) {
    is Either.Left -> throw value
    is Either.Right -> value
}

inline fun <L, R, T> Either<L, R>.fold(ifLeft: (L) -> T, ifRight: (R) -> T): T = when (this) {
    is Either.Left -> ifLeft(value)
    is Either.Right -> ifRight(value)
}

fun <L, R> Either<L, R>.filterOrElse(
    predicate: (R) -> Boolean,
    default: () -> L
): Either<L, R> = when (this) {
    is Either.Left -> this
    is Either.Right -> if (predicate(value)) this else Either.Left(default())
}

/**
 * Recover from Left with a function.
 */
fun <L, R> Either<L, R>.recover(f: (L) -> R): Either<L, R> = when (this) {
    is Either.Left -> Either.Right(f(value))
    is Either.Right -> this
}

/**
 * Recover from Left with another Either.
 */
fun <L1, L2, R> Either<L1, R>.recoverWith(f: (L1) -> Either<L2, R>): Either<L2, R> = when (this) {
    is Either.Left -> f(value)
    is Either.Right -> this
}

fun <A> Either<A, A>.collapse(): A = when (this) {
    is Either.Left -> value
    is Either.Right -> value
}

inline fun <L, R> Either<L, R>.onRight(f: (R) -> Unit): Either<L, R> {
    if (this is Either.Right) f(value)
    return this
}

inline fun <L, R> Either<L, R>.onLeft(f: (L) -> Unit): Either<L, R> {
    if (this is Either.Left) f(value)
    return this
}

/**
 * Tap: for debugging / logging.
 */
inline fun <L, R> Either<L, R>.tap(f: (Either<L, R>) -> Unit): Either<L, R> {
    f(this)
    return this
}

fun <L, R> Either<L, R>.swap(): Either<R, L> = when (this) {
    is Either.Left -> Either.Right(value)
    is Either.Right -> Either.Left(value)
}

fun Either<*, *>.isLeft(): Boolean = this is Either.Left<*>
fun Either<*, *>.isRight(): Boolean = this is Either.Right<*>

/**
 * Discards any Left value.
 */
fun <A> Either<*, A>.toOption(): Option<A> = when (this) {
    is Either.Left -> Option.None
    is Either.Right -> Option.Some(value)
}

fun <E> Either<E, *>.toLeftOption(): Option<E> = when (this) {
    is Either.Left -> Option.Some(value)
    is Either.Right -> Option.None
}

/**
 * Apply a function wrapped in Either to a value wrapped in Either.
 */
fun <L, R, S> Either<L, R>.ap(f: Either<L, (R) -> S>): Either<L, S> = when (f) {
    is Either.Left -> f
    is Either.Right -> this.map(f.value)
}

/**
 * Zip two Eithers (both must be Right).
 */
fun <L, R1, R2> Either<L, R1>.zip(other: Either<L, R2>): Either<L, Pair<R1, R2>> =
    flatMap { r1 -> other.map { r2 -> r1 to r2 } }

/**
 * Zip with transformation function.
 */
fun <L, R1, R2, S> Either<L, R1>.zipWith(
    other: Either<L, R2>,
    f: (R1, R2) -> S
): Either<L, S> = flatMap { r1 -> other.map { r2 -> f(r1, r2) } }

fun <L, R> Either<L, Either<L, R>>.flatten(): Either<L, R> = flatMap { it }

/**
 * Ensure: assert a condition.
 */
fun <L, R> Either<L, R>.ensure(error: () -> L, predicate: (R) -> Boolean): Either<L, R> =
    flatMap { if (predicate(it)) Either.Right(it) else Either.Left(error()) }

fun <L, R> Either<L, R>.guard(error: () -> L, predicate: (R) -> Boolean): Either<L, R> =
    ensure(error, predicate)

fun <L, R> Either<L, R>.filter(error: () -> L, predicate: (R) -> Boolean): Either<L, R> =
    ensure(error, predicate)

fun <E, A, B> ((A) -> B).liftEither(): (Either<E, A>) -> Either<E, B> = { either ->
    either.map(this)
}

fun <E, A, B, C> ((A, B) -> C).liftEither(): (Either<E, A>, Either<E, B>) -> Either<E, C> =
    { ea, eb ->
        ea.flatMap { a -> eb.map { b -> this(a, b) } }
    }

fun <E, A, B, C, D> ((A, B, C) -> D).liftEither(): (Either<E, A>, Either<E, B>, Either<E, C>) -> Either<E, D> =
    { ea, eb, ec ->
        ea.flatMap { a -> eb.flatMap { b -> ec.map { c -> this(a, b, c) } } }
    }

object Eithers {
    fun <E, A, B, C> map2(ae: Either<E, A>, be: Either<E, B>, f: (A, B) -> C): Either<E, C> =
        ae.flatMap { a -> be.map { b -> f(a, b) } }

    /**
     * Traverse a List of Eithers.
     */
    fun <L, R> sequence(xs: Collection<Either<L, R>>): Either<L, List<R>> {
        tailrec fun aux(iter: Iterator<Either<L, R>> = xs.iterator(),
                        rights: MutableList<R> = mutableListOf()): Either<L, List<R>> {
            if (!iter.hasNext()) return Either.Right(rights.toList())
            val head = iter.next()
            return when (head) {
                is Either.Left -> Either.Left(head.value)
                is Either.Right -> {
                    rights.add(head.value)
                    aux(iter, rights)
                }
            }
        }
        return aux()
    }

    /**
     * Map and sequence in one step.
     */
    /**
     * Map and sequence in one step.
     * Short-circuits on the first Left encountered.
     */
    fun <A, L, R> traverse(xs: Collection<A>, f: (A) -> Either<L, R>): Either<L, List<R>> {
        tailrec fun aux(iter: Iterator<A> = xs.iterator(),
                        rights: MutableList<R> = mutableListOf()): Either<L, List<R>> {
            if (!iter.hasNext()) return Either.Right(rights.toList())
            val head = iter.next()
            return when (val result = f(head)) {
                is Either.Left -> Either.Left(result.value)
                is Either.Right -> {
                    rights.add(result.value)
                    aux(iter, rights)
                }
            }
        }
        return aux()
    }

    fun <A> catches(a: () -> A): Either<Throwable, A> =
        try {
            Either.Right(a())
        } catch (e: Throwable) {
            Either.Left(e)
        }

    fun <L, R> cond(test: Boolean, right: () -> R, left: () -> L): Either<L, R> =
        if (test) Either.Right(right()) else Either.Left(left())
}

object EitherOptics {
    fun <L> left(): Prism<Either<L, *>, L> = Prism(
        getterOrNull =  { it.getLeftOrNull() },
        reverseGetter = { Either.Left(it) },
        identityT = Identity()
    )

    fun <R> right(): Prism<Either<*, R>, R> = Prism(
        getterOrNull = { it.getOrNull() },
        reverseGetter = { Either.Right(it) },
        identityT = Identity()
    )
}
