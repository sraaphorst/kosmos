package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.PrimeField
import java.math.BigInteger

/**
 * A set of predefined finite field algebras.
 */
object FiniteFieldAlgebras {
    val F2: PrimeField = PrimeField(BigInteger.TWO)
}