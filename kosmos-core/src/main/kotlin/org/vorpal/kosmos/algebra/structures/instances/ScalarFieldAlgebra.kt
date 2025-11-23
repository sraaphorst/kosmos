package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.analysis.ScalarFields
import org.vorpal.kosmos.analysis.plus
import org.vorpal.kosmos.analysis.times
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

/**
 * The set of ScalarField<F, V> for a commutative ring.
 */

interface RingOps<S : Any> { val ring: CommutativeRing<S> }

fun interface ScalarFieldExpr<S : Any, V : Any> {
    context(r: RingOps<S>, vs: VectorSpace<S, V>)
    fun at(p: V): S
}

//fun <S : Any> fExpr(): ScalarFieldExpr<S, Pair<S, S>> = ScalarFieldExpr {
//    val (x, y) = it
//    with (ring)
//}

typealias ScalarFieldRing<F, V> = CommutativeRing<ScalarField<F, V>>

object ScalarFieldAlgebra {
    /**
     * Given a [VectorSpace] V over a [Field] 𝔽, this defines the additive abelian group
     * of [ScalarField]s (𝔽^V, +) under pointwise addition.
     */
    fun <F: Any, V: Any> additiveAbelianGroup(
        space: VectorSpace<F, V>
    ): AbelianGroup<ScalarField<F, V>> = AbelianGroup.of(
        identity = ScalarFields.zero(space),
        op = BinOp(Symbols.PLUS, ScalarField<F, V>::plus),
        inverse = Endo(Symbols.MINUS) { sf ->
            ScalarFields.of(space) {
                space.field.add.inverse(sf(it))
            }
        }
    )

    /**
     * Given a [VectorSpace] V over a [Field] 𝔽, this defines the multiplicative
     * [Monoid] of [ScalarField]s ((𝔽^*)^V, ·) under pointwise multiplication.
     */
    fun <F: Any, V: Any> multiplicativeCommutativeMonoid(
        space: VectorSpace<F, V>
    ): CommutativeMonoid<ScalarField<F, V>> = CommutativeMonoid.of(
        identity = ScalarFields.one(space),
        op = BinOp(Symbols.DOT){ sf1, sf2 -> sf1 * sf2 }
    )

    /**
     * Given a [VectorSpace] V over a [Field] 𝔽, create the [CommutativeRing] of
     * [ScalarField]s (𝔽^V, +, ·), i.e. the internal Hom(V, 𝔽) under
     * pointwise operations.
     */
    fun <F: Any, V: Any> commutativeRing(
        space: VectorSpace<F, V>
    ): CommutativeRing<ScalarField<F, V>> =
        CommutativeRing.of(
            add = additiveAbelianGroup(space),
            mul = multiplicativeCommutativeMonoid(space)
        )
}
