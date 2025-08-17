package org.vorpal.kosmos.categories

/** An isomorphism A ≅ B: f : A -> B with inverse g : B -> A. */
data class Iso<A, B>(val f: Morphism<A, B>, val g: Morphism<B, A>) {
    /** Compose isos: (A ≅ B) ∘ (B ≅ C) = (A ≅ C). */
    infix fun <C> then(other: Iso<B, C>): Iso<A, C> =
        Iso(f = this.f then other.f, g = other.g then this.g)

    /** Swap direction: B ≅ A. */
    fun inverse(): Iso<B, A> = Iso(f = g, g = f)

    fun leftIdentity(domain: FiniteSet<A>, eqA: (A, A) -> Boolean): Boolean =
        domain.toList().all { a -> eqA((f then g).apply(a), a) }

    fun rightIdentity(codomain: FiniteSet<B>, eqB: (B, B) -> Boolean): Boolean =
        codomain.toList().all { b -> eqB((g then f).apply(b), b) }
}

/** Identity isomorphism on A. */
fun <A> isoRefl(): Iso<A, A> = Iso({ it }, { it })

/**
 * If `f` is bijective on these finite sets (w.r.t. eqA/eqB), construct its inverse and return an Iso.
 * Returns null if `f` is not bijective over (domain, codomain).
 */
fun <A, B> isoFromBijective(
    f: Morphism<A, B>,
    domain: FiniteSet<A>,
    codomain: FiniteSet<B>,
    eqA: (A, A) -> Boolean,
    eqB: (B, B) -> Boolean
): Iso<A, B>? {
    val asList = domain.toList()
    val bsList = codomain.toList()

    // Build the graph of f restricted to 'domain'
    val table: List<Pair<A, B>> = asList.map { a -> a to f.apply(a) }

    // Surjectivity: each b in codomain appears in the image (by eqB)
    val surjective = bsList.all { b ->
        table.any { (_, fb) -> eqB(fb, b) }
    }
    if (!surjective) return null

    // Injectivity: no two distinct a map to eqB-equal b
    val injective = table.indices.none { i ->
        ((i + 1) until table.size).any { j ->
            val (a1, b1) = table[i]
            val (a2, b2) = table[j]
            !eqA(a1, a2) && eqB(b1, b2)
        }
    }
    if (!injective) return null

    // Build inverse g : B -> A using the (unique) preimage for each b in codomain
    val g = Morphism<B, A> { b ->
        // find the unique a with f(a) == b (by eqB). We know it exists by surjectivity.
        val pair = table.firstOrNull { (_, fb) -> eqB(fb, b) }
        requireNotNull(pair) { "No preimage for $b under f on the given domain/codomain" }
        pair.first
    }

    // Sanity: check the identities on these finite sets
    val idAok = asList.all { a -> eqA((f then g).apply(a), a) } // A -> A
    val idBok = bsList.all { b -> eqB((g then f).apply(b), b) } // B -> B
    return if (idAok && idBok) Iso(f, g) else null
}