package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.ops.Mul
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

open class RModuleLaws<S, V, RM>(
    private val M: RModule<S, V, RM>,
    private val arbS: Arb<S>,
    private val arbV: Arb<V>,
    private val EQv: Eq<V>
) where RM : Monoid<S, Mul> {

    // a·(u+v) = a·u + a·v
    suspend fun scalarDistributesOverVectorAdd() =
        checkAll(arbS, arbV, arbV) { a, u, v ->
            EQv.assertEquals(
                M.smul.apply(a, M.add.combine(u, v)),
                M.add.combine(M.smul.apply(a, u), M.smul.apply(a, v))
            )
        }

    // (a+b)·v = a·v + b·v
    suspend fun scalarAddDistributes() =
        checkAll(arbS, arbS, arbV) { a, b, v ->
            EQv.assertEquals(
                M.smul.apply(M.R.add.combine(a, b), v),
                M.add.combine(M.smul.apply(a, v), M.smul.apply(b, v))
            )
        }

    // a·(b·v) = (a*b)·v
    suspend fun associativityWithRingMul() =
        checkAll(arbS, arbS, arbV) { a, b, v ->
            EQv.assertEquals(
                M.smul.apply(a, M.smul.apply(b, v)),
                M.smul.apply(M.R.mul.combine(a, b), v)
            )
        }

    // 1·v = v
    suspend fun unitActsAsIdentity() =
        checkAll(arbV) { v ->
            EQv.assertEquals(M.smul.apply(M.R.mul.identity, v), v)
        }

    // 0_S·v = 0_V  and  a·0_V = 0_V
    suspend fun zeros() =
        checkAll(arbS, arbV) { a, v ->
            EQv.assertEquals(M.smul.apply(M.R.add.identity, v), M.add.identity)
            EQv.assertEquals(M.smul.apply(a, M.add.identity), M.add.identity)
        }

    val additiveGroupOnV = AbelianGroupLaws(M.add, arbV, EQv)

    suspend fun all() {
        additiveGroupOnV.all()
        scalarDistributesOverVectorAdd()
        scalarAddDistributes()
        associativityWithRingMul()
        unitActsAsIdentity()
        zeros()
    }
}