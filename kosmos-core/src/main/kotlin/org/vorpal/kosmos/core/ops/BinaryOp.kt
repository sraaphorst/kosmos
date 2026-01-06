package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.core.Symbols

/* =========================
 *  Generic heterogeneous op
 * ========================= */

/** A heterogeneous binary operation (A, B) -> R with a printable symbol. */
interface BinaryOp<A : Any, B : Any, R : Any> : Op {
    operator fun invoke(a: A, b: B): R
}

/**
 * A left action is (R, M) -> M.
 */
interface LeftAction<R : Any, M : Any> : BinaryOp<R, M, M> {
    fun toRightAction(newSymbol: String? = null): RightAction<M, R> =
        RightAction(newSymbol ?: symbol) { m, r -> this(r, m)}

    fun toEndo(r: R): Endo<M> =
        Endo(symbol) { m -> this(r, m) }

    /** Post-map the result in `M`. */
    fun map(f: (M) -> M): LeftAction<R, M> =
        LeftAction(symbol) { r, m -> f(this(r, m)) }

    /** Pre-map the scalar in `R`. */
    fun contramap(g: (R) -> R): LeftAction<R, M> =
        LeftAction(symbol) { r, m -> this(g(r), m) }
}

/**
 * A right action is (M, S) -> M.
 */
interface RightAction<M : Any, S : Any> : BinaryOp<M, S, M> {
    fun toLeftAction(newSymbol: String? = null): LeftAction<S, M> =
        LeftAction(newSymbol ?: symbol) { s, m -> this(m, s)}

    fun toEndo(s: S): Endo<M> = Endo(symbol) { m -> this (m, s)}

    /** Post-map the result in `M`. */
    fun map(f: (M) -> M): RightAction<M, S> =
        RightAction(symbol) { m, s -> f(this(m, s)) }

    /** Pre-map the scalar in `S`. */
    fun contramap(g: (S) -> S): RightAction<M, S> =
        RightAction(symbol) { m, s -> this(m, g(s)) }
}

/**
 * A closed operation (A, A) -> A.
 *
 * IMPORTANT: this is also a LeftAction<A, A>, so you can pass a BinOp anywhere a LeftAction<A, A> is needed.
 */
interface BinOp<A : Any> : LeftAction<A, A>

/** Bilinear form: (V, V) -> F */
typealias BilinearForm<V, F> = BinaryOp<V, V, F>

/* =========================
 *  Factory functions
 * ========================= */

fun <A : Any, B : Any, R : Any> BinaryOp(
    symbol: String = Symbols.DOT,
    combine: (A, B) -> R
): BinaryOp<A, B, R> = object : BinaryOp<A, B, R> {
    override val symbol: String = symbol
    override fun invoke(a: A, b: B): R = combine(a, b)
}

fun <A : Any> BinOp(
    symbol: String,
    combine: (A, A) -> A
): BinOp<A> = object : BinOp<A> {
    override val symbol: String = symbol
    override fun invoke(a: A, b: A): A = combine(a, b)
}

fun <A : Any> BinOp(combine: (A, A) -> A): BinOp<A> =
    BinOp(Symbols.DOT, combine)

fun <R : Any, M : Any> LeftAction(
    symbol: String,
    apply: (R, M) -> M
): LeftAction<R, M> = object : LeftAction<R, M> {
    override val symbol: String = symbol
    override fun invoke(a: R, b: M): M = apply(a, b)
}
fun <R : Any, M : Any> LeftAction(apply: (R, M) -> M) = LeftAction(Symbols.TRIANGLE_RIGHT, apply)

fun <M : Any, S : Any> RightAction(
    symbol: String,
    apply: (M, S) -> M
): RightAction<M, S> = object : RightAction<M, S> {
    override val symbol: String = symbol
    override fun invoke(a: M, b: S): M = apply(a, b)
}
fun <M : Any, S : Any> RightAction(apply: (M, S) -> M) = RightAction(Symbols.TRIANGLE_LEFT, apply)

fun <V : Any, F : Any> BilinearForm(
    symbol: String,
    apply: (V, V) -> F
): BilinearForm<V, F> = object : BilinearForm<V, F> {
    override val symbol: String = symbol
    override fun invoke(a: V, b: V): F = apply(a, b)
}
fun <V : Any, F : Any> BilinearForm(apply: (V, V) -> F) = BilinearForm(Symbols.DOT, apply)
