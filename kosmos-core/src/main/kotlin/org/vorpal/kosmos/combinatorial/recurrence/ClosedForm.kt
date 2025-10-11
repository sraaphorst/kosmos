package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

interface ClosedForm {
    fun closedForm(n: Int): BigInteger
}
