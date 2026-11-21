package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.AbelianHeap
import org.vorpal.kosmos.algebra.structures.toAbelianGroup
import org.vorpal.kosmos.algebra.structures.toAbelianHeap
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.AbelianHeapLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.verify
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import java.math.BigInteger

/**
 * ℤ as an abelian heap, and the round trips between abelian groups and abelian heaps.
 */
class IntegerAbelianHeapSpec : FunSpec({
    val arb = ArbInteger.small
    val eq = IntegerAlgebras.eqInteger
    val pr = IntegerAlgebras.printableInteger

    lawTests(lawGraph("ℤ as an abelian heap") {
        branch("IntegerAbelianHeap") {
            suite(AbelianHeapLaws(IntegerAlgebras.IntegerAbelianHeap, arb, eq, pr))
        }
        branch("(ℤ, +).toAbelianHeap()") {
            suite(AbelianHeapLaws(IntegerAlgebras.IntegerCommutativeRing.add.toAbelianHeap(), arb, eq, pr))
        }
        branch("IntegerAbelianHeap.toAbelianGroup(0)") {
            suite(AbelianGroupLaws(IntegerAlgebras.IntegerAbelianHeap.toAbelianGroup(BigInteger.ZERO), arb, eq, pr))
        }
        local("AbelianHeapLaws reject x + y + z, which is not a heap") {
            val fake = AbelianHeap.of(TernOp<BigInteger> { x, y, z -> x + y + z })
            val report = AbelianHeapLaws(fake, arb, eq, pr).verify()
            lawCheck(report.status.isProblem) { "x + y + z passed AbelianHeapLaws (${report.status})" }
        }
    })
})
