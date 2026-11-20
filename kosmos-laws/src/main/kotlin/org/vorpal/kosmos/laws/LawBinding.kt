package org.vorpal.kosmos.laws

/**
 * What a law or suite is *bound to*: its kind plus every input that determines what it checks.
 *
 * Two nodes with equal bindings perform the same check, so a run may execute it once and show the result
 * under both parents. Equality is deliberately strict:
 *
 * - the kind is the suite or law class;
 * - every argument is compared by **reference identity** (the same operation object, the same generator,
 *   the same equality, the same printable), except plain values (numbers, strings, booleans, chars, enums),
 *   which compare by value.
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

        private fun isValue(x: Any?): Boolean =
            x == null || x is Number || x is String || x is Boolean || x is Char || x is Enum<*>

        private fun same(a: Any?, b: Any?): Boolean =
            if (isValue(a) || isValue(b)) a == b else a === b

        private fun hashOf(x: Any?): Int =
            if (isValue(x)) x.hashCode() else System.identityHashCode(x)
    }
}

/**
 * The cache key of a node in a run: a binding together with the view it was built in.
 * Compact and full graphs of the same suite are different nodes.
 */
data class LawNodeKey(val binding: LawBinding, val mode: LawMode?)
