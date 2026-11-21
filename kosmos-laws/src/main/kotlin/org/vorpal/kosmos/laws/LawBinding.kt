package org.vorpal.kosmos.laws

import kotlin.jvm.internal.CallableReference

/**
 * What a law or suite is *bound to*: its kind plus every input that determines what it checks.
 *
 * Two nodes with equal bindings perform the same check, so a run may execute it once and show the result
 * under both parents. Equality is deliberately strict:
 *
 * - the kind is the suite or law class;
 * - every argument is compared by **reference identity** (the same operation object, the same generator,
 *   the same equality), except plain values (numbers, strings, booleans, chars, enums), which compare by value,
 *   and callable references such as `conj::invoke`, which compare by function plus the **identity** of their bound
 *   receiver (so two references to the same function of the same object are the same argument, while references
 *   on two distinct receivers stay separate even if the receivers are `equals`).
 *
 * Names, operation symbols and labels never take part: two different operations that happen to share the
 * symbol `+` are different checks. Rebuilding an argument (a filtered generator, a fresh `Eq.default()`)
 * makes a different binding, which only costs a repeated execution, never a wrong result.
 */
class LawBinding private constructor(
    private val kind: Any,
    private val args: List<Any?>
) {
    override fun equals(other: Any?): Boolean =
        other is LawBinding &&
            kind == other.kind &&
            args.size == other.args.size &&
            args.indices.all { same(args[it], other.args[it]) }

    override fun hashCode(): Int =
        args.fold(kind.hashCode()) { h, a -> 31 * h + hashOf(a) }

    override fun toString(): String = "LawBinding($kind, ${args.size} args)"

    companion object {
        /** Bind a suite or law instance to the arguments it was constructed with. */
        fun of(owner: Any, vararg args: Any?): LawBinding = LawBinding(owner::class, args.toList())

        /**
         * Bind a law that has no class of its own (for example one built by [TestingLaw.named]) to a stable [kind]
         * plus its arguments. Use a distinct kind per kind of check.
         */
        fun named(kind: String, vararg args: Any?): LawBinding = LawBinding(kind, args.toList())

        private fun isValue(x: Any?): Boolean =
            x == null || x is Number || x is String || x is Boolean || x is Char || x is Enum<*>

        /**
         * Kotlin's own `equals` on callable references compares bound receivers with *their* `equals`, so two
         * equal-but-distinct receivers (say data class instances with different mutable state) would merge two
         * different checks. Compare the referenced function by value and the receiver by reference instead.
         */
        private fun sameCallable(a: CallableReference, b: CallableReference): Boolean =
            a.boundReceiver === b.boundReceiver &&
                a.owner == b.owner &&
                a.name == b.name &&
                a.signature == b.signature

        private fun same(a: Any?, b: Any?): Boolean = when {
            isValue(a) || isValue(b) -> a == b
            a is CallableReference && b is CallableReference -> sameCallable(a, b)
            else -> a === b
        }

        private fun hashOf(x: Any?): Int = when {
            isValue(x) -> x.hashCode()
            x is CallableReference ->
                31 * (31 * System.identityHashCode(x.boundReceiver) + x.name.hashCode()) + x.signature.hashCode()
            else -> System.identityHashCode(x)
        }
    }
}
