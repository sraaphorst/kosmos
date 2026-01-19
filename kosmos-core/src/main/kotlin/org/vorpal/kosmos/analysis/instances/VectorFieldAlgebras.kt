package org.vorpal.kosmos.analysis.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.analysis.VectorField
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction

object VectorFieldAlgebras {

    private fun <F : Any, V : Any> requireSameSpace(
        actual: VectorSpace<F, V>,
        expected: VectorSpace<F, V>,
    ) {
        require(actual === expected) { "Field must be over the expected VectorSpace instance." }
    }

    /**
     * Abelian group structure on VectorField<F, V> with vector +.
     *
     * - zero: p ↦ 0
     * - (f + g)(p) = f(p) + g(p)
     * - (-f)(p) = -f(p)
     */
    fun <F : Any, V : Any> abelianGroup(
        space: VectorSpace<F, V>
    ): AbelianGroup<VectorField<F, V>> = AbelianGroup.of(
        identity = VectorField.zero(space),
        op = BinOp(Symbols.PLUS) { vf1, vf2 ->
            requireSameSpace(vf1.space, space)
            requireSameSpace(vf2.space, space)
            VectorField.of(space) { p -> space.add(vf1(p), vf2(p)) }
        },
        inverse = Endo(Symbols.MINUS) { vf ->
            requireSameSpace(vf.space, space)
            VectorField.of(space) { p -> space.add.inverse(vf(p)) }
        }
    )

    /**
     * R-Module structure with ScalarFields acting on the VectorField abelian group.
     */
    fun <F : Any, V : Any> module(
        space: VectorSpace<F, V>
    ): RModule<ScalarField<F, V>, VectorField<F, V>> = RModule.of(
        scalars = ScalarFieldAlgebras.commutativeRing(space),
        add = abelianGroup(space),
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { sf, vf ->
            requireSameSpace(sf.space, space)
            requireSameSpace(vf.space, space)
            VectorField.of(space) { p -> space.leftAction(sf(p), vf(p)) }
        }
    )
}
