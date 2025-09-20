package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.ops.OpTag
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.categories.FiniteSet
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals
import org.vorpal.kosmos.laws.property.CommutativityLaws
import org.vorpal.kosmos.laws.property.IdempotencyLaws

open class QuasigroupLaws<A, S>(
    protected val S: S,
    protected val arb: Arb<A>,
    protected val EQ: Eq<A>
) where S : Quasigroup<A, *> {

    /** Basic identities for left/right division. */
    open suspend fun divisionIdentities() = checkAll(arb, arb) { a, b ->
        // a ⋆ (a \ b) = b
        EQ.assertEquals(S.combine(a, S.ldiv(a, b)), b)
        // (b / a) ⋆ a = b
        EQ.assertEquals(S.combine(S.rdiv(b, a), a), b)
        // a \ (a ⋆ b) = b
        EQ.assertEquals(S.ldiv(a, S.combine(a, b)), b)
        // (b ⋆ a) / a = b
        EQ.assertEquals(S.rdiv(S.combine(b, a), a), b)
    }

    open suspend fun all() {
        divisionIdentities()
    }
}

open class CommutativeQuasigroupLaws<A, S>(
    S: S, arb: Arb<A>, EQ: Eq<A>
) : QuasigroupLaws<A, S>(S, arb, EQ)
        where S : CommutativeQuasigroup<A, *> {

    private val commutativity = CommutativityLaws(S, arb, EQ)

    override suspend fun all() {
        super.all()
        commutativity.holds()
    }
}

open class IdempotentQuasigroupLaws<A, S>(
    S: S, arb: Arb<A>, EQ: Eq<A>
) : QuasigroupLaws<A, S>(S, arb, EQ)
        where S : IdempotentQuasigroup<A, *> {

    private val idempotency = IdempotencyLaws(S, arb, EQ)

    override suspend fun all() {
        super.all()
        idempotency.holds()
    }
}

class CommutativeIdempotentQuasigroupLaws<A, S>(
    S: S, arb: Arb<A>, EQ: Eq<A>
) : QuasigroupLaws<A, S>(S, arb, EQ)
        where S : CommutativeIdempotentQuasigroup<A, out OpTag> {

    // convenience ctor from FiniteSet
    constructor(
        S: S,
        points: FiniteSet<A>,
        EQ: Eq<A>
    ) : this(S, Arb.element(points.toList()), EQ)

    private val commutativity = CommutativityLaws(S, arb, EQ)
    private val idempotency = IdempotencyLaws(S, arb, EQ)

    override suspend fun all() {
        super.all()
        commutativity.holds()
        idempotency.holds()
    }
}