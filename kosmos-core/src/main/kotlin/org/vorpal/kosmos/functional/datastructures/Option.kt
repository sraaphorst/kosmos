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
