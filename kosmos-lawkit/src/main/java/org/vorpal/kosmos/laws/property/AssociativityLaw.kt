package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.TestingLaw
import org.vorpal.kosmos.core.ops.BinOp

/** Associativity Law
 * Note that we allow a Triple producing Arb so that we can impose constraints if necessary on the values produced,
 * e.g. that they all be distinct, or to avoid NaN / overflow for floating point types. */
class AssociativityLaw<A>(
    private val op: BinOp<A>,
    private val tripleArb: Arb<Triple<A, A, A>>,
    private val eq: Eq<A>
) : TestingLaw {

    /** Convenience secondary constructor that converts an Arb to an Arb producing a Triple */
    constructor(op: BinOp<A>, arb: Arb<A>, eq: Eq<A>)
            : this(op, Arb.triple(arb, arb, arb), eq)

    override val name = "associativity"

    override suspend fun test() {
        checkAll(tripleArb) { (a, b, c) ->
            val left  = op.combine(a, op.combine(b, c))
            val right = op.combine(op.combine(a, b), c)
            withClue("Associativity failed: $a * ($b * $c) = $left, ($a * $b) * $c = $right") {
                check(eq.eqv(left, right))
            }
        }
    }
}
