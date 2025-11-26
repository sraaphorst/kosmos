package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

interface DivisionRing<A : Any> : Ring<A>, HasReciprocal<A> {
    override val zero: A
        get() = add.identity

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: Monoid<A>,
            reciprocal: Endo<A>
        ): Field<A> = object : Field<A> {
            override val add: AbelianGroup<A> = add
            override val mul: Monoid<A> = mul
            override val reciprocal: Endo<A> = reciprocal
        }
    }
}
