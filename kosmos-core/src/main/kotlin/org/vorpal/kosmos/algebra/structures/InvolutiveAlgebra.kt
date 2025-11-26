package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

interface InvolutiveAlgebra<A : Any> : NonAssociativeAlgebra<A> {
    /**
     * The involution operator of the algebra, x ↦ x*.
     */
    val conj: Endo<A>
}
