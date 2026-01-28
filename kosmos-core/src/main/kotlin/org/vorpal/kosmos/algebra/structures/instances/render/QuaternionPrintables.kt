package org.vorpal.kosmos.algebra.structures.instances.render

import org.vorpal.kosmos.algebra.structures.instances.Quaternion
import org.vorpal.kosmos.algebra.structures.instances.w
import org.vorpal.kosmos.algebra.structures.instances.x
import org.vorpal.kosmos.algebra.structures.instances.y
import org.vorpal.kosmos.algebra.structures.instances.z
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable

object QuaternionPrintables {

    val default: Printable<Quaternion> =
        of(
            prReal = RealPrintables.RealPrintable,
            eps = 0.0,
            useParensForMixedTerms = true,
        )

    fun normalizedZero(
        eps: Real = 1e-12,
        useParensForMixedTerms: Boolean = true,
    ): Printable<Quaternion> =
        of(
            prReal = RealPrintables.normalizedZero(eps),
            eps = eps,
            useParensForMixedTerms = useParensForMixedTerms,
        )

    /**
     * Render a quaternion as a factor in a product.
     */
    fun asFactor(
        q: Quaternion,
        pr: Printable<Quaternion> = default,
    ): String =
        parenIfNeededForFactor(pr(q))

    fun of(
        prReal: Printable<Real>,
        eps: Real,
        useParensForMixedTerms: Boolean = true,
    ): Printable<Quaternion> =
        Printable { q ->
            val terms = buildList {
                with(SignedTermBuilders) {
                    addSignedTerm(q.w, "", prReal, eps) { mag, sym -> "$mag$sym" }
                    addSignedTerm(q.x, "i", prReal, eps) { mag, sym -> "$mag$sym" }
                    addSignedTerm(q.y, "j", prReal, eps) { mag, sym -> "$mag$sym" }
                    addSignedTerm(q.z, "k", prReal, eps) { mag, sym -> "$mag$sym" }
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
