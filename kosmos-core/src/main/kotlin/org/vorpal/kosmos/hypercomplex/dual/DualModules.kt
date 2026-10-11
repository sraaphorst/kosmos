package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable

/**
 * An element `primal + ε·tangent` of the scalar extension `V ⊗ Dual(R)` of a module `V`: a point
 * together with a tangent vector at it.
 */
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
     * Given a module `V` over a commutative ring `R` (in particular a vector space over a field),
     * build the scalar-extension module over the dual ring `Dual(R)`:
     * ```text
     * V ⊗_R Dual(R) = V ⊗_R R[ε]/(ε²) ≅ V × V
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
     *
     * The scalars are the dual ring over the scalars of [base], so the two can never disagree.
     */
    fun <R : Any, V : Any> scalarExtension(
        base: RModule<R, V>
    ): RModule<Dual<R>, DualVector<V>> =
        object : RModule<Dual<R>, DualVector<V>> {

            override val scalars: CommutativeRing<Dual<R>> = DualAlgebras.dualRing(base.scalars)

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

            override val leftAction: LeftAction<Dual<R>, DualVector<V>> =
                LeftAction(Symbols.TRIANGLE_RIGHT) { s, (primal, tangent) ->
                    val (a, b) = s

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
