package org.vorpal.kosmos.combinatorics.recurrence

import java.math.BigInteger

/** Marker interface for linear recurrences. */
interface LinearRecurrence {
    val coefficients: List<BigInteger>
}
