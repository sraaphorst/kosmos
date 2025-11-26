package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

interface NonAssociativeAlgebra<A : Any> {
    val add: AbelianGroup<A>
    val mul: NonAssociativeMonoid<A>

    fun fromBigInt(n: BigInteger): A {
        tailrec fun aux(rem: BigInteger, acc: A): A =
            when (rem) {
                BigInteger.ZERO -> acc
                else -> aux(
                    rem - BigInteger.ONE,
                    add.op(acc, mul.identity)
                )
            }

        val pos = aux(n.abs(), add.identity)
        return if (n.signum() == -1) add.inverse(pos) else pos
    }
}

interface InvolutiveAlgebra<A : Any> : NonAssociativeAlgebra<A> {
    /**
     * The involution operator of the algebra, x ↦ x*.
     */
    val conj: Endo<A>
}

/**
 * A ring coupled with an involution referred to as conjugation, which has the following properties:
 * 1. `conj(conj(a)) = a`
 * 2. `conj(add(a, b)) == add(conj(a), conj(b))`
 * 3. `conj(mul(a, b)) == mul(conj(b), conj(a))`
 * 4. `conj(mul.identity) == mul.identity`
 *
 * More legibly:
 * 1. `(a*)* == a`
 * 2. `(a + b)* == a* + b*`
 * 3. `(ab)* == b*a*`
 * 4. `1* = 1`
 */
interface InvolutiveRing<A : Any> : InvolutiveAlgebra<A>, Ring<A>


/**
 * We get the scalar ring, CommutativeRing<R> from the Algebra<R, A>.
 * We get the ring on A and conj: Endo<A> from InvolutiveRing<A>.
 */
interface StarAlgebra<R : Any, A : Any> : Algebra<R, A>, InvolutiveRing<A>

