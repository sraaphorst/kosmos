package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filterNot
import kotlinx.coroutines.CancellationException
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.LawViolation
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

/**
 * Shared inputs of the invertibility laws.
 *
 * The domain is every generated element except [exclude] (compared with [eq]): for a group or the additive group of
 * a ring nothing is excluded; for the multiplicative part of a division ring, [exclude] is zero. The laws build the
 * restricted generator themselves, so their binding is the original generator plus [exclude], and two suites that
 * restrict the same generator in the same way share their checks.
 */
private sealed interface InvertibilityCore<A : Any> {
    val op: BinOp<A>
    val identity: A
    val inverse: UnaryOp<A, out A?>
    val arb: Arb<A>
    val exclude: A?
    val eq: Eq<A>
    val pr: Printable<A>

    fun domain(): Arb<A> =
        exclude?.let { z -> arb.filterNot { eq(it, z) } } ?: arb

    /** The inverse of [a], or null if it is missing (the inverse returned null or threw). */
    fun inverseOrNull(a: A): A? =
        try {
            inverse(a)
        } catch (t: CancellationException) {
            throw t
        } catch (_: Throwable) {
            null
        }

    fun expr(left: String, right: String) = "$left ${op.symbol} $right"
}

/**
 * Existence of inverses: every element of the domain has an inverse.
 *
 * Fails when [inverse] returns null or throws for an element of the domain. This is what makes invertibility a
 * requirement: a field whose reciprocal is missing for some nonzero element fails here.
 */
class InverseExistenceLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val inverse: UnaryOp<A, out A?>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
    override val exclude: A? = null
) : TestingLaw, InvertibilityCore<A> {
    override val binding = LawBinding.of(this, op, identity, arb, inverse, eq, exclude)

    override val name: String = "inverse exists (${inverse.symbol})"

    override suspend fun test() {
        lawCheckAll(domain(), render = renderWith(pr)) { a ->
            val result = try {
                inverse(a)
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                throw LawViolation("No inverse: ${inverse.symbol}(${pr(a)}) threw ${t::class.simpleName}: ${t.message}", t)
            }
            lawCheck(result != null) { "No inverse: ${inverse.symbol}(${pr(a)}) is undefined." }
        }
    }
}

/**
 * Left invertibility: for every `a` in the domain with inverse `b`, `ba = e`.
 *
 * Elements without an inverse are left to [InverseExistenceLaw].
 */
class LeftInvertibilityLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val inverse: UnaryOp<A, out A?>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
    override val exclude: A? = null
) : TestingLaw, InvertibilityCore<A> {
    override val binding = LawBinding.of(this, op, identity, arb, inverse, eq, exclude)

    override val name: String = "left invertibility (${op.symbol})"

    override suspend fun test() {
        lawCheckAll(domain(), render = renderWith(pr)) { a ->
            val b = inverseOrNull(a) ?: return@lawCheckAll
            val result = op(b, a)
            lawCheck(eq(result, identity)) {
                "Left invertibility failed: ${expr("${inverse.symbol}(${pr(a)})", pr(a))} = " +
                    "${expr(pr(b), pr(a))} = ${pr(result)} (expected: ${pr(identity)})"
            }
        }
    }
}

/**
 * Right invertibility: for every `a` in the domain with inverse `b`, `ab = e`.
 *
 * Elements without an inverse are left to [InverseExistenceLaw].
 */
class RightInvertibilityLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val inverse: UnaryOp<A, out A?>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
    override val exclude: A? = null
) : TestingLaw, InvertibilityCore<A> {
    override val binding = LawBinding.of(this, op, identity, arb, inverse, eq, exclude)

    override val name: String = "right invertibility (${op.symbol})"

    override suspend fun test() {
        lawCheckAll(domain(), render = renderWith(pr)) { a ->
            val b = inverseOrNull(a) ?: return@lawCheckAll
            val result = op(a, b)
            lawCheck(eq(result, identity)) {
                "Right invertibility failed: ${expr(pr(a), "${inverse.symbol}(${pr(a)})")} = " +
                    "${expr(pr(a), pr(b))} = ${pr(result)} (expected: ${pr(identity)})"
            }
        }
    }
}

/**
 * Invertibility: every element of the domain (every generated element except [exclude]) has a two-sided inverse.
 *
 * A branch of [InverseExistenceLaw], [LeftInvertibilityLaw] and [RightInvertibilityLaw].
 */
class InvertibilityLaw<A : Any>(
    private val op: BinOp<A>,
    private val identity: A,
    private val arb: Arb<A>,
    private val inverse: UnaryOp<A, out A?>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
    private val exclude: A? = null
) : LawSuite {
    override val binding = LawBinding.of(this, op, identity, arb, inverse, eq, exclude)

    override val name: String = "invertibility (${op.symbol})"

    override fun children(): List<LawNode> = listOf(
        InverseExistenceLaw(op, identity, arb, inverse, eq, pr, exclude),
        LeftInvertibilityLaw(op, identity, arb, inverse, eq, pr, exclude),
        RightInvertibilityLaw(op, identity, arb, inverse, eq, pr, exclude)
    ).asNodes()
}
