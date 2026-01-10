package org.vorpal.kosmos.algebra.structures.instances.render

import org.vorpal.kosmos.algebra.structures.instances.Complex
import org.vorpal.kosmos.algebra.structures.instances.im
import org.vorpal.kosmos.algebra.structures.instances.re
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable

object ComplexPrintable: Printable<Complex> {
    override fun render(a: Complex): String {
        val terms = buildList {
            fun addTerm(coeff: Real, sym: String) {
                if (coeff == 0.0) return

                val abs = kotlin.math.abs(coeff)
                val mag =
                    when {
                        abs == 1.0 && sym.isNotEmpty() -> "" // 1·i -> i
                        else -> abs.toString()
                    }

                val core =
                    when {
                        sym.isEmpty() -> mag
                        mag.isEmpty() -> sym
                        else -> "$mag·$sym"
                    }

                val signed =
                    if (isEmpty()) {
                        if (coeff < 0) "-$core" else core
                    } else {
                        if (coeff < 0) " - $core" else " + $core"
                    }

                add(signed)
            }

            addTerm(a.re, "")
            addTerm(a.im, "i")
        }

        return when {
            terms.isEmpty() -> "0"
            terms.size == 1 && !terms[0].contains(" + ") && !terms[0].contains(" - ") -> terms[0]
            else -> "(${terms.joinToString(separator = "")})"
        }
    }
}