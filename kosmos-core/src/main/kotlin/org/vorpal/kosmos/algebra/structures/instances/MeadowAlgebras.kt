package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Meadow
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.toMeadow
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.lcm
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

/**
 * Concrete [Meadow]s:
 * - [RationalMeadow]: `ℚ₀`, the zero-totalized rationals.
 * - [BoolMeadow]: `𝔽₂` on Kotlin `Boolean`, where `inv` is the identity.
 * - [primeFieldMeadow]: `𝔽ₚ` with `inv(0) = 0`.
 * - [ZModMeadow]: `ℤ/nℤ` for squarefree `n`, which has zero divisors unless `n` is prime.
 *
 * Products of meadows are meadows: see `Meadows.product`.
 */
object MeadowAlgebras {

    /**
     * The zero-totalized rationals `ℚ₀`, the meadow studied by Bergstra and Tucker.
     */
    val RationalMeadow: Meadow<Rational> =
        RationalAlgebras.RationalField.toMeadow()

    /**
     * `𝔽₂` on `Boolean`. Every element is its own inverse, since `x * x = x`.
     */
    val BoolMeadow: Meadow<Boolean> =
        BooleanAlgebras.BoolField.toMeadow()

    /**
     * The zero-totalized prime field `𝔽ₚ`.
     */
    fun primeFieldMeadow(p: BigInteger): Meadow<BigInteger> =
        PrimeField(p).toMeadow()

    /**
     * The meadow `ℤ/nℤ` for squarefree `n ≥ 1`, with elements represented by `{0, ..., n - 1}`.
     *
     * `ℤ/nℤ` admits a meadow inverse exactly when `n` is squarefree: then by the CRT it is the product of the
     * prime fields `𝔽ₚ` for `p | n`, and the inverse is computed componentwise without factoring at use time:
     *
     * ```
     * inv(x) = x^(2λ - 1)
     * ```
     *
     * where `λ = λ(n) = lcm(p - 1 : p | n)` is the Carmichael function. In each component `𝔽ₚ`, a nonzero `x`
     * has `x^λ = 1`, so `x^(2λ - 1) = x^(λ - 1) = x⁻¹`, while `0^(2λ - 1) = 0` since `2λ - 1 ≥ 1`.
     *
     * If `n` is prime, this is a cancellation meadow (a zero-totalized field). Otherwise, it has zero divisors:
     * e.g. in `ℤ/6ℤ`, `inv(2) = 2` and `2 * inv(2) = 4`, a nontrivial idempotent.
     */
    data class ZModMeadow(val n: BigInteger) : Meadow<BigInteger> {
        init {
            require(n.signum() > 0) { "Modulus must be positive: $n" }
            require(isSquarefree(n)) { "ℤ/${n}ℤ is a meadow only when the modulus is squarefree: $n" }
        }

        /**
         * The Carmichael function `λ(n)` of the squarefree modulus.
         */
        val carmichael: BigInteger =
            primeFactors(n).map { it - BigInteger.ONE }.fold(BigInteger.ONE) { a, b -> lcm(a, b) }

        private val exponent: BigInteger =
            carmichael.shiftLeft(1) - BigInteger.ONE

        private fun canon(a: BigInteger): BigInteger = a.mod(n)

        override val add: AbelianGroup<BigInteger> = AbelianGroup.of(
            identity = canon(BigInteger.ZERO),
            op = BinOp(Symbols.PLUS) { a, b -> canon(a + b) },
            inverse = Endo(Symbols.MINUS) { a -> canon(-a) }
        )

        override val mul: CommutativeMonoid<BigInteger> = CommutativeMonoid.of(
            identity = canon(BigInteger.ONE),
            op = BinOp(Symbols.ASTERISK) { a, b -> canon(a * b) }
        )

        override val inv: Endo<BigInteger> =
            Endo(Symbols.INVERSE) { a -> canon(a).modPow(exponent, n) }

        companion object {
            /**
             * The distinct prime factors of `n ≥ 1`, in increasing order, by trial division.
             */
            internal fun primeFactors(n: BigInteger): List<BigInteger> =
                distinctPrimeFactors(n, BigInteger.TWO, emptyList())

            /**
             * `n ≥ 1` is squarefree if no prime square divides it.
             */
            fun isSquarefree(n: BigInteger): Boolean =
                n.signum() > 0 && primeFactors(n).none { p -> n.mod(p * p).signum() == 0 }

            private tailrec fun distinctPrimeFactors(
                n: BigInteger,
                d: BigInteger,
                acc: List<BigInteger>
            ): List<BigInteger> = when {
                n == BigInteger.ONE -> acc
                d * d > n -> acc + n
                n.mod(d).signum() == 0 -> distinctPrimeFactors(divideOut(n, d), d + BigInteger.ONE, acc + d)
                else -> distinctPrimeFactors(n, d + BigInteger.ONE, acc)
            }

            private tailrec fun divideOut(n: BigInteger, d: BigInteger): BigInteger =
                if (n.mod(d).signum() == 0) divideOut(n / d, d) else n
        }
    }
}
