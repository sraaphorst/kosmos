package org.vorpal.kosmos.algebra.structures

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

/**
 * Exponentiation by squaring: [Monoid.pow] and [Group.zTimes].
 *
 * Besides correctness, the number of monoid operations is checked: computing `aⁿ` must use exactly
 * `⌊log₂ n⌋` squarings and one multiplication per set bit of `n`, with no wasted final squaring.
 */
class ExponentiationSpec : StringSpec({

    /** `(ℤ, ·)` with a counter on the operation. */
    class CountingMonoid {
        var calls = 0
        val monoid: Monoid<BigInteger> = Monoid.of(
            identity = BigInteger.ONE,
            op = BinOp(Symbols.ASTERISK) { a, b -> calls++; a * b }
        )
    }

    /** `(ℤ, +)` with a counter on the operation. */
    class CountingGroup {
        var calls = 0
        val group: Group<BigInteger> = Group.of(
            identity = BigInteger.ZERO,
            op = BinOp(Symbols.PLUS) { a, b -> calls++; a + b },
            inverse = Endo(Symbols.MINUS) { a -> -a }
        )
    }

    fun floorLog2(n: Int): Int = 31 - Integer.numberOfLeadingZeros(n)

    "Monoid.pow agrees with BigInteger.pow" {
        checkAll(Arb.int(-9..9), Arb.int(0..40)) { a, n ->
            CountingMonoid().monoid.pow(a.toBigInteger(), n) shouldBe a.toBigInteger().pow(n)
        }
    }

    "Monoid.pow: a⁰ is the identity and uses no operations" {
        val counting = CountingMonoid()
        counting.monoid.pow(BigInteger.valueOf(5), 0) shouldBe BigInteger.ONE
        counting.calls shouldBe 0
    }

    "Monoid.pow rejects negative exponents" {
        shouldThrow<IllegalArgumentException> { CountingMonoid().monoid.pow(BigInteger.TWO, -1) }
    }

    "Monoid.pow uses ⌊log₂ n⌋ squarings plus one multiplication per set bit, and no more" {
        (1..1000).forEach { n ->
            val counting = CountingMonoid()
            counting.monoid.pow(BigInteger.valueOf(3), n)
            counting.calls shouldBe floorLog2(n) + Integer.bitCount(n)
        }
    }

    "Group.zTimes agrees with integer multiplication, for negative multipliers too" {
        checkAll(Arb.int(-500..500), Arb.int(-50..50)) { n, x ->
            CountingGroup().group.zTimes(n.toBigInteger(), x.toBigInteger()) shouldBe (n * x).toBigInteger()
        }
    }

    "Group.zTimes never uses more than ⌊log₂ |n|⌋ doublings plus one addition per set bit" {
        (1..1000).forEach { n ->
            val counting = CountingGroup()
            counting.group.zTimes(n.toBigInteger(), BigInteger.valueOf(7))
            (counting.calls <= floorLog2(n) + Integer.bitCount(n)) shouldBe true
        }
    }
})
