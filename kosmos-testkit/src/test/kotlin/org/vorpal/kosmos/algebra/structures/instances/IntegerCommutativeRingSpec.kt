package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.ConjugationLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.NormSqDefiniteLaw
import org.vorpal.kosmos.laws.property.NormSqFromConjugationLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.laws.property.NormSqNegationInvariantLaw
import org.vorpal.kosmos.laws.property.NormSqNonnegativeLaw
import org.vorpal.kosmos.testing.laws.lawTests

class IntegerCommutativeRingSpec : FunSpec({
    val ring = IntegerAlgebras.IntegerCommutativeRing
    val arb = ArbInteger.small
    val eq = IntegerAlgebras.eqInteger
    val pr = IntegerAlgebras.printableInteger

    // Suites and standalone laws in one graph. The ring suites are built from the same ring, generator,
    // equality and printable, so the sub-suites they have in common run once.
    lawTests(lawGraph("IntegerCommutativeRing") {
        suite(CommutativeRingLaws(ring, arb, eq, pr), LawMode.FULL)
        suite(InvolutiveRingLaws(ring, arb, eq, pr), LawMode.FULL)
        suite(ConjugationLaws(conj = ring.conj, add = ring.add, mul = ring.mul, arb = arb, eq = eq, pr = pr), LawMode.FULL)

        branch("norm squared (${ring.normSq.symbol})") {
            law(NormSqDefiniteLaw(
                normSq = ring.normSq,
                zeroA = ring.zero,
                arbA = arb,
                eqA = eq,
                eqN = eq,
                zeroN = ring.zero,
                prA = pr,
                prN = pr
            ))
            law(NormSqNonnegativeLaw(
                normSq = ring.normSq,
                arbA = arb,
                isNonnegative = { it >= ring.zero },
                prA = pr
            ))
            law(NormSqMultiplicativeLaw(
                mulA = ring.mul.op,
                normSq = ring.normSq,
                mulN = ring.mul.op,
                arbA = arb,
                eqN = eq,
                prA = pr,
                prN = pr
            ))
            law(NormSqNegationInvariantLaw(
                neg = ring.add.inverse,
                normSq = ring.normSq,
                arbA = arb,
                eqN = eq,
                prA = pr,
                prN = pr
            ))
            law(NormSqFromConjugationLaw(
                normSq = ring.normSq,
                embed = Endo(Identity()),
                mulA = ring.mul.op,
                conj = ring.conj,
                arbA = arb,
                eqA = eq,
                prA = pr,
                prN = pr
            ))
        }
    })
})
