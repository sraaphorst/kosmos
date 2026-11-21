package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.filter
import org.vorpal.kosmos.laws.algebra.GroupLaws
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instance.arbPermMat
import org.vorpal.kosmos.linear.instance.eqPermMat
import org.vorpal.kosmos.linear.instance.prPermMat
import org.vorpal.kosmos.linear.values.PermMat
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Tests for [PermMatAlgebras]: the symmetric group `S_n` and its alternating subgroup `A_n`,
 * realized as compressed permutation matrices.
 *
 * [PermMat] does not override `equals`, so all structural comparisons use [eqPermMat].
 */
class PermMatAlgebrasSpec : FunSpec({
    lawTests(lawGraph("permutation matrices") {
        branch("symmetric group S_n") {
            branch("symmetricGroup satisfies GroupLaws") {
                // n = 4 is non-abelian, so this genuinely exercises a non-commutative group.
                suite(GroupLaws(
                    group = PermMatAlgebras.symmetricGroup(4),
                    arb = arbPermMat(4),
                    eq = eqPermMat,
                    pr = prPermMat
                ))
            }

            local("a permutation composed with its inverse is the identity") {
                val group = PermMatAlgebras.symmetricGroup(3)
                lawCheckAll(arbPermMat(3)) { p ->
                    eqPermMat(group.op(p, group.inverse(p)), group.identity) shouldBe true
                    eqPermMat(group.op(group.inverse(p), p), group.identity) shouldBe true
                }
            }

            local("andThen composes in application order") {
                // p sends 0->1->2->0; q swaps 0 and 1.
                val p = PermMat.of(intArrayOf(1, 2, 0))
                val q = PermMat.of(intArrayOf(1, 0, 2))
                // (p andThen q)(c) = q(p(c)): 0->q(1)=0, 1->q(2)=2, 2->q(0)=1.
                eqPermMat(p andThen q, PermMat.of(intArrayOf(0, 2, 1))) shouldBe true
            }
        }

        branch("alternating subgroup A_n") {
            val arbEven = arbPermMat(4).filter { it.isEven() }

            local("A_n is closed under composition and inverse") {
                val group = PermMatAlgebras.alternatingSubgroup(4)
                lawCheckAll(arbEven, arbEven) { p, q ->
                    group.op(p, q).isEven() shouldBe true
                    group.inverse(p).isEven() shouldBe true
                }
            }

            local("the identity is even") {
                PermMatAlgebras.alternatingSubgroup(4).identity.isEven() shouldBe true
            }
        }
    })
})
