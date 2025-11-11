package org.vorpal.kosmos.functional.datastructures

import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.functional.optics.Prism

sealed class Option<out A> {
    data class Some<out A>(val value: A) : Option<A>()
    object None : Option<Nothing>()

    companion object {
        fun <A> of(value: A?) = value?.let { Some(it) } ?: None
    }
}

fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
    is Option.Some -> Option.Some(f(value))
    is Option.None -> Option.None
}

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
    is Option.Some -> f(value)
    is Option.None -> Option.None
}

fun <A> Option<Option<A>>.flatten(): Option<A> = when (this) {
    is Option.Some -> value
    is Option.None -> Option.None
}

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

fun <A> Option<A>.isPresent(): Boolean = when (this) {
    is Option.Some -> true
    is Option.None -> false
}

fun <A> Option<A>.filter(predicate: (A) -> Boolean): Option<A> = when (this) {
    is Option.Some -> if (predicate(value)) this else Option.None
    is Option.None -> this
}

fun <L, A> Option<A>.toEither(onNone: () -> L): Either<L, A> = when (this) {
    is Option.None -> Either.Left(onNone())
    is Option.Some -> Either.Right(value)
}

fun <A, B> ((A) -> B).liftOption(): (Option<A>) -> Option<B> =
    { it.map(this) }

fun <A, B, C> ((A, B) -> C).liftOption(): (Option<A>, Option<B>) -> Option<C> =
    { ea, eb -> ea.flatMap { a -> eb.map { b -> this(a, b) } } }

fun <A, B, C, D> ((A, B, C) -> D).liftOption(): (Option<A>, Option<B>, Option<C>) -> Option<D> =
    { ea, eb, ec -> ea.flatMap { a -> eb.flatMap { b -> ec.map { c -> this(a, b, c) } } } }

object Options {
    fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
        a.flatMap { a -> b.map { b -> f(a, b) } }

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

    fun <A> catches(a: () -> A): Option<A> =
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

    fun none(): Prism<Option<Nothing>, Nothing> = Prism(
        getterOrNull = { null },
        reverseGetter = { Option.None },
        identityT = Identity()
    )
}
