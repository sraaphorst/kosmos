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
import kotlin.math.abs

object OctonionPrintables {

    val default: Printable<Octonion> =
        of(
            prReal = RealPrintables.RealPrintable,
            eps = 0.0,
            useParensForMixedTerms = true,
        )

    fun normalizedZero(
        eps: Real = 1e-12,
        useParensForMixedTerms: Boolean = true,
    ): Printable<Octonion> =
        of(
            prReal = RealPrintables.normalizedZero(eps),
            eps = eps,
            useParensForMixedTerms = useParensForMixedTerms,
        )

    fun asFactor(
        o: Octonion,
        pr: Printable<Octonion> = default,
    ): String =
        parenIfNeededForFactor(pr(o))

    fun of(
        prReal: Printable<Real>,
        eps: Real,
        useParensForMixedTerms: Boolean = true,
    ): Printable<Octonion> =
        Printable { o ->
            // Note: octonions get a `$mag·$sym` join for readability.
            val terms = buildList {
                with(SignedTermBuilders) {
                    addSignedTerm(o.w, "", prReal, eps) { mag, sym -> mag } // sym empty anyway
                    addSignedTerm(o.x, "e1", prReal, eps) { mag, sym -> "$mag·$sym" }
                    addSignedTerm(o.y, "e2", prReal, eps) { mag, sym -> "$mag·$sym" }
                    addSignedTerm(o.z, "e3", prReal, eps) { mag, sym -> "$mag·$sym" }
                    addSignedTerm(o.u, "e4", prReal, eps) { mag, sym -> "$mag·$sym" }
                    addSignedTerm(o.v, "e5", prReal, eps) { mag, sym -> "$mag·$sym" }
                    addSignedTerm(o.s, "e6", prReal, eps) { mag, sym -> "$mag·$sym" }
                    addSignedTerm(o.t, "e7", prReal, eps) { mag, sym -> "$mag·$sym" }
                }
            }

            when {
                terms.isEmpty() -> "0"
                !useParensForMixedTerms -> terms.joinToString(separator = "")
                terms.size == 1 -> terms[0]
                else -> "(${terms.joinToString(separator = "")})"
            }
        }

    private fun parenIfNeededForFactor(s: String): String =
        if (needsParensAsFactor(s)) "($s)" else s

    private fun needsParensAsFactor(s: String): Boolean =
        s.contains(" + ") || s.contains(" - ")
}
