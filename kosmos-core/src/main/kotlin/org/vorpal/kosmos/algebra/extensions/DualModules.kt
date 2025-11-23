// DualModules.kt
package org.vorpal.kosmos.algebra.extensions

import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

typealias DualOf<F> = DualRing<F>.Dual

object DualModules {
    /**
     * Given a base vector space `V` over a field F and the dual ring [Dual]<F>,
     * build the scalar-extension module:
     *
     * `(V ⊗_F Dual<F>) ≅ V × V`
     *
     * with action:
     *
     * `(a + bε) · (x, y) = (a x, a y + b x)`.
     *
     * We don't use [RModule.of] because we want to extend the RModule with [pack] and [atPoint]
     * without making them extension functions.
     */
    fun <F: Any, V: Any> over(
        base: VectorSpace<F, V>,
        dual: DualRing<F>
    ): RModule<DualOf<F>, Pair<V, V>> = object : RModule<DualOf<F>, Pair<V, V>> {

        // Dual<F> is a commutative *ring*, not a field → RModule, not VectorSpace.
        override val ring: CommutativeRing<DualOf<F>> = dual

        override val group: AbelianGroup<Pair<V, V>> = AbelianGroup.of(
            identity = base.group.identity to base.group.identity,
            op = BinOp(Symbols.PLUS) { (x1, x2), (y1, y2) ->
                base.group.op(x1, x2) to base.group.op(y1, y2)
            },
            inverse = Endo(Symbols.INVERSE) { (x, y) -> base.group.inverse(x) to base.group.inverse(y) }
        )

        override val action: Action<DualOf<F>, Pair<V, V>> =
            Action(Symbols.TRIANGLE_LEFT) { s, xy ->
                val (a, b) = s      // s = a + bε
                val (x, y) = xy     // (x, y) ≅ x + ε y
                val ax = base.action(a, x)
                val ay = base.action(a, y)
                val bx = base.action(b, x)
                ax to base.group.op(ay, bx) // (a x, a y + b x)
            }
    }

    /** Pack a point and a tangent into the scalar-extended carrier: p + ε v ≡ (p, v). */
    fun <V: Any> pack(p: V, v: V): Pair<V, V> = p to v

    /** Convenience: zero tangent at p. */
    fun <V: Any> atPoint(p: V, zero: V): Pair<V, V> = p to zero
}
