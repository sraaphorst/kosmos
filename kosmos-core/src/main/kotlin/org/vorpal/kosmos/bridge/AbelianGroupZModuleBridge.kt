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
     */
    fun <M : Any> AbelianGroup<M>.toZModule(): ZModule<M> {
        val action: LeftAction<BigInteger, M> = LeftAction(::zTimes)
        return RModule.of(IntegerAlgebras.ZCommutativeRing, this, action)
    }
}
