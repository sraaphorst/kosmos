package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CarlstromWheel
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.algebra.structures.EuclideanDomain
import org.vorpal.kosmos.algebra.structures.toFracNormalizer
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.FracNormalizer
import org.vorpal.kosmos.core.rational.WheelFrac
import org.vorpal.kosmos.core.rational.WheelZ
import org.vorpal.kosmos.core.rational.wheelFracEq
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.getOrElse
import org.vorpal.kosmos.functional.datastructures.map

object WheelAlgebras {
    /**
     * An algebraic structure representing the [CarlstromWheel] of rational numbers with bottom and infinite elements.
     */
    object CarlstromWheelZ : CarlstromWheel<WheelZ> {
        override val zero: WheelZ = WheelZ.ZERO
        override val one: WheelZ = WheelZ.ONE
        override val bottom: WheelZ = WheelZ.BOTTOM
        override val inf: WheelZ = WheelZ.INF
        override val add: CommutativeMonoid<WheelZ> = CommutativeMonoid.of(
            WheelZ.ZERO,
            BinOp(Symbols.PLUS, WheelZ::plus)
        )
        override val mul: CommutativeMonoid<WheelZ> = CommutativeMonoid.of(
            WheelZ.ONE,
            BinOp(Symbols.ASTERISK, WheelZ::times)
        )
        override val inv: Endo<WheelZ> = Endo(Symbols.INVERSE, WheelZ::inv)
    }

    data class CarlstromWheelOfFrac<A : Any>(
        val wheel: CarlstromWheel<WheelFrac<A>>,
        val eq: Eq<WheelFrac<A>>
    )

    /**
     * Any [CommutativeSemiring] can be used as the base ring for the [CarlstromWheel] of fractions.
     *
     * [eqA] may be supplied for determining equality. If no [eqA] is provided, the default [Eq.default] is used.
     *
     * If a [FracNormalizer] is provided, it will be used to normalize the fractions.
     */
    fun <A : Any> CommutativeSemiring<A>.semiringCarlstromWheelOfFrac(
        eqA: Eq<A> = Eq.default(),
        normalizer: Option<FracNormalizer<A>> = Option.None
    ): CarlstromWheelOfFrac<A> {

        val zeroA = add.identity
        val oneA = mul.identity

        fun isZero(x: A): Boolean =
            eqA(x, zeroA)

        /**
         * Normalize uses the provided [FracNormalizer] if available, otherwise fractions are left in their
         * original form (except for bottom and infinity, which are normalized to 0/0 and 1/0 respectively).
         */
        fun normalize(n: A, d: A): WheelFrac<A> {
            if (isZero(d)) {
                return if (isZero(n)) WheelFrac(zeroA, zeroA) else WheelFrac(oneA, zeroA)
            }

            val (nn, dd) = normalizer
                .map { it.normalize(n, d) }
                .getOrElse { Pair(n, d) }
            return WheelFrac(nn, dd)
        }

        val bottom = WheelFrac(zeroA, zeroA)
        val zero = WheelFrac(zeroA, oneA)
        val one = WheelFrac(oneA, oneA)
        val inf = WheelFrac(oneA, zeroA)

        val addOp = BinOp<WheelFrac<A>>(Symbols.PLUS) { x, y ->
            // addition absorbs bottom (axiomatic in Carlström wheels)
            if (x == bottom || y == bottom) return@BinOp bottom

            // (n/d) + (p/q) = (nq + dp)/(dq)
            val nq = mul(x.n, y.d)
            val dp = mul(x.d, y.n)
            val num = add(nq, dp)
            val den = mul(x.d, y.d)
            normalize(num, den)
        }

        val mulOp = BinOp<WheelFrac<A>>(Symbols.ASTERISK) { x, y ->
            if (x == bottom || y == bottom) return@BinOp bottom

            // (n/d) * (p/q) = (np)/(dq)
            val num = mul(x.n, y.n)
            val den = mul(x.d, y.d)
            normalize(num, den)
        }

        val invOp = Endo<WheelFrac<A>>(Symbols.INVERSE) { x ->
            if (x == bottom) bottom
            else normalize(x.d, x.n)
        }

        val wheel = object : CarlstromWheel<WheelFrac<A>> {
            override val zero = zero
            override val one = one
            override val bottom = bottom
            override val inf = inf

            override val add: CommutativeMonoid<WheelFrac<A>> =
                CommutativeMonoid.of(zero, addOp)

            override val mul: CommutativeMonoid<WheelFrac<A>> =
                CommutativeMonoid.of(one, mulOp)

            override val inv: Endo<WheelFrac<A>> =
                invOp
        }

        val eqWheel: Eq<WheelFrac<A>> = when (normalizer) {
            is Option.Some -> Eq { x, y -> eqA(x.n, y.n) && eqA(x.d, y.d)}
            is Option.None -> wheelFracEq(this, eqA)
        }

        return CarlstromWheelOfFrac(wheel, eqWheel)
    }


    /**
     * Convenience method for creating a [CarlstromWheel] of fractions over a [EuclideanDomain], which is able
     * to derive a [FracNormalizer]. An optional [eqA] can be provided: otherwise, the default [Eq.default] is used.
     */
    fun <A : Any, M : Any> EuclideanDomain<A, M>.euclideanDomainCarlstromWheelOfFrac(
        eqA: Eq<A> = Eq.default()
    ) = semiringCarlstromWheelOfFrac(
        eqA = eqA,
        normalizer = Option.Some(this.toFracNormalizer(eqA))
    )
}
