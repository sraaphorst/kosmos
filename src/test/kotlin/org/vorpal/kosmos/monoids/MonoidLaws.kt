package org.vorpal.kosmos.monoids

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.monoids.Eq
import org.vorpal.monoids.Monoid

// Template for a StringSpec to avoid having to use something like this repeatedly:
//     "Int + is a monoid" {
//        val laws = MonoidLaws(IntSum, Arb.int(), IntEq)
//        laws.associativity()
//        laws.leftIdentity()
//        laws.rightIdentity()
//    }
fun <A> StringSpec.registerMonoidLaws(
    name: String,
    laws: MonoidLaws<A>
) {
    "$name :: associativity" { laws.associativity() }
    "$name :: left identity" { laws.leftIdentity() }
    "$name :: right identity" { laws.rightIdentity() }
}

class MonoidLaws<A>(
    private val M: Monoid<A>,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) {
    suspend fun associativity() = checkAll(arb, arb, arb) { a, b, c ->
        check(EQ.eqv(M.combine(a, M.combine(b, c)), M.combine(M.combine(a, b), c))) {
            "Associativity failed for $a, $b, $c"
        }
    }
    suspend fun leftIdentity() = checkAll(arb) { a ->
        check(EQ.eqv(M.combine(M.empty, a), a)) { "Left identity failed for $a" }
    }
    suspend fun rightIdentity() = checkAll(arb) { a ->
        check(EQ.eqv(M.combine(a, M.empty), a)) { "Right identity failed for $a" }
    }
}