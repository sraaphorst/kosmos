package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.analysis.ScalarFields
import org.vorpal.kosmos.core.ops.BinOp

object ScalarFieldAlgebra {
    /**
     * Given a [Field] and a [VectorSpace], this defines the additive group of [ScalarFields] (𝔽^V, +).
     */
    fun <F, V> additiveAbelianGroup(
        field: Field<F>,
        space: VectorSpace<F, V>
    ): AbelianGroup<ScalarField<F, V>> where F : Any, V : VectorSpace<F, V> =
        object : AbelianGroup<ScalarField<F, V>> {
            override val identity: ScalarField<F, V> =
                ScalarFields.zero(field, space)

            override val op: BinOp<ScalarField<F, V>> = BinOp(
                combine = { sf1, sf2 ->
                    ScalarFields.of(field, space) {
                        v -> field.add.op.combine(sf1(v), sf2(v))
                    }
                },
                symbol = "+"
            )

            override val inverse: (ScalarField<F, V>) -> ScalarField<F, V> = { sf ->
                ScalarFields.of(field, space) {
                    field.add.inverse(sf(it))
                }
            }
        }

    /**
     * Given a [Field] and a [VectorSpace], this defines the multiplicative [Monoid] of [ScalarFields] ((𝔽^*)^V, *).
     */
    fun <F, V> multiplicativeMonoid(
        field: Field<F>,
        space: VectorSpace<F, V>
    ): Monoid<ScalarField<F, V>> where F : Any, V : VectorSpace<F, V> =
        object : Monoid<ScalarField<F, V>> {
            override val identity: ScalarField<F, V> =
                ScalarFields.one(field, space)

            override val op: BinOp<ScalarField<F, V>> = BinOp(
                combine = { sf1, sf2 ->
                    ScalarFields.of(field, space) {
                        v -> field.mul.op.combine(sf1(v), sf2(v))
                    }
                },
                symbol = "*"
            )
        }

    /**
     * Given a [Field] and a [VectorSpace], create the [CommutativeRing] of [ScalarField]s (𝔽^V, +, ·),
     * i.e. the internal Hom(V, 𝔽) under pointwise operations.
     */
    fun <F, V> commutativeRing(
        field: Field<F>,
        space: VectorSpace<F, V>
    ): CommutativeRing<ScalarField<F, V>> where F: Any, V: VectorSpace<F, V> =
        object : CommutativeRing<ScalarField<F, V>> {
            override val add: AbelianGroup<ScalarField<F, V>> = additiveAbelianGroup(field, space)
            override val mul: Monoid<ScalarField<F, V>> = multiplicativeMonoid(field, space)
        }
}
