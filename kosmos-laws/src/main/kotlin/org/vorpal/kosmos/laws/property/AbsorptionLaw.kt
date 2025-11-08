package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Absorption laws:
 *  absorb absorbs over:  a ⊗ (a ⊕ b) = a
 *  over   absorbs absorb: a ⊕ (a ⊗ b) = a
 */
private sealed interface AbsorptionCore<A: Any> {
    val absorb: BinOp<A>   // e.g. meet ∧
    val over: BinOp<A>     // e.g. join ∨
    val pairArb: Arb<Pair<A, A>> // (a, b)
    val eq: Eq<A>
    val pr: Printable<A>
    val absorbSymbol: String
    val overSymbol: String

    private fun absorbInfix(l: String, r: String) = "$l $absorbSymbol $r"
    private fun overInfix(l: String, r: String)   = "$l $overSymbol $r"

    /** a absorb (a over b) = a */
    suspend fun absorbOverCheck() {
        checkAll(pairArb) { (a, b) ->
            val aOverB = over(a, b)
            val left   = absorb(a, aOverB)  // a ⊗ (a ⊕ b)
            withClue(absorbOverFailure(a, b, aOverB, left)) {
                check(eq.eqv(left, a))
            }
        }
    }

    /** a over (a absorb b) = a */
    suspend fun overAbsorbCheck() {
        checkAll(pairArb) { (a, b) ->
            val aAbsorbB = absorb(a, b)
            val left     = over(a, aAbsorbB) // a ⊕ (a ⊗ b)
            withClue(overAbsorbFailure(a, b, aAbsorbB, left)) {
                check(eq.eqv(left, a))
            }
        }
    }

    private fun absorbOverFailure(
        a: A, b: A, aOverB: A, left: A
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sAoverB = pr.render(aOverB)
        val sLeft = pr.render(left)
        buildString {
            appendLine("Absorption failed (absorb over):")
            append(absorbInfix(sa, "(" + overInfix(sa, sb) + ")"))
            append(" = ")
            append(absorbInfix(sa, sAoverB))
            append(" = ")
            append(sLeft)
            append(" (expected: $sa)")
            appendLine()
        }
    }

    private fun overAbsorbFailure(
        a: A, b: A, aAbsorbB: A, left: A
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sAabsorbB = pr.render(aAbsorbB)
        val sLeft = pr.render(left)
        buildString {
            appendLine("Absorption failed (over absorb):")
            append(overInfix(sa, "(" + absorbInfix(sa, sb) + ")"))
            append(" = ")
            append(overInfix(sa, sAabsorbB))
            append(" = ")
            append(sLeft)
            append(" (expected: $sa)")
            appendLine()
        }
    }
}

/** Only a ⊗ (a ⊕ b) = a */
class AbsorbOverLaw<A: Any>(
    override val absorb: BinOp<A>,
    override val over: BinOp<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val absorbSymbol: String = "∧",
    override val overSymbol: String = "∨",
) : TestingLaw, AbsorptionCore<A> {

    constructor(
        absorb: BinOp<A>,
        over: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        absorbSym: String = "∧",
        overSym: String = "∨",
    ) : this(absorb, over, Arb.pair(arb, arb), eq, pr, absorbSym, overSym)

    override val name = "absorption ($absorbSymbol over $overSymbol)"
    override suspend fun test() = absorbOverCheck()
}

/** Only a ⊕ (a ⊗ b) = a */
class OverAbsorbLaw<A: Any>(
    override val absorb: BinOp<A>,
    override val over: BinOp<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val absorbSymbol: String = "∧",
    override val overSymbol: String = "∨",
) : TestingLaw, AbsorptionCore<A> {

    constructor(
        absorb: BinOp<A>,
        over: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        absorbSymbol: String = "∧",
        overSymbol: String = "∨",
    ) : this(absorb, over, Arb.pair(arb, arb), eq, pr, absorbSymbol, overSymbol)

    override val name = "absorption ($overSymbol over $absorbSymbol)"
    override suspend fun test() = overAbsorbCheck()
}

/** Both absorption directions (lattice-style). */
class AbsorptionLaw<A: Any>(
    override val absorb: BinOp<A>,
    override val over: BinOp<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val absorbSymbol: String = "∧",
    override val overSymbol: String = "∨",
) : TestingLaw, AbsorptionCore<A> {

    constructor(
        absorb: BinOp<A>,
        over: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        absorbSymbol: String = "∧",
        overSymbol: String = "∨",
    ) : this(absorb, over, Arb.pair(arb, arb), eq, pr, absorbSymbol, overSymbol)

    override val name = "absorption (both: $absorbSymbol and $overSymbol)"
    override suspend fun test() {
        absorbOverCheck()
        overAbsorbCheck()
    }
}