package org.vorpal.kosmos.laws

/**
 * The laws of a structure, as one branch of a law graph.
 *
 * A suite is the **complete** contract of its structure: it lists its parent suites (as their own nodes) and the
 * laws it adds (as leaves). For example, `MonoidLaws` = `SemigroupLaws` + `NonAssociativeMonoidLaws`. A check
 * reached through several parents is not repeated: every suite and law has a [binding], and within one run equal
 * bindings execute once (see [LawRunner]).
 *
 * Compound laws (such as both-sided distributivity) are suites too: one branch whose children are the atomic laws.
 */
interface LawSuite : LawComponent {

    /** Human-readable label for this suite. */
    val name: String
        get() = this::class.simpleName ?: "UnnamedLawSuite"

    /**
     * What this suite is bound to (see [LawBinding]): normally `LawBinding.of(this, <every constructor argument>)`.
     * Two suites of the same class built from the same arguments are then run once per run and shared.
     * Null disables sharing for this suite (its laws can still be shared one by one).
     */
    val binding: LawBinding?
        get() = null

    /** The ordered children of this suite's branch: parent suites first, then the laws this suite adds. */
    fun children(): List<LawNode>

    override fun node(): LawNode =
        LawNode.Branch(name, children(), binding)
}

/**
 * Derive a name for the [LawSuite] based on the object being tested and its operator symbols.
 */
fun suiteName(objName: String, vararg opNames: String): String {
    require(objName.isNotBlank()) { "In suiteName, the object name cannot be blank." }

    val ops = opNames
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return buildString {
        append(objName)
        if (ops.isNotEmpty()) {
            append(" (")
            append(ops.joinToString(", "))
            append(")")
        }
    }
}
