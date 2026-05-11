package org.vorpal.kosmos.categories

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.finiteset.FiniteSet

/** A general morphism from one type to another. */
fun interface Morphism<A : Any, B : Any> {
    fun apply(a: A): B

    /** Morphism composition: note (f then g)(x) = g(f(x)). */
    infix fun <C : Any> andThen(g: Morphism<B, C>): Morphism<A, C> =
        Morphism { a -> g.apply(apply(a)) }

    infix fun <C : Any> compose(g: Morphism<C, A>): Morphism<C, B> =
        g andThen this

    /**
     * Create an equality checker for a given finite domain.
     * This produces a function that takes another morphism from A to B
     * and determines if they are equal over the domain.
     */
    fun eqOn(
        domain: FiniteSet<A>,
        eqB: Eq<B> = Eq.default()
    ): (Morphism<A, B>) -> Boolean = { other ->
        domain.all {
            a -> eqB(apply(a), other.apply(a))
        }
    }

    companion object {
        fun <A : Any> id(): Morphism<A, A> = Morphism { it }
    }
}

/**
 * An injective (one-to-one) morphism from one type to another.
 *
 * This interface is a semantic refinement only; injectivity is not enforced here.
 */
interface Monomorphism<A : Any, B : Any> : Morphism<A, B> {
    infix fun <C : Any> andThen(g: Monomorphism<B, C>): Monomorphism<A, C> =
        object : Monomorphism<A, C> {
            override fun apply(a: A): C = g.apply(this@Monomorphism.apply(a))
        }

    infix fun <C : Any> compose(g: Monomorphism<C, A>): Monomorphism<C, B> =
        g andThen this

    companion object {
        fun <A : Any> id(): Monomorphism<A, A> = object : Monomorphism<A, A> {
            override fun apply(a: A): A = a
        }
    }
}

/**
 * A surjective (onto) morphism from one type to another.
 *
 * This interface is a semantic refinement only; surjectivity is not enforced here.
 */
interface Epimorphism<A : Any, B : Any> : Morphism<A, B> {
    infix fun <C : Any> andThen(g: Epimorphism<B, C>): Epimorphism<A, C> =
        object : Epimorphism<A, C> {
            override fun apply(a: A): C = g.apply(this@Epimorphism.apply(a))
        }

    infix fun <C : Any> compose(g: Epimorphism<C, A>): Epimorphism<C, B> =
        g andThen this

    companion object {
        fun <A : Any> id(): Epimorphism<A, A> = object : Epimorphism<A, A> {
            override fun apply(a: A): A = a
        }
    }
}

/**
 * A bijective morphism from one type to another.
 *
 * The isomorphism itself is the forward map.
 * The inverse direction is witnessed by [backward].
 */
interface Isomorphism<A : Any, B : Any> : Morphism<A, B> {
    val backward: Morphism<B, A>

    infix fun <C : Any> andThen(g: Isomorphism<B, C>): Isomorphism<A, C> =
        of(
            forward = { a -> g.apply(apply(a)) },
            backward = { c -> backward.apply(g.backward.apply(c)) }
        )

    infix fun <C : Any> compose(g: Isomorphism<C, A>): Isomorphism<C, B> =
        g andThen this

    fun inverse(): Isomorphism<B, A> =
        of(
            forward = backward,
            backward = this
        )

    companion object {
        fun <A : Any, B : Any> of(
            forward: Morphism<A, B>,
            backward: Morphism<B, A>
        ): Isomorphism<A, B> = object : Isomorphism<A, B> {
            override val backward = backward
            override fun apply(a: A): B = forward.apply(a)
        }

        fun <A : Any> id(): Isomorphism<A, A> =
            of(Morphism.id(), Morphism.id())
    }
}

fun interface Endomorphism<A : Any> : Morphism<A, A> {
    companion object {
        fun <A : Any> id(): Endomorphism<A> = Endomorphism(Identity())
    }
}

interface Automorphism<A : Any> : Isomorphism<A, A>, Endomorphism<A> {
    override val backward: Endomorphism<A>

    infix fun andThen(g: Automorphism<A>): Automorphism<A> =
        of(
            forward = { a -> g.apply(apply(a)) },
            backward = { a -> backward.apply(g.backward.apply(a)) }
        )


    infix fun compose(g: Automorphism<A>): Automorphism<A> =
        g andThen this

    override fun inverse(): Automorphism<A> =
        of(
            forward = backward,
            backward = this
        )

    companion object {
        fun <A : Any> of(
            forward: Endomorphism<A>,
            backward: Endomorphism<A>
        ): Automorphism<A> = object : Automorphism<A> {
            override val backward = backward
            override fun apply(a: A): A = forward.apply(a)
        }

        fun <A : Any> id(): Automorphism<A> =
            of(Endomorphism.id(), Endomorphism.id())
    }
}

// ============================================================================
// Automorphism extensions - orbit operations on the algebraic structure
// ============================================================================

/**
 * Extension: Compute the orbit of an element under an automorphism.
 * Since automorphisms don't carry domain witnesses, the domain must be provided.
 */
fun <A : Any> Automorphism<A>.orbit(element: A, domain: FiniteSet<A>): FiniteSet<A> {
    require(element in domain) { "Element must be in domain" }

    tailrec fun go(cur: A, acc: List<A>, seen: Set<A>): List<A> =
        if (cur in seen) acc
        else go(apply(cur), acc + cur, seen + cur)

    return FiniteSet.ordered(go(element, emptyList(), emptySet()))
}

/**
 * Extension: Get all cycles in an automorphism as a cycle decomposition.
 */
fun <A : Any> Automorphism<A>.cycleDecomposition(domain: FiniteSet<A>): List<List<A>> {
    tailrec fun go(remaining: Set<A>, acc: List<List<A>>): List<List<A>> =
        if (remaining.isEmpty()) acc
        else {
            val cycle = orbit(remaining.first(), domain).toList()
            go(remaining - cycle.toSet(), if (cycle.size > 1) acc + listOf(cycle) else acc)
        }

    return go(domain.toSet(), emptyList())
}

/**
 * Extension: Compute the order (period) of an element under this automorphism.
 */
fun <A : Any> Automorphism<A>.orderOf(element: A, domain: FiniteSet<A>): Int =
    orbit(element, domain).size

/**
 * Extension: Check if this automorphism is the identity on the given domain.
 */
fun <A : Any> Automorphism<A>.isIdentity(domain: FiniteSet<A>): Boolean =
    domain.all { apply(it) == it }
