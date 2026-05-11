package org.vorpal.kosmos.categories

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.functional.datastructures.Option

/**
 * Left-to-right morphism composition alias.
 *
 * `(f andThen g)(x) = g(f(x))`
 *
 * Equivalent to [Morphism.andThen]; provided so call sites can read in the
 * mathematical direction: first `f`, then `g`.
 */
infix fun <A : Any, B : Any, C : Any> Morphism<A, B>.andThen(g: Morphism<B, C>): Morphism<A, C> =
    this andThen g

/**
 * Right-to-left morphism composition alias.
 */
infix fun <A : Any, B : Any, C : Any> Morphism<C, B>.compose(g: Morphism<A, C>): Morphism<A, B> =
    g andThen this

/**
 * An isomorphism A ≅ B as a forward map `f : A → B` paired with an inverse `g : B → A`.
 *
 * This is the lightweight, type-only sibling of [Isomorphism]: it does not carry domain
 * or codomain witnesses, so the identity laws can only be checked on a specific finite
 * set, via [leftIdentity] / [rightIdentity].
 *
 * For a witnessed version of the same idea over [FiniteSet]s, see [Bijection].
 */
data class Iso<A : Any, B : Any>(val f: Morphism<A, B>, val g: Morphism<B, A>) {
    /** Compose isos: `(A ≅ B) andThen (B ≅ C)` gives `A ≅ C`. */
    infix fun <C : Any> andThen(other: Iso<B, C>): Iso<A, C> =
        Iso(
            f = this.f andThen other.f,
            g = other.g andThen this.g
        )

    infix fun <C : Any> compose(other: Iso<C, A>): Iso<C, B> =
        other andThen this

    /** Swap direction: `B ≅ A`. */
    fun inverse(): Iso<B, A> =
        Iso(f = g, g = f)

    /** `(f andThen g)` is the identity on `domain`, checked elementwise. */
    fun leftIdentity(domain: FiniteSet<A>, eqA: Eq<A> = Eq.default()): Boolean =
        domain.toList().all { a -> eqA((f andThen g).apply(a), a) }

    /** `(g andThen f)` is the identity on `codomain`, checked elementwise. */
    fun rightIdentity(codomain: FiniteSet<B>, eqB: Eq<B> = Eq.default()): Boolean =
        codomain.toList().all { b -> eqB((g andThen f).apply(b), b) }
}

object Isos {
    /** The identity isomorphism on `A`. */
    fun <A : Any> identity(): Iso<A, A> =
        Iso({ it }, { it })

    /**
     * If `f` is bijective on `(domain, codomain)` with respect to `eqA`/`eqB`, build its
     * inverse and return the resulting [Iso]; otherwise return `None`.
     *
     * Bijectivity is checked by:
     *  - **surjectivity**: every `b` in `codomain` is `eqB`-equal to some `f(a)`;
     *  - **injectivity**: no two `eqA`-distinct `a`s share an `eqB`-equal image.
     *
     * The returned inverse `g : B → A` picks, for each `b`, the first `a` with `eqB(f(a), b)`.
     * The construction is sanity-checked by [Iso.leftIdentity] and [Iso.rightIdentity]; if
     * either fails, `null` is returned.
     */
    fun <A : Any, B : Any> isoFromBijection(
        f: Morphism<A, B>,
        domain: FiniteSet<A>,
        codomain: FiniteSet<B>,
        eqA: Eq<A> = Eq.default(),
        eqB: Eq<B> = Eq.default()
    ): Option<Iso<A, B>> {
        val asList = domain.toList()
        val bsList = codomain.toList()

        // The graph of f restricted to `domain`.
        val table: List<Pair<A, B>> = asList.map { a -> a to f.apply(a) }

        // Surjectivity: each b in codomain appears in the image (by eqB).
        val surjective = bsList.all { b -> table.any { (_, fb) -> eqB(fb, b) } }
        if (!surjective) return Option.None

        // Injectivity: no two eqA-distinct a values map to eqB-equal b values.
        val injective = table.indices.none { i ->
            ((i + 1) until table.size).any { j ->
                val (a1, b1) = table[i]
                val (a2, b2) = table[j]
                !eqA(a1, a2) && eqB(b1, b2)
            }
        }
        if (!injective) return Option.None

        // Build inverse g: B → A using the (unique) preimage of each b in codomain.
        val g = Morphism<B, A> { b ->
            val pair = table.firstOrNull { (_, fb) -> eqB(fb, b) }
            requireNotNull(pair) { "No preimage for $b under f on the given domain/codomain" }
            pair.first
        }

        // Sanity check both identities on the given finite sets.
        val idAok = asList.all { a -> eqA((f andThen g).apply(a), a) }
        val idBok = bsList.all { b -> eqB((g andThen f).apply(b), b) }
        return if (idAok && idBok) Option.Some(Iso(f, g)) else Option.None
    }
}
