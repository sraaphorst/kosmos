package org.vorpal.kosmos

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.laws.AbelianGroupLaws
import org.vorpal.kosmos.algebra.laws.CommutativeIdempotentQuasigroupLaws
import org.vorpal.kosmos.algebra.laws.FieldLaws
import org.vorpal.kosmos.algebra.laws.GroupLaws
import org.vorpal.kosmos.algebra.laws.RModuleLaws
import org.vorpal.kosmos.algebra.laws.MonoidLaws
import org.vorpal.kosmos.algebra.laws.RingLaws
import org.vorpal.kosmos.algebra.laws.SemigroupLaws
import org.vorpal.kosmos.algebra.laws.VectorSpaceLaws
import org.vorpal.kosmos.algebra.ops.Mul
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.relations.laws.EquivalenceLaws
import org.vorpal.kosmos.relations.laws.PosetLaws
import org.vorpal.kosmos.relations.laws.PreorderLaws
import org.vorpal.kosmos.relations.laws.StrictTotalOrderLaws
import org.vorpal.kosmos.relations.laws.TotalOrderLaws

/**
 * This file comprises registration methods to register testing classes.
 */

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

/* ------------ Quasigroups ------------ */

fun <A> StringSpec.registerCIQ(
    name: String,
    laws: CommutativeIdempotentQuasigroupLaws<A, *>
) = apply {
    "$name :: all (CIQ)" { laws.all() }
}

/* -------------- Modules -------------- */

fun <S, V, RM> StringSpec.registerModule(
    name: String,
    laws: RModuleLaws<S, V, RM>
) where RM : Monoid<S, Mul> = apply {
    "$name :: all (module)" { laws.all() }
}

/* ------------ Vector Spaces ------------ */

fun <S, V> StringSpec.registerVectorSpace(
    name: String,
    laws: VectorSpaceLaws<S, V>
) = apply {
    "$name :: all (vector space)" { laws.all() }
}

/* ------------- Relations -------------*/

fun <A> StringSpec.registerPreorder(
    name: String,
    laws: PreorderLaws<A>
) = apply {
    "$name :: all (preorder)" { laws.all() }
}

fun <A> StringSpec.registerEquivalence(
    name: String,
    laws: EquivalenceLaws<A>
) = apply {
    "$name :: all (equivalence)" { laws.all() }
}

fun <A> StringSpec.registerPoset(
    name: String,
    laws: PosetLaws<A>
) = apply {
    "$name :: all (poset)" { laws.all() }
}

fun <A> StringSpec.registerTotalOrder(
    name: String,
    laws: TotalOrderLaws<A>
) = apply {
    "$name :: all (total order)" { laws.all() }
}

fun <A> StringSpec.registerStrictTotalOrder(
    name: String,
    laws: StrictTotalOrderLaws<A>
) {
    "$name :: all (strict total order)" { laws.all() }
}
