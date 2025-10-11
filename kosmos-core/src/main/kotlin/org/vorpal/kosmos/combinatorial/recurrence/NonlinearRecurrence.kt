package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/** Marker interface for nonlinear recurrences. */
interface NonlinearRecurrence {
    val next: (List<BigInteger>) -> BigInteger
}
