package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.LeftRModule
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.RSBiModule
import org.vorpal.kosmos.algebra.structures.RightRModule
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.RightAction
import org.vorpal.kosmos.core.ops.pairLeftAction
import org.vorpal.kosmos.core.ops.pairRightAction

object LeftRModules {
    fun <R : Any, M : Any, N : Any> product(
        left: LeftRModule<R, M>,
        right: LeftRModule<R, N>
    ): LeftRModule<R, Pair<M, N>> = object : LeftRModule<R, Pair<M, N>> {
        init {
            require(left.leftScalars === right.leftScalars) { "Left scalars must be the same for module product" }
        }
        override val leftScalars: Ring<R> = left.leftScalars
        override val add: AbelianGroup<Pair<M, N>> = AbelianGroups.product(left.add, right.add)
        override val leftAction: LeftAction<R, Pair<M, N>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <R : Any, M : Any> double(
        obj: LeftRModule<R, M>
    ): LeftRModule<R, Pair<M, M>> = product(obj, obj)
}

object RightRModules {
    fun <M : Any, N : Any, S : Any> product(
        left: RightRModule<M, S>,
        right: RightRModule<N, S>
    ): RightRModule<Pair<M, N>, S> = object : RightRModule<Pair<M, N>, S> {
        init {
            require(left.rightScalars === right.rightScalars) { "Right scalars must be the same for module product" }
        }
        override val rightScalars: Ring<S> = left.rightScalars
        override val add: AbelianGroup<Pair<M, N>> = AbelianGroups.product(left.add, right.add)
        override val rightAction: RightAction<Pair<M, N>, S> = pairRightAction(left.rightAction, right.rightAction)
    }

    fun <M : Any, S : Any> double(
        obj: RightRModule<M, S>
    ): RightRModule<Pair<M, M>, S> = product(obj, obj)
}

object RSBiModules {
    fun <R : Any, M : Any, N : Any, S : Any> product(
        left: RSBiModule<R, M, S>,
        right: RSBiModule<R, N, S>
    ): RSBiModule<R, Pair<M, N>, S> = object : RSBiModule<R, Pair<M, N>, S> {
        init {
            require(left.leftScalars === right.leftScalars) { "Left scalars must be the same for module product" }
            require(left.rightScalars === right.rightScalars) { "Right scalars must be the same for module product" }
        }
        override val leftScalars: Ring<R> = left.leftScalars
        override val rightScalars: Ring<S> = left.rightScalars
        override val add: AbelianGroup<Pair<M, N>> = AbelianGroups.product(left.add, right.add)
        override val leftAction: LeftAction<R, Pair<M, N>> = pairLeftAction(left.leftAction, right.leftAction)
        override val rightAction: RightAction<Pair<M, N>, S> = pairRightAction(left.rightAction, right.rightAction)
    }

    fun <R : Any, M : Any, S : Any> double(
        obj: RSBiModule<R, M, S>
    ): RSBiModule<R, Pair<M, M>, S> = product(obj, obj)
}

object RModules {
    fun <R : Any, M : Any, N : Any> product(
        left: RModule<R, M>,
        right: RModule<R, N>
    ): RModule<R, Pair<M, N>> = object : RModule<R, Pair<M, N>> {
        init {
            require(left.scalars === right.scalars) { "Scalars must be the same for module product" }
        }
        override val scalars: CommutativeRing<R> = left.scalars
        override val add: AbelianGroup<Pair<M, N>> = AbelianGroups.product(left.add, right.add)
        override val leftAction: LeftAction<R, Pair<M, N>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <R : Any, M : Any> double(
        obj: RModule<R, M>
    ): RModule<R, Pair<M, M>> = product(obj, obj)
}
