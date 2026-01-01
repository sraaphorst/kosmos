package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.analysis.VectorField
import org.vorpal.kosmos.analysis.VectorFields
import org.vorpal.kosmos.analysis.plus
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction

typealias VectorFieldModule<F, V> = RModule<ScalarField<F, V>, VectorField<F, V>>

object VectorFieldAlgebra {
    infix fun <F: Any, V: Any> ScalarField<F, V>.actOn(vf: VectorField<F, V>): VectorField<F, V> =
        VectorFields.of(vf.space) { p -> vf.space.leftAction(this(p), vf(p)) }

    /**
     * Given a [VectorSpace] `V` over a [Field] `F`, this defines the
     * additive abelian group of [VectorField]s `(V^V, +)` under pointwise addition.
     *
     * We have an additive [AbelianGroup] over `V` in the [VectorSpace] over `<F, V>` that we extend
     * to an additive [AbelianGroup] over [VectorSpace] `<F, V>`.
     */
    fun <F: Any, V: Any> additiveAbelianGroup(
        space: VectorSpace<F, V>
    ): AbelianGroup<VectorField<F, V>> = AbelianGroup.of(
        identity = VectorFields.zero(space),
        op = BinOp(Symbols.PLUS) { vf1, vf2 -> vf1 + vf2 },
        inverse = Endo(Symbols.MINUS){ vf ->
            VectorFields.of(space) {
                space.group.inverse(vf(it))
            }
        }
    )

    /**
     * Now we define an action of the commutative ring of [ScalarField]<F, V>
     * over the elements of the [AbelianGroup] of [VectorField]<F, V>
     * to get our module:
     * 𝒞^∞(M) (ring of functions) → 𝔛(M) (module of vector fields).
     */
    fun <F: Any, V: Any> module(
        space: VectorSpace<F, V>
    ): RModule<ScalarField<F, V>, VectorField<F, V>> = RModule.of(
        scalars = ScalarFieldAlgebra.commutativeRing(space),
        group = additiveAbelianGroup(space),
        leftAction = LeftAction { sf, vf -> sf actOn vf }
    )
}
