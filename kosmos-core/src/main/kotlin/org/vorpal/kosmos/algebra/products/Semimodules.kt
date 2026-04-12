package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.algebra.structures.LeftRSemimodule
import org.vorpal.kosmos.algebra.structures.RSBiSemimodule
import org.vorpal.kosmos.algebra.structures.RightRSemimodule
import org.vorpal.kosmos.algebra.structures.Semimodule
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.RightAction
import org.vorpal.kosmos.core.ops.pairLeftAction
import org.vorpal.kosmos.core.ops.pairRightAction

object LeftRSemimodules {
    fun <R : Any, M : Any, N : Any> product(
        left: LeftRSemimodule<R, M>,
        right: LeftRSemimodule<R, N>
    ): LeftRSemimodule<R, Pair<M, N>> = object : LeftRSemimodule<R, Pair<M, N>> {
        init {
            require(left.leftScalars === right.leftScalars) { "Left scalars must be the same for semimodule product" }
        }
        override val leftScalars: Semiring<R> = left.leftScalars
        override val add: CommutativeMonoid<Pair<M, N>> = CommutativeMonoids.product(left.add, right.add)
        override val leftAction: LeftAction<R, Pair<M, N>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <R : Any, M : Any> double(
        obj: LeftRSemimodule<R, M>
    ): LeftRSemimodule<R, Pair<M, M>> = product(obj, obj)
}

object RightRSemimodules {
    fun <M : Any, N : Any, S : Any> product(
        left: RightRSemimodule<M, S>,
        right: RightRSemimodule<N, S>
    ): RightRSemimodule<Pair<M, N>, S> = object : RightRSemimodule<Pair<M, N>, S> {
        init {
            require(left.rightScalars === right.rightScalars) { "Right scalars must be the same for semimodule product" }
        }
        override val rightScalars: Semiring<S> = left.rightScalars
        override val add: CommutativeMonoid<Pair<M, N>> = CommutativeMonoids.product(left.add, right.add)
        override val rightAction: RightAction<Pair<M, N>, S> = pairRightAction(left.rightAction, right.rightAction)
    }

    fun <M : Any, S : Any> double(
        obj: RightRSemimodule<M, S>
    ): RightRSemimodule<Pair<M, M>, S> = product(obj, obj)
}

object RSBiSemimodules {
    fun <R : Any, M : Any, N : Any, S : Any> product(
        left: RSBiSemimodule<R, M, S>,
        right: RSBiSemimodule<R, N, S>
    ): RSBiSemimodule<R, Pair<M, N>, S> = object : RSBiSemimodule<R, Pair<M, N>, S> {
        init {
            require(left.leftScalars === right.leftScalars) { "Left scalars must be the same for semimodule product" }
            require(left.rightScalars === right.rightScalars) { "Right scalars must be the same for semimodule product" }
        }
        override val leftScalars: Semiring<R> = left.leftScalars
        override val rightScalars: Semiring<S> = left.rightScalars
        override val add: CommutativeMonoid<Pair<M, N>> = CommutativeMonoids.product(left.add, right.add)
        override val leftAction: LeftAction<R, Pair<M, N>> = pairLeftAction(left.leftAction, right.leftAction)
        override val rightAction: RightAction<Pair<M, N>, S> = pairRightAction(left.rightAction, right.rightAction)
    }

    fun <R : Any, M : Any, S : Any> double(
        obj: RSBiSemimodule<R, M, S>
    ): RSBiSemimodule<R, Pair<M, M>, S> = product(obj, obj)
}

object Semimodules {
    fun <R : Any, M : Any, N : Any> product(
        left: Semimodule<R, M>,
        right: Semimodule<R, N>
    ): Semimodule<R, Pair<M, N>> = object : Semimodule<R, Pair<M, N>> {
        init {
            require(left.scalars === right.scalars) { "Scalars must be the same for semimodule product" }
        }
        override val scalars: CommutativeSemiring<R> = left.scalars
        override val add: CommutativeMonoid<Pair<M, N>> = CommutativeMonoids.product(left.add, right.add)
        override val leftAction: LeftAction<R, Pair<M, N>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <R : Any, M : Any> double(
        obj: Semimodule<R, M>
    ): Semimodule<R, Pair<M, M>> = product(obj, obj)
}
