package org.vorpal.kosmos.algebra.structures.instances.render

import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable
import kotlin.math.abs

object RealPrintables {
    /**
     * Default Real printable, which just prints everything regardless of value.
     */
    val RealPrintable: Printable<Real> = Printable.default()

    /**
     * Snap tiny values to 0.0 and render with default Double toString().
     * You can extend this later to fixed decimals, stripping "-0.0", etc.
     */
    fun normalizedZero(eps: Real = 1e-12): Printable<Real> =
        Printable { x ->
            val y = if (abs(x) < eps) 0.0 else x
            if (y == 0.0) "0.0" else y.toString()
        }
}
