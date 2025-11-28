package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.take

private fun <A : Any> existsSample(
    arb: Arb<A>,
    attempts: Int,
    pred: (A) -> Boolean
): A? =
    arb.take(attempts).firstOrNull(pred)
