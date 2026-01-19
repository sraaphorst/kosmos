package org.vorpal.kosmos.analysis.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.Covector
import org.vorpal.kosmos.analysis.CovectorField
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction

object CovectorFieldAlgebras {

    private fun <F : Any, V : Any> requireSameSpace(
        actual: VectorSpace<F, V>,
        expected: VectorSpace<F, V>
    ) {
        require(actual === expected) { "CovectorFields must be over the expected VectorSpace instance." }
    }

    /**
     * [CovectorField]s over the same [VectorSpace] form an additive [AbelianGroup].
     */
    fun <F : Any, V : Any> abelianGroup(
        space: VectorSpace<F, V>
    ) : AbelianGroup<CovectorField<F, V>> = AbelianGroup.of(
        identity = CovectorField.zero(space),
        op = BinOp(Symbols.PLUS) { cvf1, cvf2 ->
            requireSameSpace(cvf1.space, space)
            requireSameSpace(cvf2.space, space)
            CovectorField.of(space) { p ->
                val cv1 = cvf1(p)
                val cv2 = cvf2(p)
                requireSameSpace(cv1.space, space)
                requireSameSpace(cv2.space, space)
                Covector.of(space) { v ->
                    space.field.add(cv1(v), cv2(v))
                }
            }
        },
        inverse = Endo(Symbols.MINUS) { cvf ->
            requireSameSpace(cvf.space, space)
            CovectorField.of(space) { p ->
                val cv = cvf(p)
                requireSameSpace(cv.space, space)
                Covector.of(space) { v ->
                    space.field.add.inverse(cv(v))
                }
            }
        }
    )

    /**
     * We get an [RModule] structure with [ScalarFields] acting on [CovectorField]s:
     * ```
     * (f ⊳ ω)(p)(v) = f(p) * ω(p)(v)
     * ```
     */
    fun <F: Any, V : Any> module(
        space: VectorSpace<F, V>
    ): RModule<ScalarField<F, V>, CovectorField<F, V>> = RModule.of(
        scalars = ScalarFieldAlgebras.commutativeRing(space),
        add = abelianGroup(space),
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { sf, cvf ->
            require(sf.space === space) { "ScalarField must be over the expected VectorSpace instance." }
            requireSameSpace(cvf.space, space)
            CovectorField.of(space) { p ->
                val cv = cvf(p)
                requireSameSpace(cv.space, space)
                Covector.of(space) { v ->
                    space.field.mul(sf(p), cv(v))
                }
            }
        }
    )
}
