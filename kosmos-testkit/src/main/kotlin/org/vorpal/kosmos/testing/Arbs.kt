package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.triple

import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.Eq

/** suchThat is simply an alias for filter, implemented for enhanced readability. */
fun <A: Any> Arb<A>.suchThat(predicate: (A) -> Boolean): Arb<A> =
    filter(predicate)

/** Generate a FiniteSet of a size as given in the size parameter. */
fun <A: Any> Arb<A>.finiteSet(size: IntRange = 0..8): Arb<FiniteSet<A>> =
    Arb.set(this, size).map { FiniteSet.ordered(it) }

/** Generate a sorted FiniteSet of a size as given in the size parameter. */
fun <A : Comparable<A>> Arb<A>.sortedFiniteSet(size: IntRange = 0..8): Arb<FiniteSet<A>> =
    Arb.set(this, size).map { s -> FiniteSet.sortedWith(s, Comparator.naturalOrder()) }

/** For a FiniteSet, create a generator that returns an element from the FiniteSet. */
fun <A: Any> FiniteSet<A>.arb(): Arb<A> {
    require(isNotEmpty) { "Cannot build Arb from an empty FiniteSet." }
    return Arb.element(toList())
}

/** Create a generator that excludes any of the [banned] values. */
fun <A: Any> Arb<A>.excluding(eq: Eq<A>, vararg banned: A): Arb<A> =
    this.filterNot { a -> banned.any { eq(a, it) } }


/** For a FiniteSet, create a generator that excludes the [banned] elements specified. */
fun <A: Any> FiniteSet<A>.arbExcluding(eq: Eq<A>, vararg banned: A): Arb<A> {
    val keep = toList().filterNot { a -> banned.any { eq(a, it) } }
    require(keep.isNotEmpty()) { "All elements were banned from generation." }
    return Arb.element(keep)
}


/** Create a generator excluding any of the elements that satisfy the predicate. */
fun <A: Any> Arb<A>.excludingIf(predicate: (A) -> Boolean): Arb<A> =
    filterNot( predicate)

/** Create a generator excluding the value [one]. */
fun <A: Any> Arb<A>.excludingOne(eq: Eq<A>, one: A): Arb<A> =
    filterNot { eq(one, it) }

/** Create a generator that excludes the specified identity element. */
fun <A: Any> Arb<A>.nonIdentity(eq: Eq<A>, identity: A): Arb<A> =
    excludingOne(eq, identity)

/** Create a generator for a pair (a, b) with a ≠ b. */
fun <A: Any> Arb<A>.pairDistinct(eq: Eq<A>): Arb<Pair<A, A>> =
    Arb.pair(this, this)
        .filterNot { (a, b) -> eq(a, b) }

/** Create a generator for a triple (a, b, c) with pairwise distinct elements. */
fun <A: Any> Arb<A>.tripleDistinct(eq: Eq<A>): Arb<Triple<A, A, A>> =
    Arb.triple(this, this, this)
        .filterNot { (a, b, c) -> eq(a, b) || eq(a, c) || eq(b, c)  }


/** Create a generator that ensures that the generated pair comprises distinct elements. */
fun <A: Any> Arb<Pair<A, A>>.distinct(eq: Eq<A>): Arb<Pair<A, A>> =
    filterNot { (a, b) -> eq(a, b) }


/** Create a generator that does not produce the value [zero] on the left. */
fun <A: Any> Arb<Pair<A, A>>.nonZeroLeft(eq: Eq<A>, zero: A): Arb<Pair<A, A>> =
    filterNot { (a, _) -> eq(a, zero) }

/** Create a generator that does not produce the value [zero] on the right. */
fun <A: Any> Arb<Pair<A, A>>.nonZeroRight(eq: Eq<A>, zero: A): Arb<Pair<A, A>> =
    filterNot { (_, b) -> eq(b, zero) }

/** Create a generator that does not produce the value [zero]. */
fun <A: Any> Arb<Pair<A, A>>.nonZeroBoth(eq: Eq<A>, zero: A): Arb<Pair<A, A>> =
    filterNot { (a, b) -> eq(a, zero) || eq(b, zero) }
