package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/** Marker interface for linear recurrences. */
interface LinearRecurrence {
    val coefficients: List<BigInteger>
}
