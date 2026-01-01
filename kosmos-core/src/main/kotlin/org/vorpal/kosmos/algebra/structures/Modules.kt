package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.RightAction

/**
 * A Left R-Module is a (noncommutative) [Ring] R acting on an [AbelianGroup] M from the left.
 *
 * Laws:
 *
 *    r ⊳ (x + y) = r ⊳ x + r ⊳ y
 *    (r + s) ⊳ x = r ⊳ x + s ⊳ x
 *    (r · s) ⊳ x = r ⊳ (s ⊳ x)
 *    1 ⊳ x = x
 */
interface LeftRModule<R : Any, M : Any> {
    val leftScalars: Ring<R>
    val group: AbelianGroup<M>
    val leftAction: LeftAction<R, M>

    companion object {
        fun <R : Any, M : Any> of(
            leftScalars: Ring<R>,
            group: AbelianGroup<M>,
            leftAction: LeftAction<R, M>,
        ): LeftRModule<R, M> = object : LeftRModule<R, M> {
            override val leftScalars = leftScalars
            override val group = group
            override val leftAction = leftAction
        }
    }
}

/**
 * A Right S-Module is a (noncommutative) [Ring] S acting on an [AbelianGroup] from the right.
 *
 * Laws:
 *
 *    (x + y) ⊲ s = x ⊲ s + y ⊲ s
 *    x ⊲ (r + s) = x ⊲ r + x ⊲ s
 *    x ⊲ (r · s) = (x ⊲ r) ⊲ s
 *    x ⊲ 1 = x
 */
interface RightRModule<M : Any, S : Any> {
    val rightScalars: Ring<S>
    val group: AbelianGroup<M>
    val rightAction: RightAction<M, S>

    companion object {
        fun <M : Any, S : Any> of(
            rightScalars: Ring<S>,
            group: AbelianGroup<M>,
            rightAction: RightAction<M, S>,
        ): RightRModule<M, S> = object : RightRModule<M, S> {
            override val rightScalars = rightScalars
            override val group = group
            override val rightAction = rightAction
        }
    }
}

/**
 * An (R,S)-Bimodule is an [AbelianGroup] that is simultaneously a left R-Module
 * and a right S-Module, with the compatibility condition:
 *
 *    (r ⊳ m) ⊲ s = r ⊳ (m ⊲ s)
 */
interface RSBiModule<R : Any, M : Any, S : Any> : LeftRModule<R, M>, RightRModule<M, S> {
    companion object {
        fun <R : Any, M : Any, S : Any> of(
            leftScalars: Ring<R>,
            rightScalars: Ring<S>,
            group: AbelianGroup<M>,
            leftAction: LeftAction<R, M>,
            rightAction: RightAction<M, S>
        ): RSBiModule<R, M, S> = object : RSBiModule<R, M, S> {
            override val leftScalars = leftScalars
            override val rightScalars = rightScalars
            override val group = group
            override val leftAction = leftAction
            override val rightAction = rightAction
        }
    }
}

/**
 * An R-Module is a module over a commutative ring R.
 *
 * We model it as a left module; a canonical right action is derived by commutativity.
 */
interface RModule<R : Any, M : Any> : LeftRModule<R, M>, RightRModule<M, R> {
    val scalars: CommutativeRing<R>

    override val leftScalars: Ring<R>
        get() = scalars

    override val rightScalars: Ring<R>
        get() = scalars

    override val rightAction: RightAction<M, R>
        get() = leftAction.toRightAction()

    companion object {
        fun <R : Any, M : Any> of(
            scalars: CommutativeRing<R>,
            group: AbelianGroup<M>,
            leftAction: LeftAction<R, M>
        ): RModule<R, M> = object : RModule<R, M> {
            override val scalars = scalars
            override val group = group
            override val leftAction = leftAction
        }
    }
}
