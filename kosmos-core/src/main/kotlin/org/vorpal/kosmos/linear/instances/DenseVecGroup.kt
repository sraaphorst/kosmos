package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Dimensionality
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

/**
 * Build the additive abelian group structure on A^n (dense coordinate vectors),
 * given an additive abelian group structure on A.
 */
class DenseVecGroup<A : Any>(
    val baseGroup: AbelianGroup<A>,
    override val dimension: Int
): AbelianGroup<DenseVec<A>>, Dimensionality {
    init {
        DenseKernel.checkNonnegative(dimension)
    }

    override val identity: DenseVec<A> = DenseVec.tabulate(dimension) { baseGroup.identity }

    override val op: BinOp<DenseVec<A>> = BinOp(Symbols.PLUS) { u, v ->
        DenseKernel.requireSize(u.size, dimension)
        DenseKernel.requireSize(v.size, dimension)
        u.zipWith(v, baseGroup::invoke)
    }

    override val inverse: Endo<DenseVec<A>> = Endo(Symbols.MINUS) { v: DenseVec<A> ->
        DenseKernel.requireSize(v.size, dimension)
        v.map(baseGroup.inverse::invoke)
    }
}
