package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.structures.instances.Real
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

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
interface InvolutiveRing<A : Any> : Ring<A> {
    /**
     * The involution of the ring, x ↦ x*.
     */
    val conj: Endo<A>
}


/**
 * We get the scalar ring, CommutativeRing<R> from the Algebra<R, A>.
 * We get the ring on A and conj: Endo<A> from InvolutiveRing<A>.
 */
interface StarAlgebra<R : Any, A : Any> : Algebra<R, A>, InvolutiveRing<A>

