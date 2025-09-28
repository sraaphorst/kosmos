package org.vorpal.kosmos.core.ops

/**
 * The action of one type on another.
 * As an example, we can use it in the definition of a vector space:
 * We can have S = Q and V = Q^n for some n.
 * Then apply would perform scalar multiplication using elements of Q on elements of Q^n.
 * V is acted on S on the left.
 */
data class Action<S, V>(
    val apply: (S, V) -> V,
    val symbol: String = "·"
) {
    companion object {
        fun <S, V> leftAction(action: (S, V) -> V) = Action(action, "⊳")
        fun <S, V> rightAction(action: (S, V) -> V): Action<S, V> = Action(action, "⊲")
    }
}

// Example of creating a left action.
// val a = Action.leftAction<Int, Int>({ a, b -> a + b })