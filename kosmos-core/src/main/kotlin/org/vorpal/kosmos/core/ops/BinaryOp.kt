package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.core.Symbols

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
 * Combine two [LeftAction]s from scalars [R] to types [M] and [N] to create a joint [LeftAction] on [Pair<M, N>].
 */
fun <R : Any, M : Any, N : Any> pairLeftAction(
    leftActionFirst: LeftAction<R, M>,
    leftActionSecond: LeftAction<R, N>
): LeftAction<R, Pair<M, N>> =
    LeftAction("${leftActionFirst.symbol}${Symbols.TIMES}${leftActionSecond.symbol}") { r, (m, n) ->
        Pair(
            leftActionFirst(r, m),
            leftActionSecond(r, n)
        )
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
 * Combine two [RightAction]s from scalars [S] to types [M] and [N] to create a joint [RightAction] on [Pair<M, N>].
 */
fun <M : Any, N : Any, S : Any> pairRightAction(
    rightActionFirst: RightAction<M, S>,
    rightActionSecond: RightAction<N, S>
): RightAction<Pair<M, N>, S> =
    RightAction("${rightActionFirst.symbol}${Symbols.TIMES}${rightActionSecond.symbol}") { (m, n), s ->
        Pair(
            rightActionFirst(m, s),
            rightActionSecond(n, s)
        )
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

/**
 * Combine two [BinOp] on types [L] and [R] to create a joint [BinOp] on [Pair<L, R>].
 */
fun <L : Any, R : Any> pairOp(
    leftOp: BinOp<L>,
    rightOp: BinOp<R>
): BinOp<Pair<L, R>> =
    BinOp("${leftOp.symbol}${Symbols.TIMES}${rightOp.symbol}") { x, y ->
        Pair(
            leftOp(x.first, y.first),
            rightOp(x.second, y.second)
        )
    }

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

fun <V : Any, F : Any> BilinearForm(
    apply: (V, V) -> F
) = BilinearForm(Symbols.DOT, apply)
