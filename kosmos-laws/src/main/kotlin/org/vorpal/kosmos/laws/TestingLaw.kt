package org.vorpal.kosmos.laws

import io.kotest.core.Tuple4
import io.kotest.core.Tuple5
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.triple

/**
 * A single property / law that can be checked: one leaf of a law graph.
 *
 * Implementations signal a false law with [lawCheck] / [LawViolation] (see [lawCheckAll]).
 */
fun interface TestingLaw : LawComponent {
    suspend fun test()

    /**
     * What this law is bound to (see [LawBinding]): normally `LawBinding.of(this, <every argument that changes the
     * check>)`. Within one run, laws with equal bindings execute once, wherever they appear in the graph.
     * Null (the default) means this law is never shared with a different instance.
     */
    val binding: LawBinding?
        get() = null

    override fun node(): LawNode = LawNode.Leaf(this)

    /**
     * Human-readable label for this law.
     *
     * Defaults to the simple class name, but you can override it
     * when you want a more descriptive name.
     */
    val name: String
        get() = this::class.simpleName ?: "UnnamedLaw"

    companion object {
        /**
         * A law from a lambda. Without a [binding] it is never shared with another instance; give one (for example
         * `LawBinding.named("my law", op, arb, eq)`) when the same check may be registered more than once.
         */
        fun named(name: String, binding: LawBinding? = null, test: suspend () -> Unit): TestingLaw =
            object : TestingLaw {
                override val name: String = name
                override val binding: LawBinding? = binding
                override suspend fun test() = test()
            }

        fun <A> arbPair(arb: Arb<A>): Arb<Pair<A, A>> = Arb.pair(arb, arb)
        fun <A> arbTriple(arb: Arb<A>): Arb<Triple<A, A, A>> = Arb.triple(arb, arb, arb)

        /**
         * Quadruples will be necessary for testing posets / orders.
         */
        fun <A> arbQuadruple(arb: Arb<A>): Arb<Tuple4<A, A, A, A>> =
            Arb.bind(arb, arb, arb, arb) { a, b, c, d -> Tuple4(a, b, c, d) }

        /**
         * Quintuples will be necessary for heap testing.
         */
        fun <A> arbQuintuple(arb: Arb<A>): Arb<Tuple5<A, A, A, A, A>> =
            Arb.bind(arb, arb, arb, arb, arb) { a, b, c, d, e -> Tuple5(a, b, c, d, e) }
    }
}

/**
 * Utility to run several laws in sequence.
 *
 * If any law fails (throws), the surrounding test framework
 * will report the failure.
 */
suspend fun runLaws(vararg laws: TestingLaw) {
    for (law in laws) {
        law.test()
    }
}
