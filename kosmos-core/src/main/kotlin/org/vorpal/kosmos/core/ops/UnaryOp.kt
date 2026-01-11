package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.core.Symbols

/**
 * A unary operator A -> B with a printable symbol.
 * We want B to be able to be nullable.
 */
interface UnaryOp<A, B> : Op {
    operator fun invoke(a: A): B

    /**
     * Compose on the right: (A -> B) andThen (B -> C) = (A -> C).
     *
     * Member function so callers don't need to import an extension.
     */
    infix fun <C> andThen(other: UnaryOp<B, C>): UnaryOp<A, C> =
        UnaryOp("${this.symbol}∘${other.symbol}") { other(this(it)) }

    /**
     * Compose on the left: (A -> B) compose (C -> A) = (C -> B).
     */
    infix fun <C> compose(other: UnaryOp<C, A>): UnaryOp<C, B> =
        other andThen this
}

fun <A, B> UnaryOp(
    symbol: String,
    transform: (A) -> B
): UnaryOp<A, B> = object : UnaryOp<A, B> {
    override val symbol: String = symbol
    override fun invoke(a: A): B = transform(a)
}

fun <A, B> UnaryOp(transform: (A) -> B): UnaryOp<A, B> =
    UnaryOp(Symbols.NOTHING, transform)

/** A Mapper is just a UnaryOp. */
typealias Mapper<A, B> = UnaryOp<A, B>

fun <A, B> Mapper(symbol: String, transform: (A) -> B): Mapper<A, B> =
    UnaryOp(symbol, transform)

fun <A, B> Mapper(transform: (A) -> B): Mapper<A, B> =
    UnaryOp(transform)

/** An Endo is a UnaryOp from a type to itself. */
typealias Endo<A> = UnaryOp<A, A>

fun <A> Endo(symbol: String, transform: (A) -> A): Endo<A> =
    UnaryOp(symbol, transform)

fun <A> Endo(transform: (A) -> A): Endo<A> =
    UnaryOp(transform)
