package org.vorpal.kosmos.functional.instances.finiteset

import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.FiniteSetOf
import org.vorpal.kosmos.core.finiteset.ForFiniteSet
import org.vorpal.kosmos.core.finiteset.bind
import org.vorpal.kosmos.core.finiteset.fix
import org.vorpal.kosmos.functional.typeclasses.Monad

object FiniteSetMonad : Monad<ForFiniteSet> {

    // 1. Pure: The identity of the Monoid (Singleton Set)
    override fun <A> pure(a: A): FiniteSetOf<A> =
        FiniteSet.singleton(a)

    // 2. FlatMap: The definition of a Set Monad (Union of mapped sets)
    override fun <A, B> flatMap(
        fa: FiniteSetOf<A>,
        f: (A) -> FiniteSetOf<B>
    ): FiniteSetOf<B> {
        // We delegate to the 'bind' extension function you already wrote
        // or implement it directly here using your functional combinators.
        val setA = fa.fix()
        // Determine result type based on input type to preserve Ordering if possible
        return setA.bind { f(it).fix() }
    }

    // 3. Ap: Cartesian Application
    // While Monad provides a default 'ap' via flatMap, overriding it
    // explicitly can sometimes be faster or clearer.
    // Logic: for every f in ff, for every a in fa, yield f(a)
    override fun <A, B> ap(
        ff: FiniteSetOf<(A) -> B>,
        fa: FiniteSetOf<A>
    ): FiniteSetOf<B> {
        val funcs = ff.fix()
        val args = fa.fix()

        // This is effectively funcs.cartesianProduct(args).map { (f, a) -> f(a) }
        // Your class handles Ordered/Unordered logic inside 'bind' or 'flatMap',
        // so we can rely on standard Monad behavior or optimize.
        // Let's rely on the Monad default for consistency:
        return flatMap(ff) { f -> map(fa, f) }
    }

    // 4. Override Traverse for Performance
    // The default 'traverse' builds a list O(N^2).
    // Since we know the size of the output (product of sizes), we could optimize,
    // but Set union/construction is complex.
    // For now, the standard 'sequence' logic (List.foldRight) works fine
    // because it just repeatedly applies Cartesian Products.
}

fun main() {
    // 1. Define the dimensions of our space
    val ranks = FiniteSet.of("10", "J", "Q", "K", "A")
    val suits = FiniteSet.of("♠", "♥", "♦", "♣")

    // 2. Create the list of sets to combine
    // We want to combine Rank and Suit.
    // Note: To use sequence, the list must contain things of the SAME type.
    // So usually we map them to a common type or use a tuple builder.
    val cards = suits.map { s -> ranks.map { r -> s + r } }.toList()
    println(cards.size)
    cards.forEach { println(it) }

    // List<FiniteSet<String>> -> FiniteSet<List<String>>
    // Goes from 4 x 5 -> 625 x 4 (picks one card from each suit, so 5^4 lists in the set).
    val swappedTypes = FiniteSetMonad.sequence(cards).fix()
    swappedTypes.forEach { println(it) }
    println("**** SIZE: ${swappedTypes.size}")

    // Let's try something simpler: Binary combinations
    val bits = FiniteSet.of(0, 1)
    val byteSlots = List(8) { bits } // List of 8 sets: [{0,1}, {0,1}, ...]

    // 3. GENERATE ALL BYTES (Cartesian Product of 8 sets)
    // List<Set<Int>>  --->  Set<List<Int>>
    val allBytes: FiniteSet<List<Int>> = FiniteSetMonad.sequence(byteSlots).fix()

    println("Total combinations: ${allBytes.size}")
    // Output: 256

    allBytes.forEach { println(it) }
    // Output: [[0,0,0,0,0,0,0,0], [0,0,0,0,0,0,0,1], ...]
}