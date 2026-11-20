package org.vorpal.kosmos.laws

/**
 * What a law or suite is *bound to*: its kind plus every input that determines what it checks.
 *
 * Two nodes with equal bindings perform the same check, so a run may execute it once and show the result
 * under both parents. Equality is deliberately strict:
 *
 * - the kind is the suite or law class;
 * - every argument is compared by **reference identity** (the same operation object, the same generator,
 *   the same equality), except plain values (numbers, strings, booleans, chars, enums), which compare by value,
 *   and bound callable references such as `conj::invoke`, which compare by receiver and function (so two
 *   references to the same function of the same object are the same argument).
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

        private fun isCallableReference(x: Any?): Boolean =
            x is kotlin.jvm.internal.CallableReference

        private fun same(a: Any?, b: Any?): Boolean =
            if (isValue(a) || isValue(b) || isCallableReference(a)) a == b else a === b

        private fun hashOf(x: Any?): Int =
            if (isValue(x) || isCallableReference(x)) x.hashCode() else System.identityHashCode(x)
    }
}
