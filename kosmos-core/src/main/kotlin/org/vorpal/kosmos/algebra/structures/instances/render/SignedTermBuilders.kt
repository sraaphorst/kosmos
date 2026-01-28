package org.vorpal.kosmos.algebra.structures.instances.render

import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable
import kotlin.math.abs

internal object SignedTermBuilders {

    internal fun isZero(x: Real, eps: Real): Boolean =
        if (eps == 0.0) x == 0.0 else abs(x) < eps

    internal fun isOne(x: Real, eps: Real): Boolean =
        if (eps == 0.0) x == 1.0 else abs(x - 1.0) < eps

    /**
     * Append a signed term for coeff * sym into the provided list.
     *
     * Rules:
     * - skip if coeff ~ 0
     * - suppress magnitude if |coeff| ~ 1 and sym is not empty
     * - sign style: first term has no leading spaces, subsequent terms use " + " / " - "
     * - basis multiplication formatting is controlled by [joinMagSym]
     */
    internal fun MutableList<String>.addSignedTerm(
        coeff: Real,
        sym: String,
        prReal: Printable<Real>,
        eps: Real,
        joinMagSym: (mag: String, sym: String) -> String,
    ) {
        if (isZero(coeff, eps)) return

        val negative = coeff < 0.0
        val absCoeff = abs(coeff)

        val mag =
            when {
                isOne(absCoeff, eps) && sym.isNotEmpty() -> ""
                else -> prReal(absCoeff)
            }

        val core =
            when {
                sym.isEmpty() -> mag
                mag.isEmpty() -> sym
                else -> joinMagSym(mag, sym)
            }

        val signed =
            if (isEmpty()) {
                if (negative) "-$core" else core
            } else {
                if (negative) " - $core" else " + $core"
            }

        add(signed)
    }

    /**
     * Call style #2: explicit list parameter.
     * Just forwards to the extension version.
     *
     * Allows calls like:
     * ```
     * val terms = buildList {
     *     SignedTermBuilders.addSignedTerm(
     *         terms = this,
     *         coeff = z.re,
     *         sym = "",
     *         prReal = prReal,
     *         eps = eps,
     *         joinMagSym = { mag, sym -> "$mag$sym" },
     *     )
     *
     *     SignedTermBuilders.addSignedTerm(
     *         terms = this,
     *         coeff = z.im,
     *         sym = "i",
     *         prReal = prReal,
     *         eps = eps,
     *         joinMagSym = { mag, sym -> "$mag$sym" },
     *     )
     * }
     * ```
     *
     * This has the same JVM signature as the above method, so we have to give it a distinct JVM name.
     */
    @JvmName("addSignedTermExplicit")
    internal fun addSignedTerm(
        terms: MutableList<String>,
        coeff: Real,
        sym: String,
        prReal: Printable<Real>,
        eps: Real,
        joinMagSym: (mag: String, sym: String) -> String,
    ) {
        terms.addSignedTerm(coeff, sym, prReal, eps, joinMagSym)
    }
}
