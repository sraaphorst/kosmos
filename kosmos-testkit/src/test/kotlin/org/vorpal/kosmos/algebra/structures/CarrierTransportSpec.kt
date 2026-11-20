package org.vorpal.kosmos.algebra.structures

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import java.math.BigInteger

private data class Wrapped<A : Any>(val inner: A)

private fun <A : Any> transport(): CarrierTransport<A, Wrapped<A>> =
    CarrierTransport(
        wrap = UnaryOp { a -> Wrapped(a) },
        unwrap = UnaryOp { w -> w.inner }
    )

private fun <A : Any> doubled(arb: Arb<A>): Arb<CD<A>> =
    Arb.pair(arb, arb).map { (a, b) -> CD(a, b) }

/**
 * Direct tests of [CarrierTransport], independent of the hypercomplex wrappers that use it.
 *
 * A test-local wrapper type is transported from the Cayley-Dickson levels over ℚ, and each
 * transported structure is checked to satisfy the same laws as the original and to agree with it
 * under `unwrap`.
 */
class CarrierTransportSpec : FunSpec({
    val smallRational: Arb<Rational> =
        Arb.pair(Arb.int(-12..12), Arb.int(1..12)).map { (p, d) -> Rational.of(p, d) }

    val level1 = CayleyDickson.commutative(RationalAlgebras.RationalField, Rational.ONE)
    val level2 = CayleyDickson.associative(level1)
    val level3 = CayleyDickson.usual(level2)

    val arb1 = doubled(smallRational)
    val arb2 = doubled(arb1)
    val arb3 = doubled(arb2)

    val ring1 = transport<CD<Rational>>().commutativeInvolutiveRing(level1)
    val wrapped1 = arb1.map { Wrapped(it) }
    val ring2 = transport<CD<CD<Rational>>>().involutiveRing(level2)
    val ring3 = transport<CD<CD<CD<Rational>>>>().nonAssociativeInvolutiveRing(level3)
    val wrapped3 = arb3.map { Wrapped(it) }

    lawTests(lawGraph("carrier transport") {
        branch("transported CommutativeInvolutiveRing") {
            suite(CommutativeRingLaws(ring1, wrapped1))
            suite(InvolutiveRingLaws(ring1, wrapped1))
        }

        branch("transported InvolutiveRing") {
            suite(InvolutiveRingLaws(ring2, arb2.map { Wrapped(it) }))
        }

        branch("transported NonAssociativeInvolutiveRing") {
            suite(NonAssociativeInvolutiveRingLaws(ring3, wrapped3))
            suite(AlternativityLaw(ring3.mul.op, wrapped3))
        }

        local("transported operations agree with the originals under unwrap") {
            lawCheckAll(arb2, arb2) { x, y ->
                lawCheck(ring2.add(Wrapped(x), Wrapped(y)).inner == level2.add(x, y)) { "add" }
                lawCheck(ring2.mul(Wrapped(x), Wrapped(y)).inner == level2.mul(x, y)) { "mul" }
                lawCheck(ring2.conj(Wrapped(x)).inner == level2.conj(x)) { "conj" }
                lawCheck(ring2.add.inverse(Wrapped(x)).inner == level2.add.inverse(x)) { "negation" }
            }
            lawCheck(ring2.zero.inner == level2.zero && ring2.one.inner == level2.one) { "zero or one" }
            lawCheck(ring2.fromBigInt(BigInteger.valueOf(7)).inner == level2.fromBigInt(BigInteger.valueOf(7))) { "fromBigInt" }
        }

        local("transported operators keep their symbols, and maps out of the carrier are transported by `from`") {
            val t = transport<CD<Rational>>()
            lawCheck(t(level1.mul.op).symbol == level1.mul.op.symbol) { "mul symbol" }
            lawCheck(t(level1.conj).symbol == level1.conj.symbol) { "conj symbol" }

            val firstCoordinate = t.from(UnaryOp<CD<Rational>, Rational>("re") { x -> x.a })
            lawCheck(firstCoordinate.symbol == "re") { "from() symbol" }
            lawCheckAll(arb1) { x ->
                lawCheck(firstCoordinate(Wrapped(x)) == x.a) { "re($x)" }
            }
        }

        local("the monoid transports carry the identity across") {
            val t = transport<CD<Rational>>()
            lawCheck(t.abelianGroup(level1.add).identity == Wrapped(level1.zero)) { "abelianGroup" }
            lawCheck(t.monoid(level1.mul).identity == Wrapped(level1.one)) { "monoid" }
            lawCheck(t.commutativeMonoid(level1.mul).identity == Wrapped(level1.one)) { "commutativeMonoid" }
            lawCheck(t.nonAssociativeMonoid(level1.mul).identity == Wrapped(level1.one)) { "nonAssociativeMonoid" }
        }
    })
})
