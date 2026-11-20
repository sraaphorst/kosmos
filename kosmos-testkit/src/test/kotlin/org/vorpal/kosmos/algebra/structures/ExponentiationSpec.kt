package org.vorpal.kosmos.algebra.structures

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.laws.algebra.IntegerPowerLaws
import org.vorpal.kosmos.laws.algebra.NaturalPowerLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import org.vorpal.kosmos.testing.laws.throwsA
import java.math.BigInteger

/** `(ℤ, ·)` with a counter on the operation. */
private class CountingMonoid {
    var calls = 0
    val monoid: Monoid<BigInteger> = Monoid.of(
        identity = BigInteger.ONE,
        op = BinOp(Symbols.ASTERISK) { a, b -> calls++; a * b }
    )
}

/** `(ℤ, +)` with a counter on the operation. */
private class CountingGroup {
    var calls = 0
    val group: Group<BigInteger> = Group.of(
        identity = BigInteger.ZERO,
        op = BinOp(Symbols.PLUS) { a, b -> calls++; a + b },
        inverse = Endo(Symbols.MINUS) { a -> -a }
    )
}

private fun floorLog2(n: Int): Int = 31 - Integer.numberOfLeadingZeros(n)

/**
 * Exponentiation by squaring: [Monoid.pow] and [Group.zTimes].
 *
 * The power laws check that `n ↦ aⁿ` (and `n ↦ n·x`) are homomorphisms. Besides correctness, the number of
 * monoid operations is checked: computing `aⁿ` must use exactly `⌊log₂ n⌋` squarings and one multiplication per
 * set bit of `n`, with no wasted final squaring.
 */
class ExponentiationSpec : FunSpec({
    val ints: Arb<BigInteger> = Arb.int(-9..9).map(Int::toBigInteger)
    val multiplicative: Monoid<BigInteger> = IntegerAlgebras.IntegerCommutativeRing.mul
    val additive: Group<BigInteger> = IntegerAlgebras.IntegerCommutativeRing.add
    val pow: (BigInteger, Int) -> BigInteger = multiplicative::pow
    val zTimes: (BigInteger, Int) -> BigInteger = { x, n -> additive.zTimes(n.toBigInteger(), x) }

    lawTests(lawGraph("exponentiation by squaring") {
        branch("Monoid.pow") {
            suite(NaturalPowerLaws(multiplicative, pow, ints, maxExponent = 40))

            local("agrees with BigInteger.pow") {
                lawCheckAll(Arb.int(-9..9), Arb.int(0..40)) { a, n ->
                    lawCheck(CountingMonoid().monoid.pow(a.toBigInteger(), n) == a.toBigInteger().pow(n)) { "$a^$n" }
                }
            }
            local("a⁰ is the identity and uses no operations") {
                val counting = CountingMonoid()
                lawCheck(counting.monoid.pow(BigInteger.valueOf(5), 0) == BigInteger.ONE) { "5⁰ ≠ 1" }
                lawCheck(counting.calls == 0) { "5⁰ used ${counting.calls} operations" }
            }
            local("negative exponents are rejected") {
                lawCheck(throwsA<IllegalArgumentException> { CountingMonoid().monoid.pow(BigInteger.TWO, -1) }) {
                    "2^-1 did not throw IllegalArgumentException"
                }
            }
            local("uses ⌊log₂ n⌋ squarings plus one multiplication per set bit, and no more (n ≤ 1000)") {
                (1..1000).forEach { n ->
                    val counting = CountingMonoid()
                    counting.monoid.pow(BigInteger.valueOf(3), n)
                    lawCheck(counting.calls == floorLog2(n) + Integer.bitCount(n)) { "3^$n used ${counting.calls} operations" }
                }
            }
        }

        branch("Group.zTimes") {
            // n·x as the integer power of x in (ℤ, +).
            suite(IntegerPowerLaws(additive, additive.inverse, zTimes, ints, maxExponent = 40))

            local("agrees with integer multiplication, for negative multipliers too") {
                lawCheckAll(Arb.int(-500..500), Arb.int(-50..50)) { n, x ->
                    lawCheck(CountingGroup().group.zTimes(n.toBigInteger(), x.toBigInteger()) == (n * x).toBigInteger()) {
                        "$n · $x"
                    }
                }
            }
            local("never uses more than ⌊log₂ |n|⌋ doublings plus one addition per set bit (n ≤ 1000)") {
                (1..1000).forEach { n ->
                    val counting = CountingGroup()
                    counting.group.zTimes(n.toBigInteger(), BigInteger.valueOf(7))
                    lawCheck(counting.calls <= floorLog2(n) + Integer.bitCount(n)) { "$n · 7 used ${counting.calls} operations" }
                }
            }
        }
    })
})
