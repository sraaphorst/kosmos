package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.structures.Ring
import java.math.BigInteger

/**
 * The canonical [RingHomomorphism] from the integers into any [Ring].
 *
 * This map sends `n ∈ ℤ` to its image under the ring's `fromBigInt` implementation.
 */
fun <S: Any> Ring<S>.zHomomorphism(): RingHomomorphism<BigInteger, S> =
    RingHomomorphism.of(
        domain = IntegerAlgebras.IntegerCommutativeRing,
        codomain = this,
        map = ::fromBigInt
    )
