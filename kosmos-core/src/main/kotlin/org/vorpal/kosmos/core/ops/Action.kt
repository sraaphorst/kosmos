package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.core.Symbols

/**
 * The action of one type on another.
 * As an example, we can use it in the definition of a vector space:
 * We can have S = Q and V = Q^n for some n.
 * Then apply would perform scalar multiplication using elements of Q on elements of Q^n.
 * V is acted on S on the left.
 */
data class Action<S, V>(
    val symbol: String = DEFAULT_SYMBOL,
    val apply: (S, V) -> V,
) {
    operator fun invoke(s: S, v: V): V =
        apply(s, v)

    companion object {
        private const val DEFAULT_SYMBOL = Symbols.DOT
        private const val DEFAULT_SYMBOL_RIGHT = Symbols.TRIANGLE_RIGHT
        private const val DEFAULT_SYMBOL_LEFT = Symbols.TRIANGLE_LEFT

        fun <S, V> leftAction(action: (S, V) -> V) =
            Action(DEFAULT_SYMBOL_RIGHT, action)
        fun <S, V> rightAction(action: (S, V) -> V): Action<S, V> =
            Action(DEFAULT_SYMBOL_LEFT, action)
    }
}
