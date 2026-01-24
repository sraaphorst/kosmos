package org.vorpal.kosmos.core.tropical

import org.vorpal.kosmos.core.math.Real

/**
 * The representation of the extended Real numbers adding a [NegInfinity] element.
 */
sealed interface TropicalMax {
    data class Finite(val value: Real) : TropicalMax {
        init {
            require(value.isFinite() && !value.isNaN()) { "Value must be finite / non-NaN" }
        }
    }
    data object NegInfinity : TropicalMax
}
