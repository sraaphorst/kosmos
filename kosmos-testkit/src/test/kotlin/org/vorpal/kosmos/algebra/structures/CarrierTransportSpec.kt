package org.vorpal.kosmos.algebra.structures

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.property.AlternativityLaw
import java.math.BigInteger

/**
 * Direct tests of [CarrierTransport], independent of the hypercomplex wrappers that use it.
 *
 * A test-local wrapper type is transported from the Cayley-Dickson levels over ℚ, and each
 * transported structure is checked to satisfy the same laws as the original and to agree with it
 * under `unwrap`.
 */
class CarrierTransportSpec : StringSpec({

    data class Wrapped<A : Any>(val inner: A)

    fun <A : Any> transport(): CarrierTransport<A, Wrapped<A>> =
        CarrierTransport(
            wrap = UnaryOp { a -> Wrapped(a) },
            unwrap = UnaryOp { w -> w.inner }
        )

    val smallRational: Arb<Rational> =
        Arb.pair(Arb.int(-12..12), Arb.int(1..12)).map { (p, d) -> Rational.of(p, d) }

    fun <A : Any> doubled(arb: Arb<A>): Arb<CD<A>> =
        Arb.pair(arb, arb).map { (a, b) -> CD(a, b) }

    val level1 = CayleyDickson.commutative(RationalAlgebras.RationalField, Rational.ONE)
    val level2 = CayleyDickson.associative(level1)
    val level3 = CayleyDickson.usual(level2)

    val arb1 = doubled(smallRational)
    val arb2 = doubled(arb1)
    val arb3 = doubled(arb2)

    "a transported CommutativeInvolutiveRing satisfies the commutative and involutive ring laws" {
        val ring = transport<CD<Rational>>().commutativeInvolutiveRing(level1)
        val arb = arb1.map { Wrapped(it) }
        CommutativeRingLaws(ring, arb).fullTest().throwIfFailed()
        InvolutiveRingLaws(ring, arb).fullTest().throwIfFailed()
    }

    "a transported InvolutiveRing satisfies the involutive ring laws" {
        val ring = transport<CD<CD<Rational>>>().involutiveRing(level2)
        InvolutiveRingLaws(ring, arb2.map { Wrapped(it) }).fullTest().throwIfFailed()
    }

    "a transported NonAssociativeInvolutiveRing satisfies its laws and stays alternative" {
        val ring = transport<CD<CD<CD<Rational>>>>().nonAssociativeInvolutiveRing(level3)
        val arb = arb3.map { Wrapped(it) }
        NonAssociativeInvolutiveRingLaws(ring, arb).fullTest().throwIfFailed()
        AlternativityLaw(ring.mul.op, arb).test()
    }

    "transported operations agree with the originals under unwrap" {
        val t = transport<CD<CD<Rational>>>()
        val ring = t.involutiveRing(level2)
        checkAll(arb2, arb2) { x, y ->
            ring.add(Wrapped(x), Wrapped(y)).inner shouldBe level2.add(x, y)
            ring.mul(Wrapped(x), Wrapped(y)).inner shouldBe level2.mul(x, y)
            ring.conj(Wrapped(x)).inner shouldBe level2.conj(x)
            ring.add.inverse(Wrapped(x)).inner shouldBe level2.add.inverse(x)
        }
        ring.zero.inner shouldBe level2.zero
        ring.one.inner shouldBe level2.one
        ring.fromBigInt(BigInteger.valueOf(7)).inner shouldBe level2.fromBigInt(BigInteger.valueOf(7))
    }

    "transported operators keep their symbols, and maps out of the carrier are transported by `from`" {
        val t = transport<CD<Rational>>()
        t(level1.mul.op).symbol shouldBe level1.mul.op.symbol
        t(level1.conj).symbol shouldBe level1.conj.symbol

        val firstCoordinate = t.from(UnaryOp<CD<Rational>, Rational>("re") { x -> x.a })
        firstCoordinate.symbol shouldBe "re"
        checkAll(arb1) { x ->
            firstCoordinate(Wrapped(x)) shouldBe x.a
        }
    }

    "the monoid transports carry the identity across" {
        val t = transport<CD<Rational>>()
        t.abelianGroup(level1.add).identity shouldBe Wrapped(level1.zero)
        t.monoid(level1.mul).identity shouldBe Wrapped(level1.one)
        t.commutativeMonoid(level1.mul).identity shouldBe Wrapped(level1.one)
        t.nonAssociativeMonoid(level1.mul).identity shouldBe Wrapped(level1.one)
    }
})
