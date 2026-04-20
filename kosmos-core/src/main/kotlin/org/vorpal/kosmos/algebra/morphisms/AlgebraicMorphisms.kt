package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.categories.Epimorphism
import org.vorpal.kosmos.categories.Isomorphism
import org.vorpal.kosmos.categories.Monomorphism
import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A structure-preserving morphism between algebraic carriers.
 *
 * This extends the idea of a plain [Morphism] by carrying an explicit [map].
 * Laws are not enforced here; they are witnessed / tested separately.
 */
interface AlgebraicHomomorphism<A : Any, B : Any> : Morphism<A, B> {
    val map: UnaryOp<A, B>
    override fun apply(a: A): B = map(a)
    operator fun invoke(a: A): B = map(a)
}

/**
 * An injective algebraic homomorphism.
 *
 * Injectivity is not enforced here; it is a semantic refinement.
 */
interface AlgebraicMonomorphism<A : Any, B : Any> :
    AlgebraicHomomorphism<A, B>,
    Monomorphism<A, B>

/**
 * A surjective algebraic homomorphism.
 *
 * Surjectivity is not enforced here; it is a semantic refinement.
 */
interface AlgebraicEpimorphism<A : Any, B : Any> :
    AlgebraicHomomorphism<A, B>,
    Epimorphism<A, B>

/**
 * A bijective algebraic homomorphism.
 *
 * The isomorphism itself is the forward homomorphism.
 * The inverse direction is witnessed by [backward].
 */
interface AlgebraicIsomorphism<A : Any, B : Any> :
    AlgebraicHomomorphism<A, B>,
    Isomorphism<A, B>
{
    override val backward: AlgebraicHomomorphism<B, A>

    override fun inverse(): AlgebraicIsomorphism<B, A> =
        of(
            forward = backward,
            backward = this
        )

    companion object {
        fun <A : Any, B : Any> of(
            forward: AlgebraicHomomorphism<A, B>,
            backward: AlgebraicHomomorphism<B, A>
        ): AlgebraicIsomorphism<A, B> = object : AlgebraicIsomorphism<A, B> {
            override val map = forward.map
            override val backward = backward
        }
    }
}