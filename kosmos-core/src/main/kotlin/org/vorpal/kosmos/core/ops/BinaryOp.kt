package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.core.Symbols

/* =========================
 *  Generic heterogeneous op
 * ========================= */

/** A heterogeneous binary operation (A, B) -> R with a printable symbol. */
data class BinaryOp<A, B, R>(
    val symbol: String = Symbols.DOT,
    val combine: (A, B) -> R
) {
    constructor(combine: (A, B) -> R) : this(Symbols.DOT, combine)
    operator fun invoke(a: A, b: B): R = combine(a, b)
}

/** Homogeneous specialization: (A, A) -> A, i.e. a closed operation. */
typealias BinOp<A> = BinaryOp<A, A, A>

/* =====================
 *  Actions = BinaryOps
 * ===================== */

/**
 * An action is just (S, V) -> V.
 * Commonly, this is the action of a field on a vector space, a ring on a module, etc.
 */
typealias Action<S, V> = BinaryOp<S, V, V>

/* ----------------
 *  Action helpers
 * ---------------- */
object Actions {
    /** Use a conventional “left action” symbol (▷). */
    fun <S, V> leftAction(apply: (S, V) -> V): Action<S, V> =
        BinaryOp(Symbols.TRIANGLE_RIGHT, apply)

    /** Use a conventional “right action” symbol (◁). */
    fun <S, V> rightAction(apply: (S, V) -> V): Action<S, V> =
        BinaryOp(Symbols.TRIANGLE_LEFT, apply)

    /** Fix the scalar to get an endomorphism on V: s ↦ (v ↦ s • v). */
    fun <S, V> Action<S, V>.toEndo(s: S): Endo<V> =
        Endo(this.symbol) { v -> this(s, v) }

    /** Post-map the result in V. */
    fun <S, V> Action<S, V>.map(f: (V) -> V): Action<S, V> =
        BinaryOp(this.symbol) { s, v -> f(this(s, v)) }

    /** Pre-map the scalar in S. */
    fun <S, V> Action<S, V>.contramap(g: (S) -> S): Action<S, V> =
        BinaryOp(this.symbol) { s, v -> this(g(s), v) }
}