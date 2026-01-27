package org.vorpal.kosmos.core.tropical

import org.vorpal.kosmos.core.math.Real

/**
 * The representation of the extended Real numbers adding a [PosInfinity] element.
 */
sealed interface TropicalMin {
    data class Finite(val value: Real) : TropicalMin {
        init {
            require(value.isFinite() && !value.isNaN()) { "Value must be finite / non-NaN" }
        }
    }
    data object PosInfinity : TropicalMin
}
