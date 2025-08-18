package org.vorpal.kosmos.algebra

import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple

// Associativity domain chooser.
sealed interface AssocGen<A> {
    fun triples(): Arb<Triple<A, A, A>>
}

data class FromSingle<A>(val elem: Arb<A>) : AssocGen<A> {
    override fun triples(): Arb<Triple<A, A, A>> = Arb.triple(elem, elem, elem)
}

data class FromTriple<A>(val tripleArb: Arb<Triple<A, A, A>>): AssocGen<A> {
    override fun triples(): Arb<Triple<A, A, A>> = tripleArb
}

// Convenience helpers.
@JvmName("assocFromElem")
fun <A> assocFrom(elem: Arb<A>): AssocGen<A> = FromSingle(elem)

@JvmName("assocFromTriples")
fun <A> assocFrom(triple: Arb<Triple<A, A, A>>): AssocGen<A> = FromTriple(triple)
