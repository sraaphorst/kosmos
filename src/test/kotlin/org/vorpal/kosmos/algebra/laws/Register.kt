package org.vorpal.kosmos.algebra.laws

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.*

/* ------------ Semigroup ------------ */

fun <A, S> StringSpec.registerSemigroup(
    name: String,
    laws: SemigroupLaws<A, S>
) where S : Semigroup<A> = apply {
    "$name :: all (semigroup)" { laws.all() }
}

/* -------------- Monoid ------------- */

fun <A, S> StringSpec.registerMonoid(
    name: String,
    laws: MonoidLaws<A, S>
) where S : Monoid<A, *> = apply {
    "$name :: all (monoid)" { laws.all() }
}

/* --------------- Group ------------- */

fun <A, S> StringSpec.registerGroup(
    name: String,
    laws: GroupLaws<A, S>
) where S : Group<A, *> = apply {
    "$name :: all (group)" { laws.all() }
}

/* ----------- Abelian Group --------- */

fun <A, S> StringSpec.registerAbelianGroup(
    name: String,
    laws: AbelianGroupLaws<A, S>
) where S : AbelianGroup<A, *> = apply {
    "$name :: all (abelian group)" { laws.all() }
}

/* ---------------- Ring -------------- */

fun <A> StringSpec.registerRing(
    name: String,
    laws: RingLaws<A>
) = apply {
    "$name :: all (ring)" { laws.all() }
}

/* ---------------- Field -------------- */

fun <A> StringSpec.registerField(
    name: String,
    laws: FieldLaws<A>
) = apply {
    "$name :: all (field)" { laws.all() }
}

/* ------------ Quasigroups ------------*/

fun <A> StringSpec.registerCIQ(
    name: String,
    laws: CommutativeIdempotentQuasigroupLaws<A, *>
) = apply {
    "$name :: all (CIQ)" { laws.all() }
}
