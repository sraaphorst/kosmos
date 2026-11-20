package org.vorpal.kosmos.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.arbitrary.int
import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras.RationalField
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras.RationalStarField
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.div
import org.vorpal.kosmos.core.rational.minus
import org.vorpal.kosmos.core.rational.plus
import org.vorpal.kosmos.core.rational.times
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.relations.instances.RationalRelations
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.algebra.AbsoluteValueLaws
import org.vorpal.kosmos.laws.algebra.IntegerPowerLaws
import org.vorpal.kosmos.laws.algebra.NormedDivisionAlgebraLaws
import org.vorpal.kosmos.laws.algebra.OrderedFieldLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.InvolutionLaw
import org.vorpal.kosmos.laws.relation.StrictTotalOrderLaws
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import org.vorpal.kosmos.testing.laws.throwsA
import java.math.BigInteger

private val q = RationalField
private val arb = ArbRational.mixed
private val eq = RationalAlgebras.eqRational
private val pr = RationalAlgebras.printableRationalPretty
private val z = IntegerAlgebras.IntegerCommutativeRing
private val eqZ = IntegerAlgebras.eqInteger
private val prZ = IntegerAlgebras.printableInteger
private val ZToQMonomorphism = IntegerAlgebras.ZToQMonomorphism
private val absRational = Endo<Rational>("|·|") { it.abs() }

/**
 * ℚ, as one law graph: the structures it forms (an ordered field, a ℤ-module, a star algebra and normed division
 * algebra over itself), its absolute value and integer powers, the embedding ℤ → ℚ, and the laws of the `Rational`
 * type itself (normal form, rounding, rendering, hashing, errors).
 *
 * The structure laws draw from [ArbRational.mixed], small and 64-bit rationals together.
 */
