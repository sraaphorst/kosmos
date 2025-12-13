package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface MoufangCore<A : Any> {
    val op: BinOp<A>
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    fun failMsg(
        which: String,
        x: A, y: A, z: A,
        left: A, right: A
    ): () -> String = {
        val sx = pr(x)
        val sy = pr(y)
        val sz = pr(z)
        val sl = pr(left)
        val sr = pr(right)
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
class LeftMoufangLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, MoufangCore<A> {
    override val name = "Moufang (left: ${op.symbol})"

    override suspend fun test() {
        checkAll(TestingLaw.arbTriple(arb)) { (x, y, z) ->
            val xz  = op(x, z)
            val lhs = op(x, op(y, xz))
            val xy  = op(x, y)
            val rhs = op(op(xy, x), z)

            withClue(failMsg("left", x, y, z, lhs, rhs)) {
                check(eq(lhs, rhs))
            }
        }
    }
}

/**
 * Right Moufang:
 *
 *    ((z ⋆ x) ⋆ y) ⋆ x = z ⋆ (x ⋆ (y ⋆ x))
 */
class RightMoufangLaw<A: Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, MoufangCore<A> {
    override val name = "Moufang (right: ${op.symbol})"

    override suspend fun test() {
        checkAll(TestingLaw.arbTriple(arb)) { (x, y, z) ->
            val lhs = op(op(op(z, x), y), x)
            val rhs = op(z, op(x, op(y, x)))
            withClue(failMsg("right", x, y, z, lhs, rhs)) {
                check(eq(lhs, rhs))
            }
        }
    }
}

/**
 * Middle Moufang:
 *
 *     (x ⋆ y) ⋆ (z ⋆ x) = x ⋆ (y ⋆ (z ⋆ x))
 */
class MiddleMoufangLaw<A: Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, MoufangCore<A> {
    override val name = "Moufang (middle: ${op.symbol})"

    override suspend fun test() {
        checkAll(TestingLaw.arbTriple(arb)) { (x, y, z) ->
            val zx = op(z, x)
            val lhs = op(op(x, y), zx)
            val rhs = op(x, op(y, zx))

            withClue(failMsg("middle", x, y, z, lhs, rhs)) {
                check(eq(lhs, rhs))
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
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
) : TestingLaw {
    private val left   = LeftMoufangLaw(op, arb, eq, pr)
    private val right  = RightMoufangLaw(op, arb, eq, pr)
    private val middle = MiddleMoufangLaw(op, arb, eq, pr)
    override val name = "Moufang (all: ${op.symbol})"
    override suspend fun test() {
        left.test()
        right.test()
        middle.test()
    }
}
