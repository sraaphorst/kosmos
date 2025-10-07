package org.vorpal.kosmos.categories

import org.vorpal.kosmos.combinatorial.FiniteSet

/**
 * A bijection between finite sets: an isomorphism with domain / codomain witnesses.
 */
interface Bijection<A, B> : Isomorphism<A, B> {
    val domain: FiniteSet<A>
    val codomain: FiniteSet<B>

    /**
     * Compose with another bijection, preserving the Bijection type.
     * Note: This shadows the Isomorphism.then to maintain type precision.
     */
    infix fun <C> then(g: Bijection<B, C>): Bijection<A, C> = of(
        domain = this.domain,
        codomain = g.codomain,
        forward = domain.associateWith { a -> g.apply(this.apply(a)) }
    )

    /**
     * Inverse bijection with domain and codomain swapped.
     */
    override fun inverse(): Bijection<B, A> = of(
        domain = this.codomain,
        codomain = this.domain,
        forward = codomain.associateWith(backward::apply)
    )

    /**
     * Check if this is an endomorphism (domain == codomain).
     */
    fun isEndomorphism(): Boolean =
        domain.size == codomain.size && domain.toSet() == codomain.toSet()

    /**
     * Convert to Automorphism if this is an endomorphism.
     */
    fun toAutomorphismOrNull(): Automorphism<A>? =
        if (isEndomorphism()) {
            @Suppress("UNCHECKED_CAST")
            Automorphism.of(
                this.forward as Morphism<A, A>,
                this.backward as Morphism<A, A>
            )
        } else null

    /**
     * Get the image of a subset under this bijection.
     */
    fun image(subset: FiniteSet<A>): FiniteSet<B> =
        subset.filter(domain::contains).map(::apply)

    /**
     * Get the preimage of a subset under this bijection.
     */
    fun preimage(subset: FiniteSet<B>): FiniteSet<A> =
        subset.filter(codomain::contains).map(backward::apply)

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
            require(forward.keys == domain.toSet()) {
                "Forward map must cover the entire domain. Missing: ${domain.toSet() - forward.keys}"
            }
            val image = forward.values.toSet()
            require(image == codomain.toSet()) {
                "Forward map must be surjective onto the codomain. " +
                        "Image size: ${image.size}, Codomain size: ${codomain.size}. " +
                        "Missing from image: ${codomain.toSet() - image}"
            }
            require(image.size == forward.size) {
                "Forward map must be injective (no collisions)."
            }

            val f: Morphism<A, B> = Morphism(forward::getValue)
            val backMap: Map<B, A> = forward.entries.associate { (a, b) -> b to a }
            val g: Morphism<B, A> = Morphism(backMap::getValue)

            return object : Bijection<A, B> {
                override val domain = domain
                override val codomain = codomain
                override val forward = f
                override val backward = g
            }
        }

        /**
         * Identity bijection on a set.
         */
        fun <A> id(base: FiniteSet<A>): Bijection<A, A> =
            of(base, base, base.associateWith { it })

        /**
         * Convenience for endomorphic bijections, i.e. permutations at the set level.
         */
        fun <A> endo(
            base: FiniteSet<A>,
            forward: Map<A, A>
        ): Bijection<A, A> = of(base, base, forward)

        /**
         * Tiny DSL for readability on small sets.
         */
        fun <A, B> build(
            domain: FiniteSet<A>,
            codomain: FiniteSet<B>,
            builder: MutableMap<A, B>.() -> Unit
        ): Bijection<A, B> {
            val m = LinkedHashMap<A, B>(domain.size).apply(builder)
            return of(domain, codomain, m)
        }

        /**
         * Create a bijection from two equal-length ordered finite sets,
         * mapping elements by position.
         */
        fun <A, B> fromOrdering(
            domain: FiniteSet.Ordered<A>,
            codomain: FiniteSet.Ordered<B>
        ): Bijection<A, B> {
            require(domain.size == codomain.size) {
                "Domain and codomain must have equal size. Domain: ${domain.size}, Codomain: ${codomain.size}."
            }
            return of(domain, codomain, domain.zip(codomain).toMap())
        }

        /**
         * Create a permutation (endomorphic bijection) from a list of cycles.
         * Each cycle is a list [a₁, a₂, ..., aₙ] meaning a₁ → a₂ → ... → aₙ → a₁.
         */
        fun <A> fromCycles(base: FiniteSet<A>, vararg cycles: List<A>): Bijection<A, A> {
            val mapping = base.associateWith { it }.toMutableMap()

            cycles.forEach { cycle ->
                require(cycle.all(base::contains)) {
                    "All cycle elements must be in the base set"
                }
                require(cycle.size == cycle.toSet().size) {
                    "Cycle must not contain duplicates"
                }

                if (cycle.size > 1) {
                    cycle.indices.forEach { i ->
                        val next = (i + 1) % cycle.size
                        mapping[cycle[i]] = cycle[next]
                    }
                }
            }

            return endo(base, mapping)
        }
    }
}

/**
 * Extension: Convert a permutation bijection to an automorphism.
 * Throws if not an endomorphism.
 */
fun <A> Bijection<A, A>.toAutomorphism(): Automorphism<A> =
    toAutomorphismOrNull()
        ?: error("Cannot convert non-endomorphic bijection to automorphism")

/**
 * Extension: Compute the orbit of an element under repeated application.
 * Returns the set of all elements reachable by repeatedly applying this bijection.
 */
fun <A> Bijection<A, A>.orbit(element: A): FiniteSet<A> {
    require(element in domain) { "Element must be in domain" }

    val seen = mutableSetOf<A>()
    var current = element

    while (current !in seen) {
        seen.add(current)
        current = apply(current)
    }

    return FiniteSet.ordered(seen)
}

/**
 * Extension: Compute the order (period) of an element under this permutation.
 */
fun <A> Bijection<A, A>.orderOf(element: A): Int =
    orbit(element).size

/**
 * Extension: Check if this permutation is the identity.
 */
fun <A> Bijection<A, A>.isIdentity(): Boolean =
    domain.all { apply(it) == it }

/**
 * Extension: Get all cycles in a permutation as a cycle decomposition.
 */
fun <A> Bijection<A, A>.cycleDecomposition(): List<List<A>> {
    val unvisited = domain.toSet().toMutableSet()
    val cycles = mutableListOf<List<A>>()

    while (unvisited.isNotEmpty()) {
        val start = unvisited.first()
        val cycle = orbit(start).toList()

        if (cycle.size > 1) { // Only include non-trivial cycles
            cycles.add(cycle)
        }

        unvisited.removeAll(cycle.toSet())
    }

    return cycles
}