class RationalSpec : FunSpec({

    lawTests(lawGraph("ℚ") {

        branch("structures") {
            suite(OrderedFieldLaws(q, RationalRelations.RationalTotalOrder, arb, eq, pr))
            suite(StrictTotalOrderLaws(RationalRelations.RationalTotalStrictOrder.strictRelation, arb, eq, pr))
            suite(RModuleLaws(RationalAlgebras.ZModuleRational, ArbInteger.small, arb, eqZ, eq, prZ, pr))
            suite(StarAlgebraLaws(RationalStarField, arb, arb, eq, eq, pr, pr))
            suite(NormedDivisionAlgebraLaws(RationalStarField, arb, q.mul.op, eq, eq, pr, pr))
            suite(AbsoluteValueLaws(q, RationalRelations.RationalTotalOrder, absRational, arb, eq, pr))
            suite(IntegerPowerLaws(q.mul, q.reciprocal, Rational::pow, arb, Rational.ZERO, 10, eq, pr))
            law(InvolutionLaw(q.add.inverse, arb, eq, pr))
            law(InvolutionLaw(q.reciprocal, ArbRational.nonZero, eq, pr))
        }

        branch("embedding ℤ → ℚ") {
            suite(RingHomomorphismLaws(ZToQMonomorphism.map::invoke, z, q, ArbInteger.small, eq, prZ, pr))
            law(injectivityLaw(ZToQMonomorphism.map::invoke, ArbInteger.small, eqZ, eq, prZ, pr))
            local("fromBigInt is the embedding") {
                lawCheckAll(ArbInteger.small) { n ->
                    lawCheck(q.fromBigInt(n) == ZToQMonomorphism.map(n) && n.toRational() == ZToQMonomorphism.map(n)) {
                        "fromBigInt($n) = ${q.fromBigInt(n)}, toRational = ${n.toRational()}, embedding = ${ZToQMonomorphism.map(n)}"
                    }
                }
            }
            local("arithmetic with integers agrees with the embedding") {
                lawCheckAll(arb, ArbInteger.small) { r, n ->
                    val m = n.toRational()
                    lawCheck(r + n == r + m && n + r == m + r) { "$r + $n" }
                    lawCheck(r - n == r - m && n - r == m - r) { "$r - $n" }
                    lawCheck(r * n == r * m && n * r == m * r) { "$r * $n" }
                    lawCheck(r + n.toInt() == r + m && r - n.toLong() == r - m && r * n.toInt() == r * m) {
                        "$r with Int/Long $n"
                    }
                }
            }
            local("division by an integer agrees with the embedding") {
                lawCheckAll(arb, ArbInteger.nonZeroSmall) { r, n ->
                    lawCheck(r / n == r / n.toRational() && r / n.toInt() == r / n.toRational()) { "$r / $n" }
                }
            }
            local("an integer divided by a rational agrees with the embedding") {
                lawCheckAll(ArbInteger.small, ArbRational.nonZero) { n, r ->
                    lawCheck(n / r == n.toRational() / r) { "$n / $r" }
                }
            }
        }

        branch("normal form") {
            local("reduced: gcd(n, d) = 1") {
                lawCheckAll(arb) { r -> lawCheck(r.n.gcd(r.d) == BigInteger.ONE) { "$r: gcd = ${r.n.gcd(r.d)}" } }
            }
            local("positive denominator") {
                lawCheckAll(arb) { r -> lawCheck(r.d.signum() == 1) { "$r has denominator ${r.d}" } }
            }
            local("every presentation n/d normalizes to the same value and hash") {
                lawCheckAll(ArbInteger.small, ArbInteger.nonZeroSmall, ArbInteger.nonZeroSmall) { n, d, k ->
                    val r = Rational.of(n, d)
                    val scaled = Rational.of(n * k, d * k)
                    val negated = Rational.of(-n, -d)
                    lawCheck(r == scaled && r == negated) { "$n/$d = $r, but ${n * k}/${d * k} = $scaled, ${-n}/${-d} = $negated" }
                    lawCheck(r.hashCode() == scaled.hashCode() && r.hashCode() == negated.hashCode()) {
                        "equal rationals $r with different hash codes"
                    }
                }
            }
            local("negating numerator and denominator keeps the value") {
                lawCheckAll(arb) { r ->
                    val flipped = Rational.of(-r.n, -r.d)
                    lawCheck(r == flipped && r.hashCode() == flipped.hashCode()) { "$r, but ${-r.n}/${-r.d} = $flipped" }
                }
            }
            local("the factories agree") {
                lawCheckAll(ArbInteger.small, ArbInteger.nonZeroSmall) { n, d ->
                    val r = Rational.of(n)
                    lawCheck(r == Rational.of(n, BigInteger.ONE) && r == Rational.of(n.toLong()) && r == Rational.of(n.toInt())) {
                        "of($n) disagrees between BigInteger, Long and Int"
                    }
                    lawCheck(Rational.of(n, d) == Rational.of(n.toInt(), d.toInt()) && Rational.of(n, d) == Rational.of(n.toLong(), d.toLong())) {
                        "of($n, $d) disagrees between BigInteger, Long and Int"
                    }
                }
            }
            local("constants") {
                lawCheck(q.zero == Rational.ZERO && q.one == Rational.ONE) { "field constants are not Rational.ZERO and Rational.ONE" }
                lawCheck(Rational.TWO == Rational.of(2) && Rational.TEN == Rational.of(10) && Rational.NEG_ONE == Rational.of(-1)) {
                    "a named constant has the wrong value"
                }
            }
        }

        branch("operators") {
            local("subtraction is addition of the negation") {
                lawCheckAll(arb, arb) { a, b -> lawCheck(a - b == a + (-b)) { "$a - $b = ${a - b}, $a + (-$b) = ${a + (-b)}" } }
            }
            local("division is multiplication by the reciprocal") {
                lawCheckAll(arb, ArbRational.nonZeroWide) { a, b ->
                    lawCheck(a / b == a * b.reciprocal()) { "$a / $b = ${a / b}, $a * $b⁻¹ = ${a * b.reciprocal()}" }
                }
            }
            local("the operators agree with the field") {
                lawCheckAll(arb, arb) { a, b ->
                    lawCheck(a + b == q.add(a, b) && a * b == q.mul(a, b) && -a == q.add.inverse(a)) { "$a, $b" }
                }
            }
            local("compareTo agrees with the sign of the difference") {
                lawCheckAll(arb, arb) { a, b ->
                    lawCheck(a.compareTo(b).let { if (it > 0) 1 else if (it < 0) -1 else 0 } == (a - b).signum) {
                        "compare($a, $b) = ${a.compareTo(b)}, but $a - $b = ${a - b}"
                    }
                }
            }
        }

        branch("rounding") {
            local("floor(r) ≤ r < floor(r) + 1") {
                lawCheckAll(arb) { r ->
                    val f = r.floor().toRational()
                    lawCheck(f <= r && r < f + Rational.ONE) { "floor($r) = $f" }
                }
            }
            local("ceil(r) - 1 < r ≤ ceil(r)") {
                lawCheckAll(arb) { r ->
                    val c = r.ceil().toRational()
                    lawCheck(c - Rational.ONE < r && r <= c) { "ceil($r) = $c" }
                }
            }
            local("floor and ceil are the identity on integers") {
                lawCheckAll(ArbRational.integer) { r -> lawCheck(r.floor() == r.n && r.ceil() == r.n) { "floor/ceil($r)" } }
            }
            local("frac(r) lies in [0, 1)") {
                lawCheckAll(arb) { r ->
                    val f = r.frac()
                    lawCheck(f >= Rational.ZERO && f < Rational.ONE) { "frac($r) = $f" }
                }
            }
            local("r mod m lies in [0, m) for m > 0") {
                lawCheckAll(arb, ArbRational.positive) { r, m ->
                    val rem = r % m
                    lawCheck(rem >= Rational.ZERO && rem < m) { "$r mod $m = $rem" }
                }
            }
            local("r = m · floor(r / m) + (r mod m) for m > 0") {
                lawCheckAll(arb, ArbRational.positive) { r, m ->
                    val k = (r / m).floor().toRational()
                    lawCheck(r == m * k + (r % m)) { "$r ≠ $m · $k + ${r % m}" }
                }
            }
        }

        branch("predicates and conversions") {
            local("isInteger, isZero, isPositive, isNegative and signum agree with the numerator") {
                lawCheckAll(arb) { r ->
                    lawCheck(r.isInteger == (r.d == BigInteger.ONE)) { "isInteger($r)" }
                    lawCheck(r.isZero == (r.n.signum() == 0)) { "isZero($r)" }
                    lawCheck(r.isPositive == (r.n.signum() > 0) && r.isNegative == (r.n.signum() < 0)) { "sign predicates of $r" }
                    lawCheck(r.signum == r.n.signum()) { "signum($r) = ${r.signum}" }
                }
            }
            local("integers round-trip through a zero-scale BigDecimal") {
                lawCheckAll(ArbInteger.small) { n ->
                    lawCheck(n.toRational().toBigDecimal(0).toBigInteger() == n) { "$n" }
                }
            }
            local("toString is n for integers and n/d otherwise") {
                lawCheckAll(arb) { r ->
                    val expected = if (r.d == BigInteger.ONE) r.n.toString() else "${r.n}/${r.d}"
                    lawCheck(r.toString() == expected) { "toString = ${r}, expected $expected" }
                }
            }
        }

        branch("errors") {
            local("a zero denominator throws ArithmeticException") {
                lawCheckAll(ArbInteger.small) { n ->
                    lawCheck(throwsA<ArithmeticException> { Rational.of(n, BigInteger.ZERO) }) { "$n/0 did not throw" }
                    lawCheck(throwsA<ArithmeticException> { Rational.of(n.toInt(), 0) }) { "$n/0 (Int) did not throw" }
                }
            }
            local("zero has no reciprocal (ArithmeticException)") {
                lawCheck(throwsA<ArithmeticException> { Rational.ZERO.reciprocal() }) { "Rational.ZERO.reciprocal() did not throw" }
                lawCheck(throwsA<ArithmeticException> { q.reciprocal(Rational.ZERO) }) { "the field's reciprocal of 0 did not throw" }
            }
            local("division by zero throws ArithmeticException") {
                lawCheckAll(arb) { r -> lawCheck(throwsA<ArithmeticException> { r / Rational.ZERO }) { "$r / 0 did not throw" } }
            }
            local("mod by a nonpositive rational throws IllegalArgumentException") {
                lawCheckAll(arb, ArbRational.negative) { r, m ->
                    lawCheck(throwsA<IllegalArgumentException> { r % Rational.ZERO }) { "$r mod 0 did not throw" }
                    lawCheck(throwsA<IllegalArgumentException> { r % m }) { "$r mod $m did not throw" }
                }
            }
            local("negative powers of zero throw ArithmeticException") {
                lawCheckAll(Arb.int(1..10)) { n ->
                    lawCheck(throwsA<ArithmeticException> { Rational.ZERO.pow(-n) }) { "0^-$n did not throw" }
                }
            }
        }
    })
})
