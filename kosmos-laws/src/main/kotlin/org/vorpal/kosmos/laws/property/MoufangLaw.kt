package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Shared helpers for Moufang variants. */
private sealed interface MoufangCore<A: Any> {
    val op: BinOp<A>
    val tripleArb: Arb<Triple<A, A, A>>
    val eq: Eq<A>
    val pr: Printable<A>
    val symbol: String

    fun failMsg(
        which: String,
        x: A, y: A, z: A,
        left: A, right: A
    ): () -> String = {
        val sx = pr.render(x)
        val sy = pr.render(y)
        val sz = pr.render(z)
        val sl = pr.render(left)
        val sr = pr.render(right)
        buildString {
            appendLine("Moufang ($which) failed:")
            appendLine("x = $sx, y = $sy, z = $sz")
            appendLine("LHS = $sl")
            appendLine("RHS = $sr")
            appendLine("Expected: LHS = RHS")
        }
    }
}

/** Left Moufang:  x ⋆ (y ⋆ (x ⋆ z)) = ((x ⋆ y) ⋆ x) ⋆ z */
class LeftMoufangLaw<A: Any>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, MoufangCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "Moufang (left: $symbol)"

    override suspend fun test() {
        checkAll(tripleArb) { (x, y, z) ->
            val xz  = op(x, z)
            val lhs = op(x, op(y, xz))
            val xy  = op(x, y)
            val rhs = op(op(xy, x), z)

            withClue(failMsg("left", x, y, z, lhs, rhs)) {
                check(eq.eqv(lhs, rhs))
            }
        }
    }
}

/** Right Moufang:  ((z ⋆ x) ⋆ y) ⋆ x = z ⋆ (x ⋆ (y ⋆ x)) */
class RightMoufangLaw<A: Any>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, MoufangCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "Moufang (right: $symbol)"

    override suspend fun test() {
        checkAll(tripleArb) { (x, y, z) ->
            val lhs = op(op(op(z, x), y), x)
            val rhs = op(z, op(x, op(y, x)))
            withClue(failMsg("right", x, y, z, lhs, rhs)) {
                check(eq.eqv(lhs, rhs))
            }
        }
    }
}

/** Middle Moufang:  (x ⋆ y) ⋆ (z ⋆ x) = x ⋆ (y ⋆ (z ⋆ x)) */
class MiddleMoufangLaw<A: Any>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, MoufangCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "Moufang (middle: $symbol)"

    override suspend fun test() {
        checkAll(tripleArb) { (x, y, z) ->
            val zx = op(z, x)
            val lhs = op(op(x, y), zx)
            val rhs = op(x, op(y, zx))

            withClue(failMsg("middle", x, y, z, lhs, rhs)) {
                check(eq.eqv(lhs, rhs))
            }
        }
    }
}

/**
 * The Moufang laws characterize Moufang loops and hold in alternative algebras (e.g. the octonions).
 * They are stronger than flexibility and imply alternativity.
 * A Moufang loop is similar to a group, but need not be associative.
 * An associative Moufang loop is a group.
 *
 * Note that Moufang loops have an associated algebra, the Malcev algebra.
 */
class MoufangLaw<A: Any>(
    op: BinOp<A>,
    arb: Arb<A>,
    eq: Eq<A>,
    pr: Printable<A> = Printable.default(),
    symbol: String = "⋆"
) : TestingLaw {
    private val left   = LeftMoufangLaw(op, arb, eq, pr, symbol)
    private val right  = RightMoufangLaw(op, arb, eq, pr, symbol)
    private val middle = MiddleMoufangLaw(op, arb, eq, pr, symbol)
    override val name = "Moufang (all: $symbol)"
    override suspend fun test() {
        left.test()
        right.test()
        middle.test()
    }
}