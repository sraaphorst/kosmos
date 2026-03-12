package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.ConjugationLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.property.NormSqDefiniteLaw
import org.vorpal.kosmos.laws.property.NormSqFromConjugationLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.laws.property.NormSqNegationInvariantLaw
import org.vorpal.kosmos.laws.property.NormSqNonnegativeLaw

class IntegerCommutativeRingSpec : StringSpec({
    val ring = IntegerAlgebras.IntegerCommutativeRing

    "IntegerCommutativeRing satisfies CommutativeRingLaws" {
        CommutativeRingLaws(
            ring = ring,
            arb = ArbInteger.small,
            eq = IntegerAlgebras.eqInt,
            pr = IntegerAlgebras.printableInteger
        ).fullTest().throwIfFailed()
    }

    "IntegerCommutativeRing satisfies InvolutiveRingLaws" {
        InvolutiveRingLaws(
            ring = ring,
            arb = ArbInteger.small,
            eq = IntegerAlgebras.eqInt,
            pr = IntegerAlgebras.printableInteger
        ).fullTest().throwIfFailed()
    }

    "IntegerCommutativeRing satisfies ConjugationLaws" {
        ConjugationLaws(
            conj = ring.conj,
            add = ring.add,
            mul = ring.mul,
            arb = ArbInteger.small,
            eq = IntegerAlgebras.eqInt,
            pr = IntegerAlgebras.printableInteger
        ).fullTest().throwIfFailed()
    }

    "IntegerCommutativeRing satisfies NormSqDefiniteLaw" {
        NormSqDefiniteLaw(
            normSq = ring.normSq,
            zeroA = ring.zero,
            arbA = ArbInteger.small,
            eqA = IntegerAlgebras.eqInt,
            eqN = IntegerAlgebras.eqInt,
            zeroN = ring.zero,
            prA = IntegerAlgebras.printableInteger,
            prN = IntegerAlgebras.printableInteger
        ).test()
    }

    "IntegerCommutativeRing satisfies NormSqNonnegativeLaw" {
        NormSqNonnegativeLaw(
            normSq = ring.normSq,
            arbA = ArbInteger.small,
            isNonnegative = { it >= ring.zero },
            prA = IntegerAlgebras.printableInteger
        ).test()
    }

    "IntegerCommutativeRing satisfies NormSqMultiplicativeLaw" {
        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = ring.normSq,
            mulN = ring.mul.op,
            arbA = ArbInteger.small,
            eqN = IntegerAlgebras.eqInt,
            prA = IntegerAlgebras.printableInteger,
            prN = IntegerAlgebras.printableInteger
        ).test()
    }

    "IntegerCommutativeRing satisfies NormSqNegationInvariantLaw" {
        NormSqNegationInvariantLaw(
            neg = ring.add.inverse,
            normSq = ring.normSq,
            arbA = ArbInteger.small,
            eqN = IntegerAlgebras.eqInt,
            prA = IntegerAlgebras.printableInteger,
            prN = IntegerAlgebras.printableInteger
        ).test()
    }

    "IntegerCommutativeRing satisfies NormSqFromConjugationLaw" {
        NormSqFromConjugationLaw(
            normSq = ring.normSq,
            embed = Endo(Identity()),
            mulA = ring.mul.op,
            conj = ring.conj,
            arbA = ArbInteger.small,
            eqA = IntegerAlgebras.eqInt,
            prA = IntegerAlgebras.printableInteger,
            prN = IntegerAlgebras.printableInteger
        ).test()
    }
})
