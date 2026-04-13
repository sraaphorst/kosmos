package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.pairLeftAction

object VectorSpaces {
    fun <F : Any, V : Any, W : Any> product(
        left: VectorSpace<F, V>,
        right: VectorSpace<F, W>
    ): VectorSpace<F, Pair<V, W>> = object : VectorSpace<F, Pair<V, W>> {
        init {
            require(left.scalars === right.scalars) { "Scalars must be the same for vector space product" }
        }
        override val scalars: Field<F> = left.scalars
        override val add: AbelianGroup<Pair<V, W>> = AbelianGroups.product(left.add, right.add)
        override val leftAction: LeftAction<F, Pair<V, W>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <F : Any, V : Any> double(
        obj: VectorSpace<F, V>
    ): VectorSpace<F, Pair<V, V>> = product(obj, obj)
}

object FiniteVectorSpaces {
    fun <F : Any, V : Any, W : Any> product(
        left: FiniteVectorSpace<F, V>,
        right: FiniteVectorSpace<F, W>
    ): FiniteVectorSpace<F, Pair<V, W>> = object : FiniteVectorSpace<F, Pair<V, W>> {
        init {
            require(left.scalars === right.scalars) { "Scalars must be the same for finite vector space product" }
        }
        override val scalars: Field<F> = left.scalars
        override val add: AbelianGroup<Pair<V, W>> = AbelianGroups.product(left.add, right.add)
        override val leftAction: LeftAction<F, Pair<V, W>> = pairLeftAction(left.leftAction, right.leftAction)
        override val dimension: Int = left.dimension + right.dimension
    }

    fun <F : Any, V : Any> double(
        obj: FiniteVectorSpace<F, V>
    ): FiniteVectorSpace<F, Pair<V, V>> = product(obj, obj)
}
