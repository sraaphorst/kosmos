package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.analysis.ScalarFields
import org.vorpal.kosmos.analysis.plus
import org.vorpal.kosmos.analysis.times
import org.vorpal.kosmos.core.ops.BinOp

/**
 * The set of ScalarField<F, V> for a commutative ring.
 */
typealias ScalarFieldRing<F, V> = CommutativeRing<ScalarField<F, V>>

object ScalarFieldAlgebra {
    /**
     * Given a [VectorSpace] V over a [Field] 𝔽, this defines the additive abelian group
     * of [ScalarField]s (𝔽^V, +) under pointwise addition.
     */
    fun <F, V> additiveAbelianGroup(
        space: VectorSpace<F, V>
    ): AbelianGroup<ScalarField<F, V>> =
        object : AbelianGroup<ScalarField<F, V>> {
            override val identity: ScalarField<F, V> =
                ScalarFields.zero(space)

            override val op: BinOp<ScalarField<F, V>> = BinOp(
                symbol = "+",
                combine = { sf1, sf2 -> sf1 + sf2 }
            )

            override val inverse: (ScalarField<F, V>) -> ScalarField<F, V> = { sf ->
                ScalarFields.of(space) {
                    space.field.add.inverse(sf(it))
                }
            }
        }

    /**
     * Given a [VectorSpace] V over a [Field] 𝔽, this defines the multiplicative
     * [Monoid] of [ScalarField]s ((𝔽^*)^V, ·) under pointwise multiplication.
     */
    fun <F, V> multiplicativeMonoid(
        space: VectorSpace<F, V>
    ): Monoid<ScalarField<F, V>> =
        Monoid.of(ScalarFields.one(space)) { sf1, sf2 -> sf1 * sf2 }

    /**
     * Given a [VectorSpace] V over a [Field] 𝔽, create the [CommutativeRing] of
     * [ScalarField]s (𝔽^V, +, ·), i.e. the internal Hom(V, 𝔽) under
     * pointwise operations.
     */
    fun <F, V> commutativeRing(
        space: VectorSpace<F, V>
    ): CommutativeRing<ScalarField<F, V>> =
        CommutativeRing.of(
            add = additiveAbelianGroup(space),
            mul = multiplicativeMonoid(space)
        )
}
