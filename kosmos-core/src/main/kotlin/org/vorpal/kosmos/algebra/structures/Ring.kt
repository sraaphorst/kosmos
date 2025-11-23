package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger

/**
 * A [Hemiring] does not attain the status of a [Ring], but has:
 * - A [CommutativeMonoid] for addition.
 * - A [Semigroup] for multiplication.
 * with multiplication being distributive over addition. */
interface Hemiring<A: Any> {
    val add: CommutativeMonoid<A>
    val mul: Semigroup<A>

    companion object {
        fun <A: Any> of(
            add: CommutativeMonoid<A>,
            mul: Semigroup<A>,
        ): Hemiring<A> = object : Hemiring<A> {
            override val add: CommutativeMonoid<A> = add
            override val mul: Semigroup<A> = mul
        }
    }
}

/**
 * A [Semiring] does not attain the status of a [Ring], but has:
 * - A [CommutativeMonoid] for addition.
 * - A [Monoid] for multiplication.
 * with multiplication being distributive over addition. */
interface Semiring<A: Any>: Hemiring<A> {
    override val mul: Monoid<A>

    companion object {
        fun <A: Any> of(
            add: CommutativeMonoid<A>,
            mul: Monoid<A>,
        ): Semiring<A> = object : Semiring<A> {
            override val add: CommutativeMonoid<A> = add
            override val mul: Monoid<A> = mul
        }
    }
}

/**
 * A [CommutativeSemiring] does not achieve the status of a [Ring], but
 * the multiplicative monoid is a [CommutativeMonoid].
 * with multiplication being distributive over addition. */
interface CommutativeSemiring<A: Any>: Semiring<A> {
    override val mul: CommutativeMonoid<A>

    companion object {
        fun <A: Any> of(
            add: CommutativeMonoid<A>,
            mul: CommutativeMonoid<A>
        ): CommutativeSemiring<A> = object : CommutativeSemiring<A> {
            override val add: CommutativeMonoid<A> = add
            override val mul: CommutativeMonoid<A> = mul
        }
    }
}

/**
 * A Ring packs two operations: an operation that acts similar to:
 * - An [AbelianGroup] for addition.
 * - A [Monoid] for multiplication.
 * with multiplication being distributive over addition. */
interface Ring<A: Any>: Semiring<A> {
    override val add: AbelianGroup<A>

    fun fromBigInt(n: BigInteger): A {
        tailrec fun aux(rem: BigInteger, acc: A): A = when (rem) {
            BigInteger.ZERO -> acc
            else -> aux(rem - BigInteger.ONE, add.op(acc, mul.identity))
        }
        val pos = aux(n.abs(), add.identity)
        return if (n.signum() == -1) add.inverse(pos) else pos
    }

    companion object {
        fun <A: Any> of(
            add: AbelianGroup<A>,
            mul: Monoid<A>,
        ): Ring<A> = object : Ring<A> {
            override val add: AbelianGroup<A> = add
            override val mul: Monoid<A> = mul
        }
    }
}


/**
 * Since CommutativeRings are special in the sense that they play so many roles in other algebraic structures,
 * they are included as an extension of Ring even though they add no inherent properties apart from being tagged
 * as being necessarily commutative.
 */
interface CommutativeRing<A: Any> : Ring<A> {
    companion object {
        fun <A: Any> of(
            add: AbelianGroup<A>,
            mul: CommutativeMonoid<A>,
        ): CommutativeRing<A> = object : CommutativeRing<A> {
            override val add: AbelianGroup<A> = add
            override val mul: CommutativeMonoid<A> = mul
        }
    }
}
