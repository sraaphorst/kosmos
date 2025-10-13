package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.triple

import org.vorpal.kosmos.combinatorics.FiniteSet
import org.vorpal.kosmos.core.Eq

/** suchThat is simply an alias for filter, implemented for enhanced readability. */
fun <A> Arb<A>.suchThat(predicate: (A) -> Boolean): Arb<A> =
    filter(predicate)


/** Generate a FiniteSet of a size as given in the size parameter. */
fun <A> Arb<A>.finiteSet(size: IntRange = 0..8): Arb<FiniteSet<A>> =
    Arb.set(this, size).map { FiniteSet.ordered(it) }

/** Generate a sorted FiniteSet of a size as given in the size parameter. */
fun <A : Comparable<A>> Arb<A>.sortedFiniteSet(size: IntRange = 0..8): Arb<FiniteSet<A>> =
    Arb.set(this, size).map { s -> FiniteSet.sortedWith(s, Comparator.naturalOrder()) }

/** For a FiniteSet, create a generator that returns an element from the FiniteSet. */
fun <A> FiniteSet<A>.arb(): Arb<A> = Arb.element(this.toList())

/** For a FiniteSet, create a generator that excludes the [banned] elements specified. */
fun <A> FiniteSet<A>.arbExcluding(eq: Eq<A>, vararg banned: A): Arb<A> {
    val keep = this.toList().filter { a -> banned.none { b -> !eq.eqv(a, b) } }
    require(keep.isNotEmpty()) { "All elements were banned from generation." }
    return Arb.element(keep)
}

/** Create a generator that excludes any of the [banned] values. */
fun <A> Arb<A>.excluding(eq: Eq<A>, vararg banned: A): Arb<A> =
    this.filter { a -> banned.none { b -> eq.eqv(a, b) } }

/** Create a generator excluding any of the elements that satisfy the predicate. */
fun <A> Arb<A>.excludingIf(predicate: (A) -> Boolean): Arb<A> =
    this.filter { !predicate(it) }

/** Create a generator excluding the value [one]. */
fun <A> Arb<A>.excludingOne(eq: Eq<A>, one: A): Arb<A> =
    this.filter { !eq.eqv(one, it) }

/** Create a generator that excludes the specified identity element. */
fun <A> Arb<A>.nonIdentity(eq: Eq<A>, identity: A): Arb<A> =
    this.excludingOne(eq, identity)

/** Create a generator for a pair (a, b) with a ≠ b. */
fun <A> Arb<A>.pairDistinct(eq: Eq<A>): Arb<Pair<A, A>> =
    Arb.pair(this, this)
        .filter { (a, b) -> !eq.eqv(a, b) }

/** Create a generator for a triple (a, b, c) with distinct elements, i.e. with a ≠ b ≠ c. */
fun <A> Arb<A>.tripleDistinct(eq: Eq<A>): Arb<Triple<A, A, A>> =
    Arb.triple(this, this, this)
        .filter { (a, b, c) -> !eq.eqv(a, b) && !eq.eqv(a, c) && !eq.eqv(b, c)  }

/** Create a generator that ensures that the generated pair comprises distinct elements. */
fun <A> Arb<Pair<A, A>>.distinct(eq: Eq<A>): Arb<Pair<A, A>> =
    filter { (a, b) -> !eq.eqv(a, b) }


/** Create a generator that does not produce the value [zero] on the left. */
fun <A> Arb<Pair<A, A>>.nonZeroLeft(eq: Eq<A>, zero: A) =
    filter { (a, _) -> !eq.eqv(a, zero) }

/** Create a generator that does not produce the value [zero] on the right. */
fun <A> Arb<Pair<A, A>>.nonZeroRight(eq: Eq<A>, zero: A) =
    filter { (_, b) -> !eq.eqv(b, zero) }

/** Create a generator that does not produce the value [zero]. */
fun <A> Arb<Pair<A, A>>.nonZeroBoth(eq: Eq<A>, zero: A) =
    filter { (a, b) -> !eq.eqv(a, zero) && !eq.eqv(b, zero) }
