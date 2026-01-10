package org.vorpal.kosmos.combinatorics.numbersystems.render

import org.vorpal.kosmos.combinatorics.numbersystems.Factoradic
import org.vorpal.kosmos.core.render.Printable

object FactoradicPrintable: Printable<Factoradic> {
    override fun render(a: Factoradic): String =
        "(${a.digits.asReversed().joinToString(" ")})_!"
}
