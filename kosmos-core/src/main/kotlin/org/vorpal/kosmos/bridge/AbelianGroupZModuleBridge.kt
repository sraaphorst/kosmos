package org.vorpal.kosmos.bridge

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.zTimes
import org.vorpal.kosmos.core.ops.LeftAction
import java.math.BigInteger

typealias ZModule<M> = RModule<BigInteger, M>

object AbelianGroupZModuleBridge {
    /**
     * Forgetful: any Z-module has an underlying additive abelian group.
     */
    fun <M : Any> ZModule<M>.toAbelianGroup(): AbelianGroup<M> =
        add

    /**
     * Canonical: an abelian group gives a Z-module structure.
     *
     * Note that the [ZModule] structure returned here likely does not use the most efficient means of
     * implementing the scalar multiplication operation: the action relies on calculating the image of the scalar
     * in the group by doubling the group identity until the effect of multiplying by the scalar is achieved.
     */
    fun <M : Any> AbelianGroup<M>.toZModule(): ZModule<M> {
        val action: LeftAction<BigInteger, M> = LeftAction(::zTimes)
        return RModule.of(IntegerAlgebras.IntegerCommutativeRing, this, action)
    }
}
