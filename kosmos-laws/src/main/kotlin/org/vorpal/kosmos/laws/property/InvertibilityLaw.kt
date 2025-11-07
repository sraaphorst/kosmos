package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Typealias so that we can refer to this law as both an InvertibilityLaw
 * and an InverseLaw. */
typealias InverseLaw<A> = InvertibilityLaw<A>

/** Invertibility Law: check, for a given operation and identity element, that there is a function
 * on the type that generates inverses of elements that combine to form the identity.
 * This can be either:
 * * A total invertibility (all elements must have an inverse)
 * * A partial invertibility (some elements may not have an inverse)
 * There are constructors to create either type for a given operation.
 */
class InvertibilityLaw<A: Any> private constructor(
    private val op: BinOp<A>,
    private val identity: A,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val inverseOrNull: (A) -> A?,
    private val modeLabel: String,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {

    /** Total inverse constructor (e.g. groups).
     * All elements must be invertible. */
    constructor(
        op: BinOp<A>,
        identity: A,
        arbAll: Arb<A>,
        eq: Eq<A>,
        inverse: (A) -> A,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(
        op = op,
        identity = identity,
        arb = arbAll,
        eq = eq,
        inverseOrNull = { a -> inverse(a) },
        pr = pr,
        symbol = symbol,
        modeLabel = "total")

    /** Partial inverse constructor (e.g. fields: exclude 0).
     * To be used when there is a generator that produces any element of the given type.
     * The provided inverseOrNull function permits null (no inverse) to be returned
     * for anything that is considered a unit. */
    constructor(
        op: BinOp<A>,
        identity: A,
        arbAll: Arb<A>,
        eq: Eq<A>,
        inverseOrNull: (A) -> A?,
        isUnit: (A) -> Boolean,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(
        op = op,
        identity = identity,
        arb = arbAll.filter(isUnit),
        eq = eq,
        inverseOrNull = inverseOrNull,
        pr = pr,
        symbol = symbol,
        modeLabel = "partial"
    )

    /** Partial inverse constructor.
     * To be used when there is a generator that specifically only produces units, i.e.
     * invertible elements. It is a failure if the generator produces an element that
     * inverseOrNull indicates does not have an inverse (returns null). */
    constructor(
        op: BinOp<A>,
        identity: A,
        arbUnits: Arb<A>,
        eq: Eq<A>,
        inverseOrNull: (A) -> A?,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(
        op = op,
        identity = identity,
        arb = arbUnits,
        eq = eq,
        inverseOrNull = inverseOrNull,
        pr = pr,
        symbol = symbol,
        modeLabel = "partial"
    )

    /** Partial inverse constructor.
     * To be used when there is a total inverse, but we want to limit the elements
     * tested for invertibility (via the isUnit function). */
    constructor(
        op: BinOp<A>,
        identity: A,
        arbAll: Arb<A>,
        eq: Eq<A>,
        inverse: (A) -> A,
        isUnit: (A) -> Boolean,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(
        op = op,
        identity = identity,
        arb = arbAll.filter(isUnit),
        eq = eq,
        inverseOrNull = { a -> if (isUnit(a)) inverse(a) else null },
        pr = pr,
        symbol = symbol,
        modeLabel = "partial"
    )

    override val name: String = "$modeLabel invertibility ($symbol)"

    override suspend fun test() {
        checkAll(arb) { a ->
            val inv = inverseOrNull(a)
                ?: error("No inverse for ${pr.render(a)} (generator produced a non-invertible value)")

            val left = op(inv, a)
            val right  = op(a, inv)

            withClue(leftFailureMessage(a, inv, left)) {
                check(eq.eqv(left, identity))
            }
            withClue(rightFailureMessage(a, inv, right)) {
                check(eq.eqv(right, identity))
            }
        }
    }

    private fun infix(l: String, r: String) = "$l $symbol $r"

    private fun leftFailureMessage(
        a: A, aInv: A, left: A
    ): () -> String = {
        val sa = pr.render(a)
        val saInv = pr.render(aInv)
        val sLeft = pr.render(left)
        val sId = pr.render(identity)

        buildString {
            appendLine("Left invertibility failed:")

            append(infix("inv($sa)", sa))
            append(" = ")
            append(infix(saInv, sa))
            append(" = ")
            append(sLeft)
            append(" (expected: $sId)")
            appendLine()
        }
    }

    private fun rightFailureMessage(
        a: A, aInv: A, right: A
    ): () -> String = {
        val sa = pr.render(a)
        val saInv = pr.render(aInv)
        val sRight = pr.render(right)
        val sId = pr.render(identity)

        buildString {
            appendLine("Right invertibility failed:")

            append(infix(sa, "inv($sa)"))
            append(" = ")
            append(infix(sa, saInv))
            append(" = ")
            append(sRight)
            append(" (expected: $sId)")
            appendLine()
        }
    }
}
