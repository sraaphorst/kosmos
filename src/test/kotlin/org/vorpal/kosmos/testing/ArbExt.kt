package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import org.vorpal.kosmos.core.Eq

/** Exclude any of the [banned] values using Eq. */
fun <A> Arb<A>.excluding(eq: Eq<A>, vararg banned: A): Arb<A> =
    this.filter { a -> banned.none { b -> eq.eqv(a, b) } }