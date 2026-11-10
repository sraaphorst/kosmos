package org.vorpal.kosmos.laws

/**
 * A [LawSuite] that declares its hierarchy instead of concatenating flat lists.
 *
 * A structured suite lists its children for each [LawMode]: constituent suites (as their own
 * [LawSuite.node]s) and its own laws (as leaves). [laws] and [fullLaws] are then just the
 * depth-first flattenings of the compact and full graphs, so the legacy flat views and the
 * graph can never disagree.
 *
 * The mode passed to a child is chosen explicitly by the parent. Most suites pass their own
 * mode through; some pick a different one (for example, the compact hemiring view leaves out
 * its additive and multiplicative constituents entirely, while the full view includes their
 * full graphs).
 */
interface StructuredLawSuite : LawSuite {

    /** The ordered children of this suite's branch in the given view. */
    fun children(mode: LawMode): List<LawNode>

    /**
     * What this suite is bound to (see [LawBinding]): normally `LawBinding.of(this, <every constructor argument>)`.
     * Two suites of the same class built from the same arguments are then run once per run and shared.
     * Null disables sharing for this suite.
     */
    val binding: LawBinding?
        get() = null

    override fun node(mode: LawMode): LawNode =
        LawNode.Branch(name, children(mode), binding?.let { LawNodeKey(it, mode) })

    override fun laws(): List<TestingLaw> =
        node(LawMode.COMPACT).flatten()

    override fun fullLaws(): List<TestingLaw> =
        node(LawMode.FULL).flatten()
}
