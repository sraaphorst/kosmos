package org.vorpal.kosmos.combinatorics.instances

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.algebra.structures.Subgroup
import org.vorpal.kosmos.algebra.structures.subgroup
import org.vorpal.kosmos.combinatorics.Permutation
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

object PermutationAlgebras {
    fun <A : Any> symmetricGroup(domain: FiniteSet<A>): Group<Permutation<A>> = Group.of(
        identity = Permutation.identity(domain),
        op = BinOp(Symbols.OPEN_CIRCLE) { x, y -> x andThen y},
        inverse = Endo(Symbols.INVERSE, Permutation<A>::inverse)
    )

    fun <A : Any> alternatingSubgroup(domain: FiniteSet<A>): Subgroup<Permutation<A>> =
        symmetricGroup(domain).subgroup { it.sign() == 1 }

    fun symmetricGroup(n: Int): Group<Permutation<Int>> {
        require(n >= 0) { "Group must be non-negative: $n" }
        return symmetricGroup(FiniteSet.ordered(0 until n))
    }

    fun alternatingSubgroup(n: Int): Subgroup<Permutation<Int>> =
        symmetricGroup(n).subgroup { it.sign() == 1 }
}
