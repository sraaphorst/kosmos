package org.vorpal.kosmos.combinatorics.blockdesign

interface SteinerSystem<T : Any> : BlockDesign<T> {
    override val lambda: Int
        get() = 1

    /**
     * Steiner systems are always simple.
     */
    val isSimple: Boolean
        get() = true
}
