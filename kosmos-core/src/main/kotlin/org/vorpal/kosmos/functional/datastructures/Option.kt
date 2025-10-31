package org.vorpal.kosmos.functional.datastructures

sealed class Option<out A>

data class Some<A>(val value: A) : Option<A>()
object None : Option<Nothing>()

fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
    is Some -> Some(f(value))
    is None -> None
}

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
    is Some -> f(value)
    is None -> None
}

fun <A> Option<Option<A>>.flatten(): Option<A> = when (this) {
    is Some -> value
    is None -> None
}

fun <A> Option<A>.getOrElse(default: () -> A): A = when (this) {
    is Some -> value
    is None -> default()
}

fun <A> Option<A>.orElse(other: Option<A>): Option<A> = when (this) {
    is Some -> this
    is None -> other
}

fun <A> Option<A>.isPresent(): Boolean = when (this) {
    is Some -> true
    is None -> false
}

fun <A> Option<A>.filter(predicate: (A) -> Boolean): Option<A> = when (this) {
    is Some -> if (predicate(value)) this else None
    is None -> None
}

object Options {
    fun <A, B> lift(f: (A) -> B): (Option<A>) -> Option<B> =
        { it.map(f) }

    fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
        a.flatMap { a -> b.map { b -> f(a, b) } }

    fun <A> sequence(xs: List<Option<A>>): Option<List<A>> {
        tailrec fun aux(iter: Iterator<Option<A>> = xs.iterator(),
                        acc: MutableList<A> = mutableListOf()): Option<List<A>> =
            if (!iter.hasNext()) Some(acc)
            else when (val result = iter.next()) {
                is Some -> {
                    acc.add(result.value)
                    aux(iter, acc)
                }
                is None -> None
            }
        return aux()
    }

    fun <A, B> traverse(xs: List<A>, f: (A) -> Option<B>): Option<List<B>> {
        tailrec fun aux(iter: Iterator<A> = xs.iterator(),
                        acc: MutableList<B> = mutableListOf()): Option<List<B>> =
            if (!iter.hasNext()) Some(acc)
            else when (val result = f(iter.next())) {
                is Some -> {
                    acc.add(result.value)
                    aux(iter, acc)
                }
                is None -> None
            }
        return aux()
    }

    fun <A> catches(a: () -> A): Option<A> =
        try {
            Some(a())
        } catch (e: Throwable) {
            None
        }
}
