package org.vorpal.kosmos.core.relations

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.rational.ArbWheelZ
import org.vorpal.kosmos.core.rational.WheelZ
import org.vorpal.kosmos.core.relations.instances.IntRelations
import org.vorpal.kosmos.core.relations.instances.RealRelations
import org.vorpal.kosmos.core.relations.instances.TropicalMaxRelations
import org.vorpal.kosmos.core.relations.instances.TropicalMinRelations
import org.vorpal.kosmos.core.relations.instances.WheelZRelations
import org.vorpal.kosmos.core.setoid.Setoid
import org.vorpal.kosmos.core.tropical.TropicalMax
import org.vorpal.kosmos.core.tropical.TropicalMin
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.relation.AsymmetryLaw
import org.vorpal.kosmos.laws.relation.ConnexityLaw
import org.vorpal.kosmos.laws.relation.CoreflexivityLaw
import org.vorpal.kosmos.laws.relation.EquivalenceRelationLaws
import org.vorpal.kosmos.laws.relation.PartialOrderLaws
import org.vorpal.kosmos.laws.relation.QuasiReflexivityLaw
import org.vorpal.kosmos.laws.relation.StrictTotalOrderLaws
import org.vorpal.kosmos.laws.relation.StrongConnexityLaw
import org.vorpal.kosmos.laws.relation.SymmetryLaw
import org.vorpal.kosmos.laws.relation.TotalOrderLaws
import org.vorpal.kosmos.laws.setoid.SetoidLaws
import org.vorpal.kosmos.laws.verify
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.failedLabels
import org.vorpal.kosmos.testing.laws.local
import java.math.BigInteger

/*
 * Relations, orders and setoids. Several laws have a premise (symmetry: a R b ⇒ b R a; asymmetry, coreflexivity,
 * quasi-reflexivity), so they are checked over small domains where the premise holds often enough to mean something.
 * The negative branches check that each law rejects a relation that does not have its property.
 */

/** Integers in [-6, 6]: small enough that `a = b` and `a ≡ b (mod 5)` come up often. */
private val smallInts: Arb<BigInteger> = Arb.int(-6..6).map(Int::toBigInteger)

/** Congruence modulo 5, on plain ints. */
private val mod5: Relation<Int> = Relation("≡₅") { a, b -> Math.floorMod(a - b, 5) == 0 }
private val mod5Eq: Eq<Int> = Eq { a, b -> mod5(a, b) }
private val ints12 = Arb.int(-12..12)

/** Divisibility `a | b` on 1..30: a partial order that is not total. */
private val divides: Relation<Int> = Relation("|") { a, b -> b % a == 0 }
private val oneTo30 = Arb.int(1..30)

private val finiteReals: Arb<Real> = Arb.double(-1.0e6..1.0e6, includeNonFiniteEdgeCases = false)

/** A chain 0 ≈ 6·10⁻⁹ ≈ 1.2·10⁻⁸ in which the ends are further apart than the tolerance 10⁻⁸. */
private val nearTolerance: Arb<Real> = Arb.element(0.0, 6.0e-9, 1.2e-8)

private val tropicalMax: Arb<TropicalMax> = Arb.choice(
    Arb.constant(TropicalMax.NegInfinity),
    Arb.int(-5..5).map { TropicalMax.Finite(it.toDouble()) }
)
private val tropicalMin: Arb<TropicalMin> = Arb.choice(
    Arb.constant(TropicalMin.PosInfinity),
    Arb.int(-5..5).map { TropicalMin.Finite(it.toDouble()) }
)

