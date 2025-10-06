package org.vorpal.kosmos.categories

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.combinatorial.FiniteSet

/** A general morphism from one type to another. */
fun interface Morphism<A, B> {
    fun apply(a: A): B

    /** Morphism composition: note (f then g)(x) = g(f(x)). */
    infix fun <C> then(g: Morphism<B, C>): Morphism<A, C> =
        Morphism { g.apply(apply(it)) }

    /** Create an equality checker for a given morphism from domain (a set of A) to B.
     * This produces a function that takes another morphism from A to B and determines if they are equal over the domain. */
    fun eqOn(domain: FiniteSet<A>, eqB: (B, B) -> Boolean): (Morphism<A, B>) -> Boolean =
        { other -> domain.all { a -> eqB(this.apply(a), other.apply(a)) } }

    companion object {
        fun <A> id(): Morphism<A, A> = Morphism { it }
    }
}

/**
 * An injective (one-to-one) morphism from one type to another.
 */
fun interface Monomorphism<A, B>: Morphism<A, B> {
    infix fun <C> then(g: Monomorphism<B, C>): Monomorphism<A, C> =
        Monomorphism { g.apply(apply(it)) }

    companion object {
        fun <A> id(): Monomorphism<A, A> = Monomorphism { it }
    }
}

/**
 * A surjective (onto) morphism from one type to another.
 */
fun interface Epimorphism<A, B>: Morphism<A, B> {
    infix fun <C> then(g: Epimorphism<B, C>): Epimorphism<A, C> =
        Epimorphism { g.apply(apply(it)) }

    companion object {
        fun <A> id(): Epimorphism<A, A> = Epimorphism { it }
    }
}

/**
 * Isomorphism: a bijective morphism from one type to another.
 */
interface Isomorphism<A, B> : Morphism<A, B> {
    val forward: Morphism<A, B>
    val backward: Morphism<B, A>

    override fun apply(a: A): B = forward.apply(a)

    /** Composition of two isomorphisms: (A ≅ B) ∘ (B ≅ C) = (A ≅ C). */
    infix fun <C> then(g: Isomorphism<B, C>): Isomorphism<A, C> = of(
        forward = { a -> g.forward.apply(forward.apply(a)) },
        backward = { c -> backward.apply(g.backward.apply(c)) }
    )

    /** Inverse isomorphism: (A ≅ B)^(-1) = (B ≅ A). */
    fun inverse(): Isomorphism<B, A> = of(backward, forward)

    companion object {
        /** Factory function for convenience. */
        fun <A, B> of(forward: Morphism<A, B>, backward: Morphism<B, A>): Isomorphism<A, B> = object : Isomorphism<A, B> {
            override val forward = forward
            override val backward = backward
        }

        /** Identity isomorphism on any type A. */
        fun <A> id(): Isomorphism<A, A> = of(Morphism.id(), Morphism.id())
    }
}

fun interface Endomorphism<A> : Morphism<A, A> {
    companion object {
        fun <A> id(): Endomorphism<A> = Endomorphism { it }
    }
}

interface Automorphism<A> : Isomorphism<A, A>, Endomorphism<A> {
    /** Composition of automorphisms: still an automorphism. */
    infix fun then(g: Automorphism<A>): Automorphism<A> = of(
        forward then g.forward, g.backward then backward
    )

    override fun inverse(): Automorphism<A> = of(backward, forward)

    companion object {
        fun <A> of(forward: Morphism<A, A>, backward: Morphism<A, A>): Automorphism<A> = object : Automorphism<A> {
            override val forward = forward
            override val backward = backward
        }

        fun <A> id(): Automorphism<A> = of(Morphism.id(), Morphism.id())
    }
}


// TODO: *** REMOVE THESE EXAMPLES ***
val intAutoGroup: Group<Automorphism<Int>> = Group.of(
    op = Automorphism<Int>::then,
    identity = Automorphism.id(),
    inv = Automorphism<Int>::inverse
)

object CategoryOfMorphisms {
    fun <A> id(): Morphism<A, A> = Morphism.id()
    fun <A, B, C> compose(f: Morphism<B, C>, g: Morphism<A, B>): Morphism<A, C> =
        g then f
}

class OneObjectCategory<A>(
    private val group: Group<Automorphism<A>>,
    private val obj: A
) : Category<A, Automorphism<A>> {
    override val compose: (Automorphism<A>, Automorphism<A>) -> Automorphism<A> = {f, g -> f then g}
    override val id: (A) -> Automorphism<A> = { Automorphism.id() }
    override fun toString(): String = "Category(Obj=$obj, Morphisms=Automorphisms($obj))"
}

fun main() {
    val autInt = Automorphism.of<Int>({it + 3}, {it - 3})
    println(autInt.apply(10))
    println(autInt.inverse().apply(10))
}
