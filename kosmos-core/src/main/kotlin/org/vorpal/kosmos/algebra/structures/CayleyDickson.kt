package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

/**
 * The Cayley-Dickson elements: a Pair over the CommutativeRing.
 */
data class CD<A : Any>(val a: A, val b: A) {
    companion object {
        fun <A : Any> eq(eqA: Eq<A>): Eq<CD<A>> = Eq { x, y ->
            eqA(x.a, y.a) && eqA(x.b, y.b)
        }
    }
}

/**
 * The generic doubling step.
 *
 * It always gives (at least) an NonAssociativeInvolutiveRing<CD<A>>.
 *
 * It doesn't try to decide "this is a Field, this is only a Ring, etc."
 * We will do tha that per stage when we know the algebraic facts by wrapping
 * it, e.g., in a Field<Complex>.
 *
 * We include `sigma` here to be able to use the Cayley-Dickson process to build the split versions.
 */
class CayleyDickson<A : Any> private constructor(
    private val base: NonAssociativeInvolutiveRing<A>,
    val sigma: A
) : NonAssociativeInvolutiveRing<CD<A>> {

    override val add: AbelianGroup<CD<A>> = AbelianGroup.of<CD<A>>(
        identity = CD(base.add.identity, base.add.identity),
        op = BinOp(Symbols.PLUS) { (a, b), (c, d) ->
            CD(
                base.add(a, c),
                base.add(b, d)
            )
        },
        inverse = Endo(Symbols.MINUS) { (a, b) ->
            CD(
                base.add.inverse(a),
                base.add.inverse(b)
            )
        }
    )

    override val mul: NonAssociativeMonoid<CD<A>> = NonAssociativeMonoid.of(
        identity = CD(base.mul.identity, base.add.identity),
        op = BinOp<CD<A>>(Symbols.ASTERISK) { (a, b), (c, d) ->
            // (a, b)(c, d) = (ac - sigma * d* b, da + b c*)
            val ac = base.mul(a, c)
            val dbs = base.mul(base.conj(d), b)
            val sigDbs = base.mul(sigma, dbs)
            val first = base.add(ac, base.add.inverse(sigDbs))

            val da = base.mul(d, a)
            val bcs = base.mul(b, base.conj(c))
            val second = base.add(da, bcs)

            CD(first, second)
        }
    )

    override fun fromBigInt(n: BigInteger): CD<A> =
        CD(base.fromBigInt(n), base.add.identity)

    override val conj = Endo<CD<A>>(Symbols.CONJ) { (a, b) ->
        CD(base.conj(a), base.add.inverse(b))
    }

    companion object {
        /**
         * Constructor for the Cayley–Dickson doubling step.
         *
         * - `usual` uses σ = 1 (the multiplicative identity)
         * - `split` uses σ = -1 (the additive inverse of 1)
         *
         * These names correspond to the usual vs split forms over ℝ and many other bases.
         */
        fun <A : Any> withSigma(
            base: NonAssociativeInvolutiveRing<A>,
            sigma: A
        ): CayleyDickson<A> = CayleyDickson(
            base = base,
            sigma = sigma
        )

        fun <A: Any> usual(
            base: NonAssociativeInvolutiveRing<A>,
        ): CayleyDickson<A> = withSigma(base, base.mul.identity)

        fun <A: Any> split(
            base: NonAssociativeInvolutiveRing<A>
        ): CayleyDickson<A> {
            val negOne = base.add.inverse(base.mul.identity)
            return withSigma(base, negOne)
        }
    }
}
