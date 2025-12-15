package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Action

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
    val leftAction: Action<R, M>

    companion object {
        fun <R: Any, M: Any> of(
            leftScalars: Ring<R>,
            group: AbelianGroup<M>,
            leftAction: Action<R, M>,
        ): LeftRModule<R, M> = object : LeftRModule<R, M> {
            override val leftScalars = leftScalars
            override val group = group
            override val leftAction = leftAction
        }
    }
}

/**
 * A Right R-Module is a (noncommutative) [Ring] R acting on an [AbelianGroup] from the right.
 *
 * Laws:
 *
 *    (x + y) ⊲ r = x ⊲ r + y ⊲ r
 *    x ⊲ (r + s) = x ⊲ r + x ⊲ s
 *    x ⊲ (r · s) = (x ⊲ r) ⊲ s
 *    x ⊲ 1 = x
 */
interface RightRModule<R : Any, M : Any> {
    val rightScalars: Ring<R>
    val group: AbelianGroup<M>
    val rightAction: Action<R, M>

    companion object {
        fun <R: Any, M: Any> of(
            rightScalars: Ring<R>,
            group: AbelianGroup<M>,
            rightAction: Action<R, M>,
        ): RightRModule<R, M> = object : RightRModule<R, M> {
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
interface RSBiModule<R : Any, S : Any, M : Any> : LeftRModule<R, M>, RightRModule<S, M> {
    companion object {
        fun <R: Any, S: Any, M: Any> of(
            leftScalars: Ring<R>,
            rightScalars: Ring<S>,
            group: AbelianGroup<M>,
            leftAction: Action<R, M>,
            rightAction: Action<S, M>
        ): RSBiModule<R, S, M> = object : RSBiModule<R, S, M> {
            override val leftScalars = leftScalars
            override val rightScalars = rightScalars
            override val group = group
            override val leftAction = leftAction
            override val rightAction = rightAction
        }
    }
}

/**
 * A [ModuleCore] captures the commutative case: a single scalar action suffices
 * to define both left and right module structures.
 */
interface ModuleCore<R : Any, M : Any> {
    val scalars: CommutativeRing<R>
    val group: AbelianGroup<M>
    val action: Action<R, M>
}

/**
 * An R-Module is a module over a [CommutativeRing] R.
 * It is both a [LeftRModule] and [RightRModule], with the actions coinciding.
 */
interface RModule<R : Any, M : Any> : LeftRModule<R, M>, RightRModule<R, M>, ModuleCore<R, M> {
    override val scalars: CommutativeRing<R>

    override val leftScalars: Ring<R>
        get() = scalars

    override val rightScalars: Ring<R>
        get() = scalars

    override val leftAction: Action<R, M>
        get() = action

    override val rightAction: Action<R, M>
        get() = action

    companion object {
        fun <R : Any, M : Any> of(
            scalars: CommutativeRing<R>,
            group: AbelianGroup<M>,
            action: Action<R, M>
        ): RModule<R, M> = object : RModule<R, M> {
            override val scalars = scalars
            override val group = group
            override val action = action
        }
    }
}

/**
 * Helper function for RightRModules to allow the scalars to act from the right instead of the left,
 * as is the convention with [Action].
 */
fun <R : Any, M : Any> RightRModule<R, M>.actRight(m: M, r: R): M =
    rightAction(r, m)

/**
 * Helper function for LeftRModules to clarify that scalars act from the left.
 *
 * This is mainly just to mirror [actRight].
 */
fun <R : Any, M : Any> LeftRModule<R, M>.actLeft(r: R, m: M): M =
    leftAction(r, m)
