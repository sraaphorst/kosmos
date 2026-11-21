package org.vorpal.kosmos.algebra.structures

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.products.BooleanAlgebras as BooleanAlgebraProducts
import org.vorpal.kosmos.algebra.products.DistributiveLattices
import org.vorpal.kosmos.algebra.products.Lattices as LatticeProducts
import org.vorpal.kosmos.algebra.structures.instances.BooleanAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.multiset.Multiset
import org.vorpal.kosmos.core.multiset.algebra.MultisetAlgebras
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.algebra.BooleanAlgebraLaws
import org.vorpal.kosmos.laws.algebra.BoundedLatticeLaws
import org.vorpal.kosmos.laws.algebra.DistributiveLatticeLaws
import org.vorpal.kosmos.laws.algebra.JoinSemilatticeLaws
import org.vorpal.kosmos.laws.algebra.LatticeLaws
import org.vorpal.kosmos.laws.algebra.MeetSemilatticeLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.leaves
import org.vorpal.kosmos.laws.verify
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.failedLabels
import org.vorpal.kosmos.testing.laws.local

/*
 * Lattices and Boolean algebras: the suites accept genuine instances, and reject structures that only claim to be
 * distributive or Boolean. Power-set algebras are in FiniteSetAlgebrasSpec.
 */

private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
private fun lcm(a: Int, b: Int): Int = a / gcd(a, b) * b

/** The divisors of [n], ordered by divisibility: a bounded distributive lattice with ⊥ = 1, ⊤ = n. */
private fun divisors(n: Int): List<Int> = (1..n).filter { n % it == 0 }

private fun divisorLattice(n: Int): DistributiveLattice<Int> = DistributiveLattice.of(
    join = BinOp("lcm") { a, b -> lcm(a, b) },
    meet = BinOp("gcd") { a, b -> gcd(a, b) },
    bottom = 1,
    top = n
)

/** The divisor lattice of [n] with `¬d = n / d`. Boolean exactly when [n] is squarefree. */
private fun divisorAlgebra(n: Int): BooleanAlgebra<Int> = BooleanAlgebra.of(
    join = BinOp("lcm") { a, b -> lcm(a, b) },
    meet = BinOp("gcd") { a, b -> gcd(a, b) },
    bottom = 1,
    top = n,
    not = Endo("$n/·") { d -> n / d }
)

/**
 * A finite lattice given by its order on `0 until size` (0 = ⊥, size - 1 = ⊤), with join and meet computed as least
 * upper and greatest lower bounds. Declared distributive, so the laws have to find out whether it is.
 */
private fun finiteLattice(size: Int, le: (Int, Int) -> Boolean): DistributiveLattice<Int> {
    val elements = 0 until size
    fun lub(a: Int, b: Int) = elements.filter { le(a, it) && le(b, it) }.first { u -> elements.all { v -> !(le(a, v) && le(b, v)) || le(u, v) } }
    fun glb(a: Int, b: Int) = elements.filter { le(it, a) && le(it, b) }.first { l -> elements.all { v -> !(le(v, a) && le(v, b)) || le(v, l) } }
    return DistributiveLattice.of(BinOp("∨") { a, b -> lub(a, b) }, BinOp("∧") { a, b -> glb(a, b) }, 0, size - 1)
}

/** M₃, the diamond: ⊥ = 0 < 1, 2, 3 < 4 = ⊤, with 1, 2, 3 pairwise incomparable. */
private val m3 = finiteLattice(5) { a, b -> a == b || a == 0 || b == 4 }

/** N₅, the pentagon: ⊥ = 0 < 1 < 2 < 4 = ⊤ and 0 < 3 < 4, with 3 incomparable to 1 and 2. */
private val n5 = finiteLattice(5) { a, b -> a == b || a == 0 || b == 4 || (a == 1 && b == 2) }

private val five = Arb.element(0, 1, 2, 3, 4)

/** Sub-multisets of {a, a, b, b, b, c}. */
private val universeMultiset: Multiset<Char> = Multiset.of(mapOf('a' to 2, 'b' to 3, 'c' to 1))
private val subMultisets: Arb<Multiset<Char>> =
    Arb.list(Arb.element('a', 'b', 'c'), 0..6).map { cs ->
        Multiset.of(cs.groupingBy { it }.eachCount().mapValues { (c, k) -> minOf(k, universeMultiset.multiplicity(c)) })
    }
