package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.core.Symbols

/**
 * A unary operator that takes a value of a type `A` and transforms it to another type `B`.
 * This is typealiased to a [Mapper] since it can be used in map operations and an [Endo] so
 * it can map an element from a type to another element from the same type.
 */
data class UnaryOp<A, B>(
    override val symbol: String = DEFAULT_SYMBOL,
    val transform: (A) -> B
): Op {
    operator fun invoke(a: A): B =
        transform(a)

    companion object {
        const val DEFAULT_SYMBOL = Symbols.NOTHING
    }
}

/**
 * A [Mapper] is a mapping operation that can be used in maps, functors, etc.
 * It is a typealias of a [UnaryOp].
 */
typealias Mapper<A, B> = UnaryOp<A, B>

/**
 * An Endo is a UnaryOp from a type to itself.
 */
typealias Endo<A> = UnaryOp<A, A>
