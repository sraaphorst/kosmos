package org.vorpal.kosmos.core.gaussian

import java.math.BigInteger

/**
 * The type for a Gaussian integer, i.e. a [BigInteger] real and complex component.
 */
data class GaussianInt(val re: BigInteger, val im: BigInteger)
