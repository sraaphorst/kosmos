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

    /**
     * Whether an [AssertionError] raised while this law runs (a Kotest matcher such as `shouldBe`, inside or outside
     * [lawCheckAll]) means the law is false, as a [LawViolation] does.
     *
     * False (the default) for reusable laws, which signal falsity only with [lawCheck]: there an [AssertionError]
     * from inside a property is an error like any other exception. True for spec-local laws, whose assertions are
     * written with matchers.
     *
     * The classification is by exception type, not by throw site: in a law with this flag, an [AssertionError]
     * thrown by the code under test (an internal invariant check, say) also reads as failed, not errored. Either way
     * the leaf is a problem, so nothing broken passes; only the failed/errored diagnosis is weaker. Every other
     * exception (from the operation, the generator, the equality) is an error, not a failure.
     */
    val assertionsAreViolations: Boolean
        get() = false

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
         * Set [assertionsAreViolations] when the body asserts with Kotest matchers (see [TestingLaw.assertionsAreViolations]).
         */
        fun named(
            name: String,
            binding: LawBinding? = null,
            assertionsAreViolations: Boolean = false,
            test: suspend () -> Unit
        ): TestingLaw =
            object : TestingLaw {
                override val name: String = name
                override val binding: LawBinding? = binding
                override val assertionsAreViolations: Boolean = assertionsAreViolations
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
