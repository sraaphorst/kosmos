package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.algebra.structures.Subgroup
import org.vorpal.kosmos.algebra.structures.subgroup
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.linear.values.PermMat

object PermMatAlgebras {
    /**
     * Create a group of `n×n` [PermMat] representing the (compressed) matrix representation of the `S_n`.
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
     * Create the subgroup of `n×n` [PermMat] representing the (compressed) matrix subgroup `A_n` of `S_n`.
     */
    fun alternatingSubgroup(n: Int): Subgroup<PermMat> =
        symmetricGroup(n).subgroup(PermMat::isEven)
}