private val eqMultiset: Eq<Multiset<Char>> = Eq { x, y ->
    x.support.toSet() == y.support.toSet() && x.support.all { x.multiplicity(it) == y.multiplicity(it) }
}

class LatticeSpec : FunSpec({
    lawTests(lawGraph("lattices") {

        branch("semilattices") {
            suite(JoinSemilatticeLaws(JoinSemilattice.of(BinOp("max") { a: Int, b: Int -> maxOf(a, b) }), Arb.int()))
            suite(MeetSemilatticeLaws(MeetSemilattice.of(BinOp("min") { a: Int, b: Int -> minOf(a, b) }), Arb.int()))
            local("addition is not a semilattice: it is not idempotent") {
                val report = JoinSemilatticeLaws(JoinSemilattice.of(BinOp("+") { a: Int, b: Int -> a + b }), Arb.int(-50..50))
                    .verify()
                report.failedLabels().single() shouldContain "idempotency"
            }
        }

        branch("Bool") {
            suite(BooleanAlgebraLaws(BooleanAlgebras.BoolAlgebra, Arb.boolean()))
        }

        branch("divisor lattices") {
            branch("D₆₀ (divisors of 60, gcd and lcm)") {
                suite(DistributiveLatticeLaws(divisorLattice(60), Arb.element(divisors(60))))
            }
            branch("D₃₀ with ¬d = 30/d is a Boolean algebra (30 is squarefree)") {
                suite(BooleanAlgebraLaws(divisorAlgebra(30), Arb.element(divisors(30))))
            }
            local("D₁₂ with ¬d = 12/d is not Boolean: involution and De Morgan hold, complements do not") {
                val report = BooleanAlgebraLaws(divisorAlgebra(12), Arb.element(divisors(12))).verify()
                val failing = report.failedLabels()
                failing.isNotEmpty() shouldBe true
                // Only the two complement laws fail: 2 ∨ ¬2 = lcm(2, 6) = 6 ≠ 12, and 2 ∧ ¬2 = gcd(2, 6) = 2 ≠ 1.
                failing.size shouldBe 2
                report.root.leaves().filter { it.label.contains("De Morgan", ignoreCase = true) || it.label.contains("involution") }
                    .forEach { it.status shouldBe LawStatus.PASSED }
            }
        }

        branch("M₃ and N₅: lattices that are not distributive") {
            branch("M₃ is a bounded lattice") { suite(BoundedLatticeLaws(m3, five)) }
            branch("N₅ is a bounded lattice") { suite(BoundedLatticeLaws(n5, five)) }
            local("M₃ and N₅ fail distributivity, and nothing else") {
                listOf(m3, n5).forEach { lattice ->
                    val failing = DistributiveLatticeLaws(lattice, five).verify().failedLabels()
                    failing.isNotEmpty() shouldBe true
                    failing.forEach { it shouldContain "distributiv" }
                }
            }
        }

        branch("multisets") {
            branch("sub-multisets of {a, a, b, b, b, c} under max and min") {
                suite(BoundedLatticeLaws(MultisetAlgebras.multisetMinMaxLattice(universeMultiset), subMultisets, eqMultiset))
            }
        }

        branch("products") {
            branch("Bool × D₃₀") {
                suite(BooleanAlgebraLaws(
                    BooleanAlgebraProducts.product(BooleanAlgebras.BoolAlgebra, divisorAlgebra(30)),
                    Arb.pair(Arb.boolean(), Arb.element(divisors(30)))
                ))
            }
            branch("D₆₀ × D₆₀") {
                suite(DistributiveLatticeLaws(
                    DistributiveLattices.double(divisorLattice(60)),
                    Arb.pair(Arb.element(divisors(60)), Arb.element(divisors(60)))
                ))
            }
            branch("M₃ × M₃ is a lattice") {
                suite(LatticeLaws(LatticeProducts.double(m3), Arb.pair(five, five)))
            }
        }
    })
})
