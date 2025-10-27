package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.analysis.VectorField
import org.vorpal.kosmos.analysis.VectorFields

object VectorFieldAlgebra {
    /**
     * Given a [VectorSpace] `V` over a [Field] `F`, this defines the
     * additive abelian group of [VectorField]s `(V^V, +)` under pointwise addition.
     *
     * We have an additive [AbelianGroup] over `V` in the [VectorSpace] over `<F, V>` that we extend
     * to an additive [AbelianGroup] over [VectorSpace] `<F, V>`.
     */
    fun <F, V> additiveAbelianGroup(
        space: VectorSpace<F, V>
    ): AbelianGroup<VectorField<F, V>> where F: Any, V: VectorSpace<F, V> =
        AbelianGroup.of(
            op = { vf1, vf2 ->
                VectorFields.of(space) { v ->
                    space.group.op(vf1(v), vf2(v))
                }
            },
            identity = VectorFields.zero(space),
            inverse = { vf ->
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
    fun <F, V> module(
        space: VectorSpace<F, V>
    ): RModule<ScalarField<F, V>, VectorField<F, V>> where F: Any, V: VectorSpace<F, V> =
        RModule.of(
            ring = ScalarFieldAlgebra.commutativeRing(space),
            group = additiveAbelianGroup(space),
            action = { sf, vf -> VectorFields.of(space) { p -> space.action(sf(p), vf(p) )} }
        )
}
