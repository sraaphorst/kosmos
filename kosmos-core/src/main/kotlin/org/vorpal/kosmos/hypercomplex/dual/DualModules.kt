package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable

data class DualVector<V : Any>(
    val primal: V,
    val tangent: V
)

fun <V : Any> eqDualVector(eqV: Eq<V>): Eq<DualVector<V>> =
    Eq { x, y -> eqV(x.primal, y.primal) && eqV(x.tangent, y.tangent) }

fun <V : Any> printableDualVector(prV: Printable<V>): Printable<DualVector<V>> =
    Printable { v -> "DualVector(primal=${prV(v.primal)}, tangent=${prV(v.tangent)})"}


object DualModules {
    /**
     * Given a base vector space `V` over a field `F` and the dual ring `Dual(F)`,
     * build the scalar-extension module:
     * ```text
     * V ⊗_F Dual(F) = V ⊗_F F[ε]/(ε²) ≅ V × V
     * ```
     * with action:
     * ```text
     * (a + bε)(x, y) = (ax, ay + bx).
     * ```
     * Intuition:
     * ```text
     * (x, y) corresponds to x + εy.
     * ```
     * Thus, this module is the algebraic setting behind dual-number directional
     * derivatives, where `p + εv` is represented by `(p, v)`.
     *
     * For suitable differentiable maps, this supports the familiar expression:
     * ```text
     * f(p + εv) = f(p) + ε D_v f(p).
     * ```
     */
    fun <F : Any, V : Any> scalarExtension(
        base: VectorSpace<F, V>,
        dual: DualAlgebras.DualCommutativeRing<F>,
    ): RModule<Dual<F>, DualVector<V>> =
        object : RModule<Dual<F>, DualVector<V>> {

            override val scalars: CommutativeRing<Dual<F>> = dual

            private val zeroV = base.add.identity

            override val add: AbelianGroup<DualVector<V>> = AbelianGroup.of(
                identity = DualVector(
                    primal = zeroV,
                    tangent = zeroV
                ),
                op = BinOp(Symbols.PLUS) { (primal1, tangent1), (primal2, tangent2) ->
                    DualVector(
                        primal = base.add(primal1, primal2),
                        tangent = base.add(tangent1, tangent2)
                    )
                },
                inverse = Endo(Symbols.MINUS) { (primal, tangent) ->
                    DualVector(
                        primal = base.add.inverse(primal),
                        tangent = base.add.inverse(tangent)
                    )
                }
            )

            override val leftAction: LeftAction<Dual<F>, DualVector<V>> =
                LeftAction(Symbols.TRIANGLE_RIGHT) { s, (primal, tangent) ->
                    val a = s.f
                    val b = s.df

                    val ax = base.leftAction(a, primal)
                    val ay = base.leftAction(a, tangent)
                    val bx = base.leftAction(b, primal)

                    DualVector(
                        primal = ax,
                        tangent = base.add(ay, bx)
                    )
                }
        }
}
