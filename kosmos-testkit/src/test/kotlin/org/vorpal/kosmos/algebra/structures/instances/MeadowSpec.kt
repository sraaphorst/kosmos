package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.Meadow
import org.vorpal.kosmos.algebra.structures.instances.MeadowAlgebras.ZModMeadow
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.algebra.MeadowLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.verify
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import org.vorpal.kosmos.testing.laws.throwsA
import java.math.BigInteger

private fun residues(n: Int): Arb<BigInteger> =
    Arb.int(0 until n).map(Int::toBigInteger)

/**
 * The concrete [Meadow]s in [MeadowAlgebras], and the field-to-meadow construction [Meadow.fromField].
 */
class MeadowSpec : FunSpec({
    val rationalMeadow = MeadowAlgebras.RationalMeadow
    val rationalArb: Arb<Rational> = Arb.choice(Arb.constant(Rational.ZERO), ArbRational.small)

    lawTests(lawGraph("meadows") {

        branch("ℚ₀") {
            suite(MeadowLaws(rationalMeadow, rationalArb, RationalAlgebras.eqRational, RationalAlgebras.printableRationalPretty))

            local("inv(0) = 0, and inv agrees with the field reciprocal away from 0") {
                lawCheck(rationalMeadow.inv(Rational.ZERO) == Rational.ZERO) { "inv(0) = ${rationalMeadow.inv(Rational.ZERO)}" }
                lawCheckAll(ArbRational.nonZero) { x ->
                    lawCheck(rationalMeadow.inv(x) == RationalAlgebras.RationalField.reciprocal(x)) { "inv($x)" }
                }
            }
            local("a cancellation meadow: x ≠ 0 implies x * inv(x) = 1") {
                lawCheckAll(ArbRational.nonZero) { x ->
                    lawCheck(rationalMeadow.mul(x, rationalMeadow.inv(x)) == Rational.ONE) { "$x * inv($x)" }
                }
            }
        }

        branch("𝔽₂ on Boolean") {
            val meadow = MeadowAlgebras.BoolMeadow
            suite(MeadowLaws(meadow, Arb.boolean()))
            local("inv is the identity") {
                listOf(false, true).forEach { b -> lawCheck(meadow.inv(b) == b) { "inv($b)" } }
            }
        }

        listOf(2, 3, 7, 101).forEach { p ->
            val meadow = MeadowAlgebras.primeFieldMeadow(p.toBigInteger())
            branch("𝔽_$p") {
                suite(MeadowLaws(meadow, residues(p)))
                local("every multiple of $p, not just the literal 0, is sent to 0") {
                    listOf(0, p, -p, 5 * p).map(Int::toBigInteger).forEach { a ->
                        lawCheck(meadow.inv(a) == BigInteger.ZERO) { "inv($a) = ${meadow.inv(a)}" }
                    }
                }
                local("a cancellation meadow (exhaustive)") {
                    (1 until p).map(Int::toBigInteger).forEach { a ->
                        lawCheck(meadow.mul(a, meadow.inv(a)) == BigInteger.ONE) { "$a * inv($a)" }
                    }
                }
                local("agrees with ℤ/${p}ℤ (exhaustive)") {
                    val zmod = ZModMeadow(p.toBigInteger())
                    (0 until p).map(Int::toBigInteger).forEach { a ->
                        lawCheck(zmod.inv(a) == meadow.inv(a)) { "inv($a)" }
                    }
                }
            }
        }

        branch("ℤ/nℤ, n squarefree") {
            listOf(1, 2, 6, 15, 30, 101, 2310).forEach { n ->
                branch("ℤ/${n}ℤ") { suite(MeadowLaws(ZModMeadow(n.toBigInteger()), residues(n))) }
            }

            local("ℤ/30ℤ: inv(x) is the unique y with x²y = x and y²x = y (exhaustive)") {
                val meadow = ZModMeadow(BigInteger.valueOf(30))
                val elements = (0 until 30).map(Int::toBigInteger)
                fun isMeadowInverse(x: BigInteger, y: BigInteger): Boolean =
                    meadow.mul(meadow.mul(x, x), y) == x && meadow.mul(meadow.mul(y, y), x) == y
                elements.forEach { x ->
                    val found = elements.filter { y -> isMeadowInverse(x, y) }
                    lawCheck(found == listOf(meadow.inv(x))) { "meadow inverses of $x: $found, inv($x) = ${meadow.inv(x)}" }
                }
            }
            local("ℤ/6ℤ is not a cancellation meadow: 2 * inv(2) = 4 is a nontrivial idempotent") {
                val meadow = ZModMeadow(BigInteger.valueOf(6))
                val two = BigInteger.TWO
                lawCheck(meadow.inv(two) == two) { "inv(2) = ${meadow.inv(two)}" }
                val e = meadow.mul(two, meadow.inv(two))
                lawCheck(e == BigInteger.valueOf(4) && e != meadow.one && meadow.mul(e, e) == e) { "2 * inv(2) = $e" }
            }
            local("non-canonical representatives are reduced before inverting") {
                val meadow = ZModMeadow(BigInteger.valueOf(30))
                lawCheckAll(Arb.int(-1000..1000)) { k ->
                    val a = k.toBigInteger()
                    lawCheck(meadow.inv(a) == meadow.inv(a.mod(BigInteger.valueOf(30)))) { "inv($a)" }
                }
            }
            local("the Carmichael function of a squarefree modulus is lcm(p - 1)") {
                mapOf(1 to 1, 2 to 1, 6 to 2, 15 to 4, 30 to 4, 101 to 100, 2310 to 60).forEach { (n, lambda) ->
                    val actual = ZModMeadow(n.toBigInteger()).carmichael
                    lawCheck(actual == lambda.toBigInteger()) { "λ($n) = $actual, expected $lambda" }
                }
            }
            local("a modulus that is not squarefree or not positive is rejected") {
                listOf(0, -6, 4, 12, 18, 49, 2 * 3 * 3 * 5).forEach { n ->
                    lawCheck(throwsA<IllegalArgumentException> { ZModMeadow(n.toBigInteger()) }) { "ZModMeadow($n) did not throw" }
                }
            }
            local("isSquarefree agrees with a brute-force check (n ≤ 500)") {
                (1..500).forEach { n ->
                    val bruteForce = (2..n).none { d -> n % (d * d) == 0 }
                    lawCheck(ZModMeadow.isSquarefree(n.toBigInteger()) == bruteForce) { "isSquarefree($n)" }
                }
            }
        }

        branch("Meadow.of") {
            val zmod = ZModMeadow(BigInteger.valueOf(15))
            suite(MeadowLaws(Meadow.of(zmod.add, zmod.mul, zmod.inv), residues(15)))
        }

        branch("MeadowLaws catch broken inverses") {
            local("ℤ/30ℤ with inv the identity fails the restricted inverse law (2 * (2 * 2) = 8 ≠ 2)") {
                val zmod = ZModMeadow(BigInteger.valueOf(30))
                val broken = Meadow.of(zmod.add, zmod.mul, Endo(Symbols.INVERSE) { it })
                val failed = MeadowLaws(broken, residues(30)).verify().problems.map { it.label }
                lawCheck("restricted inverse law (RIL)" in failed) { "failed laws: $failed" }
            }
            local("ℚ with inv(0) = 1 patched in fails (the reflection law breaks)") {
                val broken = Meadow.of(rationalMeadow.add, rationalMeadow.mul, Endo(Symbols.INVERSE) { x ->
                    if (x == Rational.ZERO) Rational.ONE else rationalMeadow.inv(x)
                })
                val report = MeadowLaws(broken, rationalArb).verify()
                lawCheck(report.status == LawStatus.FAILED) { "status ${report.status}" }
            }
        }
    })
})
