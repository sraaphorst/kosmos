package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.core.Symbols

/**
 * A ternary operator `[A, B, C] -> D`.
 *
 * Typically, this will use the same type for all inputs and output.
 */
interface TernaryOp<A : Any, B : Any, C : Any, D : Any>: Op {
    fun apply(a: A, b: B, c: C): D
    operator fun invoke(a: A, b: B, c: C): D = apply(a, b, c)
}

interface TernOp<A : Any>: TernaryOp<A, A, A, A>

/* =========================
 *  Factory functions
 * ========================= */
fun <A : Any, B : Any, C : Any, D : Any> TernaryOp(
    symbol: String,
    apply: (A, B, C) -> D
): TernaryOp<A, B, C, D> = object : TernaryOp<A, B, C, D> {
    override val symbol: String = symbol
    override fun apply(a: A, b: B, c: C): D = apply(a, b, c)
}

fun <A : Any> TernOp(
    symbol: String,
    apply: (A, A, A) -> A
): TernOp<A> = object : TernOp<A> {
    override val symbol: String = symbol
    override fun apply(a: A, b: A, c: A): A = apply(a, b, c)
}

fun <A : Any> TernOp(apply: (A, A, A) -> A): TernOp<A> =
    TernOp(Symbols.DOT, apply)

fun <L : Any, R : Any> pairTernOp(
    leftOp: TernOp<L>,
    rightOp: TernOp<R>
): TernOp<Pair<L, R>> =
    TernOp("${leftOp.symbol}${Symbols.TIMES}${rightOp.symbol}") { x, y, z ->
        Pair(
            leftOp(x.first, y.first, z.first),
            rightOp(x.second, y.second, z.second)
        )
    }
