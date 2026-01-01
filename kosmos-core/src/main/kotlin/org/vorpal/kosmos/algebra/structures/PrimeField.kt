package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger


/**
 * Prime fields 𝔽_p.
 */
data class PrimeField(val p: BigInteger) : Field<BigInteger> {
    init {
        require(p.isProbablePrime(PRIME_CHECKING)) { "$p is not a probable prime" }
    }

    override val reciprocal: Endo<BigInteger> =
        Endo(Symbols.SLASH) { a ->
            val c = canon(a)
            require(c != BigInteger.ZERO) { "Zero has no multiplicative inverse in 𝔽_$p" }
            c.modInverse(p)
        }

    /**
     * Map the value to {0, ..., p-1}.
     */
    private fun canon(n: BigInteger): BigInteger = n.mod(p)

    override val add = AbelianGroup.of(
        identity = BigInteger.ZERO,
        op = BinOp(Symbols.PLUS) { a, b -> canon(a + b) },
        inverse = Endo(Symbols.MINUS) { a -> canon(-a) }
    )

    override val mul = CommutativeMonoid.of(
        identity = canon(BigInteger.ONE),
        op = BinOp(Symbols.ASTERISK) { a, b -> canon(a * b) }
    )

    override fun fromBigInt(n: BigInteger): BigInteger =
        canon(n)

    companion object {
        /**
         * For the purposes of primality for PrimeFields, run Miller-Rabin for this many rounds on
         * the prime passed to this function to check primality.
         *
         * This should be more than sufficient, giving only approximately a 9e-16 chance of
         * a false positive.
         */
        const val PRIME_CHECKING = 50
    }
}