package org.vorpal.kosmos.combinatorics.numbersystems.render

import org.vorpal.kosmos.combinatorics.numbersystems.Combinadic
import org.vorpal.kosmos.core.render.Printable

object CombinadicPrintable : Printable<Combinadic> {
    override fun render(a: Combinadic): String =
        "(${a.indices.joinToString(" ")})_C(${a.n},${a.k})"
}