package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
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
interface LeftRModule<R: Any, M: Any> {
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
interface RightRModule<R: Any, M: Any> {
    val rightRing: Ring<R>
    val group: AbelianGroup<M>
    val rightAction: Action<R, M>
}

/**
 * An (R,S)-Bimodule is an abelian group M that is simultaneously a left R-Module
 * and a right S-Module, with the compatibility condition:
 *    (r ⊳ m) ⊲ s = r ⊳ (m ⊲ s)
 */
interface RSBiModule<R: Any, S: Any, M: Any> : LeftRModule<R, M>, RightRModule<S, M>

/**
 * A ModuleCore captures the commutative case: a single scalar action suffices
 * to define both left and right module structures.
 */
interface ModuleCore<R: Any, M: Any> {
    val ring: CommutativeRing<R>
    val group: AbelianGroup<M>
    val action: Action<R, M>
}

/**
 * An R-Module is a module over a commutative ring R.
 * It is both a LeftRModule and RightRModule, with the actions coinciding.
 */
interface RModule<R: Any, M: Any> : LeftRModule<R, M>, RightRModule<R, M>, ModuleCore<R, M> {
    override val ring: CommutativeRing<R>

    override val leftRing: Ring<R>
        get() = ring

    override val rightRing: Ring<R>
        get() = ring

    override val leftAction: Action<R, M>
        get() = action

    override val rightAction: Action<R, M>
        get() = action

    companion object {
        private const val DEFAULT_SYMBOL = Symbols.TRIANGLE_RIGHT

        fun <R: Any, M: Any> of(
            ring: CommutativeRing<R>,
            group: AbelianGroup<M>,
            symbol: String = DEFAULT_SYMBOL,
            action: (R, M) -> M,
        ): RModule<R, M> = object : RModule<R, M> {
            override val ring: CommutativeRing<R> = ring
            override val group: AbelianGroup<M> = group
            override val action: Action<R, M> = Action(symbol, action)
        }
    }
}
