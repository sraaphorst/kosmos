package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.AbelianHeap
import org.vorpal.kosmos.algebra.structures.toAbelianGroup
import org.vorpal.kosmos.algebra.structures.toAbelianHeap
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.AbelianHeapLaws
import java.math.BigInteger

private val IntegerAdditive = IntegerAlgebras.IntegerCommutativeRing.add

class IntegerAbelianHeapSpec : StringSpec({
    "IntegerAbelianHeap satisfies AbelianHeapLaws" {
        AbelianHeapLaws(
            heap = IntegerAlgebras.IntegerAbelianHeap,
            arb = ArbInteger.small,
            eq = IntegerAlgebras.eqInteger,
            pr = IntegerAlgebras.printableInteger
        ).fullTest().throwIfFailed()
    }

    "IntegerAdditive.toAbelianHeap() agrees with IntegerAbelianHeap" {
        // After fixing BiunitaryLaw, this is the free property check that
        // AbelianGroup → AbelianHeap → laws holds.
        AbelianHeapLaws(
            heap = IntegerAdditive.toAbelianHeap(),
            arb = ArbInteger.small,
            eq = IntegerAlgebras.eqInteger,
            pr = IntegerAlgebras.printableInteger
        ).fullTest().throwIfFailed()
    }

    "IntegerAbelianHeap.toAbelianGroup(0) recovers IntegerAdditive on operations" {
        val g = IntegerAlgebras.IntegerAbelianHeap.toAbelianGroup(BigInteger.ZERO)
        AbelianGroupLaws(
            group = g,
            arb = ArbInteger.small,
            eq = IntegerAlgebras.eqInteger,
            pr = IntegerAlgebras.printableInteger
        ).fullTest().throwIfFailed()
    }

    "negative test: non-heap is rejected by AbelianHeapLaws" {
        val fake = AbelianHeap.of(TernOp<BigInteger> { x, y, z -> x + y + z })
        shouldThrow<AssertionError> {
            AbelianHeapLaws(
                heap = fake,
                arb = ArbInteger.small,
                eq = IntegerAlgebras.eqInteger,
                pr = IntegerAlgebras.printableInteger
            ).fullTest().throwIfFailed()
        }
    }
})