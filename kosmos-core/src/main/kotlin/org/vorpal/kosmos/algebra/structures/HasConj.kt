package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

/**
 * A structure that has an involutive conjugate function:
 *
 * The involution operator of the algebra, `x ↦ x*`.
 */
interface HasConj<A : Any> {
    val conj: Endo<A>
}
