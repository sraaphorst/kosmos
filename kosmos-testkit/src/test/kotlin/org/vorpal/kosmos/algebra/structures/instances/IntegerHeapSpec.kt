package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Heap
import org.vorpal.kosmos.algebra.structures.toGroup
import org.vorpal.kosmos.algebra.structures.toHeap
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.laws.algebra.HeapLaws
import java.math.BigInteger

private val IntegerAdditive = IntegerAlgebras.IntegerCommutativeRing.add

class IntegerHeapSpec : StringSpec({
    "IntegerHeap satisfies HeapLaws" {
        HeapLaws(
            heap = IntegerAlgebras.IntegerHeap,
            arb = ArbInteger.small,
            eq = IntegerAlgebras.eqInteger,
            pr = IntegerAlgebras.printableInteger
        ).fullTest().throwIfFailed()
    }

    "IntegerAdditive.toHeap() agrees with IntegerHeap" {
        // After fixing BiunitaryLaw, this is the free property check that
        // Group → Heap → laws holds.
        HeapLaws(
            heap = IntegerAdditive.toHeap(),
            arb = ArbInteger.small,
            eq = IntegerAlgebras.eqInteger,
            pr = IntegerAlgebras.printableInteger
        ).fullTest().throwIfFailed()
    }

    "IntegerHeap.toGroup(0) recovers IntegerAdditive on operations" {
        val g = IntegerAlgebras.IntegerHeap.toGroup(BigInteger.ZERO)

        checkAll(ArbInteger.small, ArbInteger.small) { a, b ->
            g.op(a, b) shouldBe IntegerAdditive.op(a, b)
        }
        checkAll(ArbInteger.small) { a ->
            g.inverse(a) shouldBe IntegerAdditive.inverse(a)
        }
    }

    "negative test: non-heap is rejected by HeapLaws" {
        val fake = Heap.of(TernOp<BigInteger> { x, y, z -> x + y + z })
        shouldThrow<AssertionError> {
            HeapLaws(
                heap = fake,
                arb = ArbInteger.small,
                eq = IntegerAlgebras.eqInteger,
                pr = IntegerAlgebras.printableInteger
            ).fullTest().throwIfFailed()
        }
    }
})