package org.vorpal.kosmos.algebra.structures.instances.render

import org.vorpal.kosmos.algebra.structures.instances.Octonion
import org.vorpal.kosmos.algebra.structures.instances.s
import org.vorpal.kosmos.algebra.structures.instances.t
import org.vorpal.kosmos.algebra.structures.instances.u
import org.vorpal.kosmos.algebra.structures.instances.v
import org.vorpal.kosmos.algebra.structures.instances.w
import org.vorpal.kosmos.algebra.structures.instances.x
import org.vorpal.kosmos.algebra.structures.instances.y
import org.vorpal.kosmos.algebra.structures.instances.z
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable

object OctonionPrintable: Printable<Octonion> {
    override fun render(a: Octonion): String {
        val terms = buildList {
            fun addTerm(coeff: Real, sym: String) {
                if (coeff == 0.0) return

                val abs = kotlin.math.abs(coeff)
                val mag =
                    when {
                        abs == 1.0 && sym.isNotEmpty() -> "" // 1·e1 -> e1
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

            addTerm(a.w, "")
            addTerm(a.x, "e1")
            addTerm(a.y, "e2")
            addTerm(a.z, "e3")
            addTerm(a.u, "e4")
            addTerm(a.v, "e5")
            addTerm(a.s, "e6")
            addTerm(a.t, "e7")
        }

        return when {
            terms.isEmpty() -> "0"
            terms.size == 1 && !terms[0].contains(" + ") && !terms[0].contains(" - ") -> terms[0]
            else -> "(${terms.joinToString(separator = "")})"
        }
    }
}
