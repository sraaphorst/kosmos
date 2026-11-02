package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.algebra.structures.Subgroup
import org.vorpal.kosmos.algebra.structures.subgroup
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.linear.values.PermMat

/**
 * The symmetric and alternating groups as compact [PermMat]s.
 *
 * The group operation is [PermMat.andThen], so that the product of `p` and `q` applies `p` first.
 * For the same groups as full matrices over a semiring, see [PermutationMatrixAlgebras].
 */
object PermMatAlgebras {
    /**
     * The symmetric group `S_n` as `n×n` [PermMat]s, under [PermMat.andThen].
     *
     * @throws IllegalArgumentException if [n] is negative
     */
    fun symmetricGroup(n: Int): Group<PermMat> {
        require(n >= 0) { "n must be nonnegative: $n" }

        return Group.of(
            identity = PermMat.identity(n),
            op = BinOp(Symbols.OPEN_CIRCLE, PermMat::andThen),
            inverse = Endo(Symbols.INVERSE, PermMat::inverse)
        )
    }

    /**
     * The alternating group `A_n`, the even permutations, as a subgroup of [symmetricGroup].
     */
    fun alternatingSubgroup(n: Int): Subgroup<PermMat> =
        symmetricGroup(n).subgroup(PermMat::isEven)
}
