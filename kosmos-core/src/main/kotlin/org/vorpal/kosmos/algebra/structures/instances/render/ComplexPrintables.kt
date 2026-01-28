package org.vorpal.kosmos.algebra.structures.instances.render

import org.vorpal.kosmos.algebra.structures.instances.Complex
import org.vorpal.kosmos.algebra.structures.instances.im
import org.vorpal.kosmos.algebra.structures.instances.re
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable

object ComplexPrintables {

    val default: Printable<Complex> =
        of(
            prReal = RealPrintables.RealPrintable,
            eps = 0.0,
            useParensForMixedTerms = true,
        )

    fun normalizedZero(
        eps: Real = 1e-12,
        useParensForMixedTerms: Boolean = true,
    ): Printable<Complex> =
        of(
            prReal = RealPrintables.normalizedZero(eps),
            eps = eps,
            useParensForMixedTerms = useParensForMixedTerms,
        )

    /**
     * Render a Complex as a factor in a product.
     * If it contains a top-level " + " or " - ", wrap it in parentheses.
     */
    fun asFactor(
        z: Complex,
        pr: Printable<Complex> = default,
    ): String =
        parenIfNeededForFactor(pr(z))

    fun of(
        prReal: Printable<Real>,
        eps: Real,
        useParensForMixedTerms: Boolean = true,
    ): Printable<Complex> =
        Printable { z ->
            /**
             * Note: addSignedTerm is an extension function that needs two receivers, namely:
             * 1. MutableList<String>
             * 2. SignedTermBuilders
             * hence the need for the with statement.
             *
             * This could instead be written:
             * val terms = buildList {
             *     with(SignedTermBuilders) {
             *         this@buildList.addSignedTerm(z.re, "", prReal, eps) { mag, sym -> "$mag$sym" }
             *         this@buildList.addSignedTerm(z.im, "i", prReal, eps) { mag, sym -> "$mag$sym" }
             *     }
             * }
             *
             * or:
             * val terms = buildList {
             *     SignedTermBuilders.run {
             *         addSignedTerm(z.re, "", prReal, eps) { mag, sym -> "$mag$sym" }
             *         addSignedTerm(z.im, "i", prReal, eps) { mag, sym -> "$mag$sym" }
             *     }
             * }
             */
            val terms = buildList {
                with(SignedTermBuilders) {
                    addSignedTerm(z.re, "", prReal, eps) { mag, sym -> "$mag$sym" }
                    addSignedTerm(z.im, "i", prReal, eps) { mag, sym -> "$mag$sym" }
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
