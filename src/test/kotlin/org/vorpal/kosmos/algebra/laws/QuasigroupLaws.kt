package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

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

    open suspend fun all() { divisionIdentities() }
}

open class CommutativeQuasigroupLaws<A, S>(
    S: S, arb: Arb<A>, EQ: Eq<A>
) : QuasigroupLaws<A, S>(S, arb, EQ)
        where S : CommutativeQuasigroup<A, *> {

    open suspend fun commutativity() = checkAll(arb, arb) { a, b ->
        EQ.assertEquals(S.combine(a, b), S.combine(b, a))
    }

    override suspend fun all() {
        super.all()
        commutativity()
    }
}

open class IdempotentQuasigroupLaws<A, S>(
    S: S, arb: Arb<A>, EQ: Eq<A>
) : QuasigroupLaws<A, S>(S, arb, EQ)
        where S : IdempotentQuasigroup<A, *> {

    open suspend fun idempotency() = checkAll(arb) { a ->
        EQ.assertEquals(S.combine(a, a), a)
    }

    override suspend fun all() {
        super.all()
        idempotency()
    }
}

class CommutativeIdempotentQuasigroupLaws<A, S>(
    S: S, arb: Arb<A>, EQ: Eq<A>
) : QuasigroupLaws<A, S>(S, arb, EQ)
        where S : CommutativeIdempotentQuasigroup<A, *> {

    private val comm = CommutativityLaws(S, arb, EQ) // you already have this
    private val idemp = IdempotentQuasigroupLaws(S, arb, EQ)

    suspend fun commutativity() = comm.holds()
    suspend fun idempotency() = idemp.idempotency()

    override suspend fun all() {
        super.all()
        commutativity()
        idempotency()
    }
}