package org.vorpal.kosmos.combinatorics.numbersystems

import org.vorpal.kosmos.core.render.Printable

object FactoradicPrinter: Printable<Factoradic> {
    override fun render(a: Factoradic): String =
        "(${a.digits.asReversed().joinToString(" ")})_!"

}
