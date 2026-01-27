package org.vorpal.kosmos.algebra.extensions

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction

object DualModules {
    /**
     * Given a base vector space V over a field F and the dual ring Dual(F),
     * build the scalar-extension module:
     *
     *
     *
     *    V ⊗_F Dual(F)  ≅  V × V
     *
     * with action:
     *
     *
     *
     *    (a + bε) · (x, y) = (a·x, a·y + b·x).
     *
     * Intuition:
     *
     *
     *
     *    (x, y) corresponds to x + εy.
     *
     * Thus, this module gives the structure behind the formula:
     *
     *
     *    f(p + εv) = f(p) + D_vf(p)
     *
     * where `p + εv` is represented by `(p, v)`.
     */
    fun <F : Any, V : Any> over(
        base: VectorSpace<F, V>,
        dual: DualRing<F>,
    ): RModule<Dual<F>, Pair<V, V>> =
        object : RModule<Dual<F>, Pair<V, V>> {

            override val scalars: CommutativeRing<Dual<F>> = dual

            private val zeroV = base.add.identity

            override val add: AbelianGroup<Pair<V, V>> = AbelianGroup.of(
                identity = zeroV to zeroV,
                op = BinOp(Symbols.PLUS) { (x1, x2), (y1, y2) ->
                    base.add(x1, y1) to base.add(x2, y2)
                },
                inverse = Endo(Symbols.MINUS) { (x, y) ->
                    base.add.inverse(x) to base.add.inverse(y)
                }
            )

            override val leftAction: LeftAction<Dual<F>, Pair<V, V>> = LeftAction(Symbols.TRIANGLE_RIGHT) { s, (x, y) ->
                val a = s.a
                val b = s.b

                val ax = base.leftAction(a, x)
                val ay = base.leftAction(a, y)
                val bx = base.leftAction(b, x)

                ax to base.add(ay, bx)
            }
        }

    /** Pack a point and a tangent into the extended carrier: p + εv ≡ (p, v). */
    fun <V : Any> pack(p: V, v: V): Pair<V, V> =
        p to v

    /** Convenience: zero tangent at p. */
    fun <V : Any> atPoint(p: V, zero: V): Pair<V, V> =
        p to zero
}
