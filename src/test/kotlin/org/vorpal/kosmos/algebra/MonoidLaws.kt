//package org.vorpal.kosmos.algebra
//
//import io.kotest.core.spec.style.StringSpec
//import io.kotest.property.Arb
//import io.kotest.property.arbitrary.list
//import io.kotest.property.arbitrary.set
//import io.kotest.property.checkAll
//
//import org.vorpal.monoids.Eq
//import org.vorpal.monoids.Monoid
//import org.vorpal.monoids.Monoids
//
//// Template for a StringSpec to avoid having to use something like this repeatedly:
////     "Int + is a monoid" {
////        val laws = MonoidLaws(IntSum, Arb.int(), IntEq)
////        laws.associativity()
////        laws.leftIdentity()
////        laws.rightIdentity()
////    }
//fun <A> StringSpec.registerMonoidLaws(
//    name: String,
//    laws: MonoidLaws<A>
//) {
//    "$name :: associativity" { laws.associativity() }
//    "$name :: left identity" { laws.leftIdentity() }
//    "$name :: right identity" { laws.rightIdentity() }
//}
//
//// Lifters for testing List<A>
//fun <A> Arb<A>.lists(size: IntRange = 0..20): Arb<List<A>> =
//    Arb.list(this, size)
//fun <A> Eq<A>.lists(): Eq<List<A>> =
//    Monoids.listEq(this)
//
//fun <A> StringSpec.registerListMonoidLaws(
//    name: String,
//    elemArb: Arb<A>,
//    eqA: Eq<A>,
//    size: IntRange = 0..20) = registerMonoidLaws(
//    name,
//    MonoidLaws(
//        M = Monoids.listMonoid(),
//        arb = elemArb.lists(size),
//        EQ  = eqA.lists()
//    )
//)
//
//// Lifters for testing Set<A>
//fun <A> Arb<A>.sets(size: IntRange = 0..20): Arb<Set<A>> =
//    Arb.set(this, size)
//
//fun <A> StringSpec.registerSetMonoidLaws(
//    name: String,
//    elemArb: Arb<A>,
//    size: IntRange = 0..20) = registerMonoidLaws(
//        name,
//        MonoidLaws(
//            M = Monoids.setMonoid(),
//            arb = elemArb.sets(size),
//            EQ  = Monoids.SetEq
//        )
//)
//
//class MonoidLaws<A>(
//    private val M: Monoid<A>,
//    private val arb: Arb<A>,        // for identity laws
//    private val EQ: Eq<A>,
//    private val assoc: AssocGen<A> = assocFrom(arb)  // for associativity laws
//) {
//    suspend fun associativity() = checkAll(assoc.triples()) { (a, b, c) ->
//        check(EQ.eqv(M.combine(a, M.combine(b, c)), M.combine(M.combine(a, b), c))) {
//            "Associativity failed for $a, $b, $c"
//        }
//    }
//    suspend fun leftIdentity() = checkAll(arb) { a ->
//        check(EQ.eqv(M.combine(M.empty, a), a)) { "Left identity failed for $a" }
//    }
//    suspend fun rightIdentity() = checkAll(arb) { a ->
//        check(EQ.eqv(M.combine(a, M.empty), a)) { "Right identity failed for $a" }
//    }
//}