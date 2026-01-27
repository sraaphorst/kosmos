package org.vorpal.kosmos.functional.datastructures

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map

/**
 * Arbitraries for property-based testing
 */
object ArbEither {
    fun <L, R> either(arbL: Arb<L>, arbR: Arb<R>): Arb<Either<L, R>> =
        arbitrary { rs ->
            if (rs.random.nextBoolean()) {
                Either.Left(arbL.bind())
            } else {
                Either.Right(arbR.bind())
            }
        }

    fun <L> left(arbL: Arb<L>): Arb<Either<L, Nothing>> =
        arbL.map { Either.Left(it) }

    fun <R> right(arbR: Arb<R>): Arb<Either<Nothing, R>> =
        arbR.map { Either.Right(it) }
}
