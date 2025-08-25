package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import org.vorpal.kosmos.categories.FiniteSet
import org.vorpal.kosmos.core.Eq

/* ----- finite set arbs ----- */

fun <A> Arb<A>.finiteSet(size: IntRange = 0..8): Arb<FiniteSet<A>> =
    Arb.set(this, size).map { FiniteSet.ofSet(it) }

fun <A : Comparable<A>> Arb<A>.sortedFiniteSet(size: IntRange = 0..8): Arb<FiniteSet<A>> =
    Arb.set(this, size).map { s -> FiniteSet.ofSet(s, Comparator.naturalOrder()) }

fun <A> FiniteSet<A>.arb(): Arb<A> = Arb.element(this.toList())

fun <A> FiniteSet<A>.arbExcluding(vararg banned: A, eq: Eq<A>): Arb<A> {
    val keep = this.toList().filter { a -> banned.none { b -> !eq.eqv(a, b) } }
    require(keep.isNotEmpty()) { "All elements were banned from generation." }
    return Arb.element(keep)
}

/* ----- simple exclusions ----- */

/** Exclude any of the [banned] values using Eq. */
fun <A> Arb<A>.excluding(eq: Eq<A>, vararg banned: A): Arb<A> =
    this.filter { a -> banned.none { b -> eq.eqv(a, b) } }

fun <A> Arb<A>.excludingIf(pred: (A) -> Boolean): Arb<A> =
    this.filter { !pred(it) }

fun <A> Arb<A>.excludingOne(one: A, eq: Eq<A>): Arb<A> =
    this.filter { !eq.eqv(one, it) }


/* ----- pairs / triples with inequality constraints ----- */

/** Generate (a, b) with a ≠ b. */
fun <A> Arb<A>.pairNeq(eq: Eq<A>): Arb<Pair<A, A>> =
    this.flatMap { a -> this.filter { b -> !eq.eqv(a, b) }.map { b -> Pair(a, b) } }

fun <A> Arb<A>.triplesAllDistinct(eq: Eq<A>): Arb<Triple<A, A, A>> =
    this.flatMap { a ->
        this.filter { b -> !eq.eqv(a, b) }.flatMap { b ->
            this.filter { c -> !eq.eqv(a, c) && !eq.eqv(b, c) }
                .map { c -> Triple(a, b, c) }
        }
    }

/* ----- nonzero generator for fields, groups, etc. ----- */
fun <A> nonIdentity(arbAll: Arb<A>, identity: A, eq: Eq<A>): Arb<A> =
    arbAll.excludingOne(identity, eq)
