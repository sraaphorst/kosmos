package org.vorpal.kosmos.combinatorics.recurrence

import java.math.BigInteger

interface ClosedForm {
    fun closedForm(n: Int): BigInteger
}
