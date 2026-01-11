package org.vorpal.kosmos.algebra.structures.instances.render

import org.vorpal.kosmos.algebra.structures.instances.Quaternion
import org.vorpal.kosmos.algebra.structures.instances.w
import org.vorpal.kosmos.algebra.structures.instances.x
import org.vorpal.kosmos.algebra.structures.instances.y
import org.vorpal.kosmos.algebra.structures.instances.z
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable

object QuaternionPrintable: Printable<Quaternion> {
    override fun render(a: Quaternion): String {
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

            addTerm(a.w, "")
            addTerm(a.x, "i")
            addTerm(a.y, "j")
            addTerm(a.z, "k")
        }

        return when {
            terms.isEmpty() -> "0"
            terms.size == 1 && !terms[0].contains(" + ") && !terms[0].contains(" - ") -> terms[0]
            else -> "(${terms.joinToString(separator = "")})"
        }
    }
}
