package org.vorpal.kosmos.categories.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import org.vorpal.kosmos.categories.FiniteSet

fun <A> Arb<A>.finiteSet(size: IntRange = 0..8): Arb<FiniteSet<A>> =
    Arb.set(this, size).map { FiniteSet.ofSet(it) }

fun <A : Comparable<A>> Arb<A>.sortedFiniteSet(size: IntRange = 0..8): Arb<FiniteSet<A>> =
    Arb.set(this, size).map { s -> FiniteSet.ofSet(s, Comparator.naturalOrder()) }

fun <A> FiniteSet<A>.arb(): Arb<A> = Arb.element(this.toList())