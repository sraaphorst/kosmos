package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

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
    override val binding = LawBinding.of(this, op, arb, eq)

    override val name = "moufang (left: ${op.symbol})"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (x, y, z) ->
            val xz  = op(x, z)
            val lhs = op(x, op(y, xz))
            val xy  = op(x, y)
            val rhs = op(op(xy, x), z)

            lawCheck(eq(lhs, rhs), failMsg("left", x, y, z, lhs, rhs))
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
    override val binding = LawBinding.of(this, op, arb, eq)

    override val name = "moufang (right: ${op.symbol})"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (x, y, z) ->
            val lhs = op(op(op(z, x), y), x)
            val rhs = op(z, op(x, op(y, x)))
            lawCheck(eq(lhs, rhs), failMsg("right", x, y, z, lhs, rhs))
        }
    }
}

/**
 * Middle Moufang:
 *
 *     (x ⋆ y) ⋆ (z ⋆ x) = x ⋆ ((y ⋆ z) ⋆ x)
 *
 * The right-hand side is the flexible expression `x ⋆ (y ⋆ z) ⋆ x`; since alternative algebras are
 * flexible, `x ⋆ ((y ⋆ z) ⋆ x) = (x ⋆ (y ⋆ z)) ⋆ x`, so the parenthesization chosen here is immaterial.
 */
class MiddleMoufangLaw<A: Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, MoufangCore<A> {
    override val binding = LawBinding.of(this, op, arb, eq)

    override val name = "Moufang (middle: ${op.symbol})"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (x, y, z) ->
            val zx = op(z, x)
            val yz = op(y, z)
            val lhs = op(op(x, y), zx)
            val rhs = op(x, op(yz, x))

            lawCheck(eq(lhs, rhs), failMsg("middle", x, y, z, lhs, rhs))
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
    private val op: BinOp<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : LawSuite {
    override val binding = LawBinding.of(this, op, arb, eq)

    override val name = "moufang (${op.symbol})"

    override fun children(): List<LawNode> = listOf(
        LeftMoufangLaw(op, arb, eq, pr),
        RightMoufangLaw(op, arb, eq, pr),
        MiddleMoufangLaw(op, arb, eq, pr)
    ).asNodes()
}
