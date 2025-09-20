package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.take

/** Try up to `attempts` random samples to find a witness for `pred`. */
fun <A> existsSample(
    arb: Arb<A>,
    attempts: Int,
    pred: (A) -> Boolean
): A? = arb.take(attempts).firstOrNull(pred)
