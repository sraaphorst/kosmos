package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.RightAction

// TODO: WRITE LAWS FOR THESE.
/**
 * A Left R-Semimodule is a (possibly noncommutative) [Semiring] R acting on a [CommutativeMonoid] M from the left.
 *
 * Laws (in addition to monoid laws on M and semiring laws on R):
 *
 *    r ⊳ (x + y) = r ⊳ x + r ⊳ y
 *    (r + s) ⊳ x = r ⊳ x + s ⊳ x
 *    (r · s) ⊳ x = r ⊳ (s ⊳ x)
 *    1 ⊳ x = x
 *
 * Usually also expected (and implied in many presentations):
 *
 *    0 ⊳ x = 0
 *    r ⊳ 0 = 0
 */
interface LeftRSemimodule<R : Any, M : Any> {
    val leftScalars: Semiring<R>
    val add: CommutativeMonoid<M>
    val leftAction: LeftAction<R, M>

    companion object {
        fun <R : Any, M : Any> of(
            leftScalars: Semiring<R>,
            add: CommutativeMonoid<M>,
            leftAction: LeftAction<R, M>,
        ): LeftRSemimodule<R, M> = object : LeftRSemimodule<R, M> {
            override val leftScalars = leftScalars
            override val add = add
            override val leftAction = leftAction
        }
    }
}

/**
 * A Right S-Semimodule is a (possibly noncommutative) [Semiring] S acting on a [CommutativeMonoid] M from the right.
 *
 * Laws:
 *
 *    (x + y) ⊲ s = x ⊲ s + y ⊲ s
 *    x ⊲ (r + s) = x ⊲ r + x ⊲ s
 *    x ⊲ (r · s) = (x ⊲ r) ⊲ s
 *    x ⊲ 1 = x
 *
 * Usually also expected:
 *
 *    x ⊲ 0 = 0
 *    0 ⊲ s = 0
 */
interface RightRSemimodule<M : Any, S : Any> {
    val rightScalars: Semiring<S>
    val add: CommutativeMonoid<M>
    val rightAction: RightAction<M, S>

    companion object {
        fun <M : Any, S : Any> of(
            rightScalars: Semiring<S>,
            add: CommutativeMonoid<M>,
            rightAction: RightAction<M, S>,
        ): RightRSemimodule<M, S> = object : RightRSemimodule<M, S> {
            override val rightScalars = rightScalars
            override val add = add
            override val rightAction = rightAction
        }
    }
}

/**
 * An (R,S)-bisemimodule is a [CommutativeMonoid] that is simultaneously a left R-semimodule
 * and a right S-semimodule, with compatibility:
 *
 *    (r ⊳ m) ⊲ s = r ⊳ (m ⊲ s)
 */
interface RSBiSemimodule<R : Any, M : Any, S : Any> :
    LeftRSemimodule<R, M>,
    RightRSemimodule<M, S> {

    companion object {
        fun <R : Any, M : Any, S : Any> of(
            leftScalars: Semiring<R>,
            rightScalars: Semiring<S>,
            add: CommutativeMonoid<M>,
            leftAction: LeftAction<R, M>,
            rightAction: RightAction<M, S>,
        ): RSBiSemimodule<R, M, S> = object : RSBiSemimodule<R, M, S> {
            override val leftScalars = leftScalars
            override val rightScalars = rightScalars
            override val add = add
            override val leftAction = leftAction
            override val rightAction = rightAction
        }
    }
}

/**
 * An R-semimodule is a semimodule over a commutative semiring R.
 *
 * We model it as a left semimodule; a canonical right action is derived by commutativity.
 */
interface Semimodule<R : Any, M : Any> :
    LeftRSemimodule<R, M>,
    RightRSemimodule<M, R> {

    val scalars: CommutativeSemiring<R>

    override val leftScalars: CommutativeSemiring<R>
        get() = scalars

    override val rightScalars: CommutativeSemiring<R>
        get() = scalars

    override val rightAction: RightAction<M, R>
        get() = leftAction.toRightAction()

    companion object {
        fun <R : Any, M : Any> of(
            scalars: CommutativeSemiring<R>,
            add: CommutativeMonoid<M>,
            leftAction: LeftAction<R, M>,
        ): Semimodule<R, M> = object : Semimodule<R, M> {
            override val scalars = scalars
            override val add = add
            override val leftAction = leftAction
        }
    }
}
