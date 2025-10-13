package org.vorpal.kosmos.combinatorics.recurrence

import java.math.BigInteger

/**
 * A cached sequence that also has a closed-form formula.
 *
 * Subclasses implement [closedFormCalculator] to define the formula.
 */
abstract class CachedClosedFormSequence : CachedRecursiveSequence(), ClosedForm {

    private val closedFormCache = createCache()

    override fun closedForm(n: Int): BigInteger =
        closedFormCache.getOrPut(n) { closedFormCalculator(n) }

    protected abstract fun closedFormCalculator(n: Int): BigInteger
}
