package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Ring
import java.math.BigInteger

fun <S: Any> Ring<S>.zHom(): RingHomomorphism<BigInteger, S> =
    RingHomomorphism.of(
        domain = IntegerAlgebras.ZCommutativeRing,
        codomain = this as CommutativeRing<S>,
        map = ::fromBigInt
    )
