package org.vorpal.kosmos.categories

import org.vorpal.kosmos.combinatorial.FiniteSet

/**
 * A bijection between finite sets: an isomorphism with domain / codomain witnesses.
 */
interface Bijection<A, B> : Isomorphism<A, B> {
    val domain: FiniteSet<A>
    val codomain: FiniteSet<B>

    companion object {
        /**
         * Build a bijection from a forward mapping.
         * Validates:
         *  - total: forward.keys == domain
         *  - surjective: image == codomain
         *  - injective: image size == domain size
         */
        fun <A, B> of(
            domain: FiniteSet<A>,
            codomain: FiniteSet<B>,
            forward: Map<A, B>
        ): Bijection<A, B> {
            require(forward.keys == domain.toSet()) { "Forward map must cover the entire domain." }
            val image = forward.values.toSet()
            require(image == codomain.toSet()) { "Forward map must be surjective onto the codomain." }
            require(image.size == forward.size) { "Forward map must be injective (no collisions)." }

            val f: Morphism<A, B> = Morphism { forward.getValue(it) }
            val backMap: Map<B, A> = forward.entries.associate { (a, b) -> b to a }
            val g: Morphism<B, A> = Morphism { backMap.getValue(it) }

            return object : Bijection<A, B> {
                override val domain = domain
                override val codomain = codomain
                override val forward = f
                override val backward = g
            }
        }

        /** Convenience for endomorphic bijections, i.e. permutations at the set level. */
        fun <A> endo(
            base: FiniteSet<A>,
            forward: Map<A, A>
        ): Bijection<A, A> = of(base, base, forward)

        /** Tiny DSL for readability on small sets. */
        fun <A, B> build(
            domain: FiniteSet<A>, codomain: FiniteSet<B>,
            builder: MutableMap<A, B>.() -> Unit
        ): Bijection<A, B> {
            val m = LinkedHashMap<A, B>(domain.size).apply(builder)
            return of(domain, codomain, m)
        }
    }
}