class RelationsSpec : FunSpec({
    lawTests(lawGraph("relations") {

        branch("integers") {
            suite(TotalOrderLaws(IntRelations.IntLE, smallInts))
            suite(StrictTotalOrderLaws(IntRelations.IntLT, smallInts))
            suite(EquivalenceRelationLaws(IntRelations.IntEqByComparator, smallInts))
            law(CoreflexivityLaw(IntRelations.IntEqByComparator, smallInts))
            law(AsymmetryLaw(IntRelations.IntLT, smallInts))
            law(ConnexityLaw(IntRelations.IntLE, smallInts))
            law(ConnexityLaw(IntRelations.IntLT, smallInts))
            suite(QuasiReflexivityLaw(IntRelations.IntLE, smallInts))

            local("≤ is not symmetric") {
                SymmetryLaw(IntRelations.IntLE, smallInts).verify().failedLabels().single() shouldContain "symmetry"
            }
            local("< is connex (on distinct pairs) but not strongly connex: a < a fails") {
                StrongConnexityLaw(IntRelations.IntLT, smallInts).verify().failedLabels().single() shouldContain
                    "strong connexity"
            }
            local("< is neither left nor right quasi-reflexive") {
                val failing = QuasiReflexivityLaw(IntRelations.IntLT, smallInts).verify().failedLabels()
                failing.size shouldBe 2
            }
            local("≤ is not asymmetric") {
                AsymmetryLaw(IntRelations.IntLE, smallInts).verify().failedLabels().single() shouldContain "asymmetry"
            }
        }

        branch("congruence mod 5") {
            suite(EquivalenceRelationLaws(mod5, ints12))
            suite(SetoidLaws(Setoid.of(mod5Eq), ints12))
            local("congruence mod 5 is not coreflexive: 0 ≡ 5 but 0 ≠ 5") {
                CoreflexivityLaw(mod5, ints12).verify().failedLabels().single() shouldContain "coreflexivity"
            }
        }

        branch("divisibility on 1..30") {
            suite(PartialOrderLaws(divides, oneTo30))
            suite(QuasiReflexivityLaw(divides, oneTo30))
            local("divisibility is not connex: neither 2 | 3 nor 3 | 2") {
                ConnexityLaw(divides, oneTo30).verify().failedLabels().single() shouldContain "connexity"
            }
        }

        branch("reals") {
            suite(TotalOrderLaws(RealRelations.RealTotalOrder.le, finiteReals))
            suite(StrictTotalOrderLaws(RealRelations.RealLT, finiteReals))
            local("approximate equality is reflexive and symmetric but not transitive") {
                val failing = EquivalenceRelationLaws(RealRelations.RealEqApprox, nearTolerance).verify().failedLabels()
                failing.single() shouldContain "transitivity"
                RealRelations.RealEqApprox(0.0, 6.0e-9) shouldBe true
                RealRelations.RealEqApprox(6.0e-9, 1.2e-8) shouldBe true
                RealRelations.RealEqApprox(0.0, 1.2e-8) shouldBe false
            }
        }

        branch("tropical") {
            branch("max-plus, with −∞ at the bottom") {
                suite(TotalOrderLaws(TropicalMaxRelations.leTropicalMax, tropicalMax))
                suite(StrictTotalOrderLaws(TropicalMaxRelations.ltTropicalMax, tropicalMax))
                suite(EquivalenceRelationLaws(TropicalMaxRelations.eqTropicalMax, tropicalMax))
            }
            branch("min-plus, with +∞ at the top") {
                suite(TotalOrderLaws(TropicalMinRelations.leTropicalMin, tropicalMin))
                suite(StrictTotalOrderLaws(TropicalMinRelations.ltTropicalMin, tropicalMin))
                suite(EquivalenceRelationLaws(TropicalMinRelations.eqTropicalMin, tropicalMin))
            }
        }

        branch("the wheel ℤ ∪ {∞, ⊥}") {
            suite(PartialOrderLaws(WheelZRelations.WheelZLe, ArbWheelZ))
            local("≤ on the wheel is not total: ⊥ is comparable only to itself") {
                WheelZRelations.WheelZLe(WheelZ.BOTTOM, WheelZ.ZERO) shouldBe false
                WheelZRelations.WheelZLe(WheelZ.ZERO, WheelZ.BOTTOM) shouldBe false
                ConnexityLaw(WheelZRelations.WheelZLe, ArbWheelZ).verify().failedLabels().single() shouldContain
                    "connexity"
            }
        }
    })
})
