package org.vorpal.kosmos.analysis.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import java.math.BigInteger

/**
 * Algebraic structures on scalar fields f : V -> F, defined pointwise.
 *
 * Fixes a specific space (witness) so we can enforce "same space" invariants.
 */
object ScalarFieldAlgebras {

    private fun <F : Any, V : Any> requireSameSpace(
        sf: ScalarField<F, V>,
        space: VectorSpace<F, V>,
    ) {
        require(sf.space === space) { "ScalarField must be over the expected VectorSpace instance." }
    }

    /**
     * Commutative ring structure on ScalarField<F, V> with pointwise + and *.
     *
     * - zero: p ↦ 0
     * - one:  p ↦ 1
     * - (f + g)(p) = f(p) + g(p)
     * - (f * g)(p) = f(p) * g(p)
     * - (-f)(p) = -f(p)
     */
    fun <F : Any, V : Any> commutativeRing(
        space: VectorSpace<F, V>
    ): CommutativeRing<ScalarField<F, V>> = object : CommutativeRing<ScalarField<F, V>> {
        private val field = space.field

        override val add = AbelianGroup.of(
            identity = ScalarField.zero(space),
            op = BinOp(Symbols.PLUS) { sf1, sf2 ->
                requireSameSpace(sf1, space)
                requireSameSpace(sf2, space)
                ScalarField.of(space) { p ->
                    field.add(sf1(p), sf2(p))
                }
            },
            inverse = Endo(Symbols.MINUS) { sf ->
                requireSameSpace(sf, space)
                ScalarField.of(space) { p ->
                    field.add.inverse(sf(p))
                }
            }
        )

        override val mul = CommutativeMonoid.of(
            identity = ScalarField.one(space),
            op = BinOp(Symbols.ASTERISK) { sf1, sf2 ->
                requireSameSpace(sf1, space)
                requireSameSpace(sf2, space)
                ScalarField.of(space) { p ->
                    field.mul(sf1(p), sf2(p))
                }
            }
        )

        override fun fromBigInt(n: BigInteger): ScalarField<F, V> =
            ScalarField.constant(space, field.fromBigInt(n))
    }

    /**
     * F-algebra structure on scalar fields.
     *
     * Scalar action is by constant scaling:
     *   (a ⊳ f)(p) = a * f(p)
     */
    fun <F : Any, V : Any> algebra(
        space: VectorSpace<F, V>,
    ): Algebra<F, ScalarField<F, V>> = Algebra.of(
        scalars = space.field,
        algebraRing = commutativeRing(space),
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { a, sf ->
            requireSameSpace(sf, space)
            ScalarField.of(space) { p ->
                space.field.mul(a, sf(p))
            }
        }
    )
}
