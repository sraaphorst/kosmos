package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

/**
 * Prime fields 𝔽_p.
 */
data class PrimeField(val p: BigInteger) : Field<BigInteger>, HasCharacteristic {
    init {
        require(p.isProbablePrime(PRIME_CHECKING)) { "$p is not a probable prime" }
    }

    override val characteristic: BigInteger = p

    /**
     * Invertibility is decided on the canonical representative, so every multiple of `p` (not just
     * the literal `0`) is rejected.
     */
    override fun hasReciprocal(a: BigInteger): Boolean =
        canon(a).signum() != 0

    override val reciprocal: Endo<BigInteger> =
        Endo(Symbols.INVERSE) { a ->
            if (!hasReciprocal(a)) throw HasReciprocal.notInvertible(a, "${Symbols.BB_F}_$p")
            canon(a).modInverse(p)
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