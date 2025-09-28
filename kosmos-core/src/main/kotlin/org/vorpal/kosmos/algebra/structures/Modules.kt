package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Action

/**
 * A Left R-Module is a (noncommutative) Ring R acting on an AbelianGroup M from the left.
 *
 * Laws:
 *  - r ⊳ (x + y) = r ⊳ x + r ⊳ y
 *  - (r + s) ⊳ x = r ⊳ x + s ⊳ x
 *  - (r · s) ⊳ x = r ⊳ (s ⊳ x)
 *  - 1 ⊳ x = x
 */
interface LeftRModule<R, M> {
    val leftRing: Ring<R>
    val group: AbelianGroup<M>
    val leftAction: Action<R, M>
}

/**
 * A Right R-Module is a (noncommutative) Ring R acting on an AbelianGroup M from the right.
 *
 * Laws:
 *  - (x + y) ⊲ r = x ⊲ r + y ⊲ r
 *  - x ⊲ (r + s) = x ⊲ r + x ⊲ s
 *  - x ⊲ (r · s) = (x ⊲ r) ⊲ s
 *  - x ⊲ 1 = x
 */
interface RightRModule<R, M> {
    val rightRing: Ring<R>
    val group: AbelianGroup<M>
    val rightAction: Action<R, M>
}

/**
 * An (R,S)-Bimodule is an abelian group M that is simultaneously a left R-Module
 * and a right S-Module, with the compatibility condition:
 *    (r ⊳ m) ⊲ s = r ⊳ (m ⊲ s)
 */
interface RSBiModule<R, S, M> : LeftRModule<R, M>, RightRModule<S, M>

/**
 * A ModuleCore captures the commutative case: a single scalar action suffices
 * to define both left and right module structures.
 */
interface ModuleCore<R, M> {
    val ring: CommutativeRing<R>
    val group: AbelianGroup<M>
    val action: Action<R, M>
}

/**
 * An R-Module is a module over a commutative ring R.
 * It is both a LeftRModule and RightRModule, with the actions coinciding.
 */
interface RModule<R, M> : LeftRModule<R, M>, RightRModule<R, M>, ModuleCore<R, M> {
    override val ring: CommutativeRing<R>

    override val leftRing: Ring<R>
        get() = ring

    override val rightRing: Ring<R>
        get() = ring

    override val leftAction: Action<R, M>
        get() = action

    override val rightAction: Action<R, M>
        get() = action
}
