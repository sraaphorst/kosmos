package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Symbols
import java.math.BigInteger

/**
 * The supplied exact division operations must return q such that numerator = denominator * q.
 * This function does not require field division, but it does require that every division
 * performed by the Bareiss algorithm is exact for the given matrix and ring.
 *
 * If exact division is impossible, should throw.
 */
object ExactDivOps {
    /**
     * An exact division operation for [BigInteger]s.
     */
    val bigInteger: BinOp<BigInteger> =
        BinOp(Symbols.SLASH) { a, b ->
            if (b == BigInteger.ZERO) throw ArithmeticException("Exact division by zero.")
            val qr = a.divideAndRemainder(b)
            if (qr[1] != BigInteger.ZERO) {
                throw ArithmeticException("Division is not exact: $a / $b leaves remainder ${qr[1]}.")
            }
            qr[0]
        }

    /**
     * We should always be able to create a valid exact division operation from a [Field].
     * If we cannot, then there is something wrong with the field.
     */
    fun <A : Any> fromField(field: Field<A>): BinOp<A> =
        BinOp(Symbols.SLASH) { a, b ->
            if (!field.hasReciprocal(b)) throw ArithmeticException("Exact division by a non-invertible element: $b.")
            field.mul(a, field.reciprocal(b))
        }
}