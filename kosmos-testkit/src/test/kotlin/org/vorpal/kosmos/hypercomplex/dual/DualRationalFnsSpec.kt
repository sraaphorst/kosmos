package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational

class DualRationalFnsSpec : FunSpec({

    val arbRational: Arb<Rational> =
        Arb.int(-20, 20)
            .map { it.toRational() }

    val arbNonZeroRational: Arb<Rational> =
        arbRational
            .filter { it != Rational.ZERO }

    val arbDualRational: Arb<Dual<Rational>> =
        arbitrary {
            dual(
                f = arbRational.bind(),
                df = arbRational.bind()
            )
        }

    val arbInvertibleDualRational: Arb<Dual<Rational>> =
        arbitrary {
            dual(
                f = arbNonZeroRational.bind(),
                df = arbRational.bind()
            )
        }

    data class MobiusCase(
        val x: Dual<Rational>,
        val a: Rational,
        val b: Rational,
        val c: Rational,
        val d: Rational
    )

    val arbMobiusCase: Arb<MobiusCase> =
        arbitrary {
            while (true) {
                val x = dual(
                    f = arbRational.bind(),
                    df = arbRational.bind()
                )

                val a = arbRational.bind()
                val b = arbRational.bind()
                val c = arbRational.bind()
                val d = arbRational.bind()

                val denominator =
                    c * x.f + d

                if (denominator != Rational.ZERO) {
                    return@arbitrary MobiusCase(
                        x = x,
                        a = a,
                        b = b,
                        c = c,
                        d = d
                    )
                }
            }

            error("unreachable")
        }

    context("Reciprocal") {

        test("inv implements (a + bε)^(-1) = 1/a - b/a² ε") {
            checkAll(arbInvertibleDualRational) { x ->
                val actual =
                    DualRationalFns.inv(x)

                val expected =
                    dual(
                        f = x.f.reciprocal(),
                        df = -x.df * x.f.reciprocal() * x.f.reciprocal()
                    )

                actual shouldBe expected
            }
        }

        test("multiplying by inv gives one") {
            checkAll(arbInvertibleDualRational) { x ->
                val inv =
                    DualRationalFns.inv(x)

                val product =
                    dual(
                        f = x.f * inv.f,
                        df = x.f * inv.df + x.df * inv.f
                    )

                product shouldBe dual(
                    f = Rational.ONE,
                    df = Rational.ZERO
                )
            }
        }

        test("inv rejects zero real part") {
            checkAll(arbRational) { b ->
                shouldThrow<IllegalArgumentException> {
                    DualRationalFns.inv(
                        dual(
                            f = Rational.ZERO,
                            df = b
                        )
                    )
                }
            }
        }
    }

    context("Integer powers") {

        test("pow with exponent 0 gives one with zero tangent") {
            checkAll(arbDualRational) { x ->
                DualRationalFns.pow(x, 0) shouldBe dual(
                    f = Rational.ONE,
                    df = Rational.ZERO
                )
            }
        }

        test("pow implements (a + bε)^n = a^n + n b a^(n - 1) ε for positive n") {
            checkAll(arbDualRational, Arb.int(1, 8)) { x, n ->
                val actual =
                    DualRationalFns.pow(x, n)

                val expected =
                    dual(
                        f = x.f.pow(n),
                        df = n.toRational() * x.df * x.f.pow(n - 1)
                    )

                actual shouldBe expected
            }
        }

        test("pow implements the same formula for negative n when the real part is nonzero") {
            checkAll(arbInvertibleDualRational, Arb.int(-8, -1)) { x, n ->
                val actual =
                    DualRationalFns.pow(x, n)

                val expected =
                    dual(
                        f = x.f.pow(n),
                        df = n.toRational() * x.df * x.f.pow(n - 1)
                    )

                actual shouldBe expected
            }
        }

        test("pow with exponent -1 agrees with inv") {
            checkAll(arbInvertibleDualRational) { x ->
                DualRationalFns.pow(x, -1) shouldBe DualRationalFns.inv(x)
            }
        }

        test("pow rejects Int.MIN_VALUE") {
            checkAll(arbInvertibleDualRational) { x ->
                shouldThrow<IllegalArgumentException> {
                    DualRationalFns.pow(x, Int.MIN_VALUE)
                }
            }
        }
    }

    context("Square and cube") {

        test("square implements (a + bε)^2 = a^2 + 2ab ε") {
            checkAll(arbDualRational) { x ->
                val actual =
                    DualRationalFns.square(x)

                val expected =
                    dual(
                        f = x.f * x.f,
                        df = Rational.TWO * x.f * x.df
                    )

                actual shouldBe expected
            }
        }

        test("square agrees with pow(x, 2)") {
            checkAll(arbDualRational) { x ->
                DualRationalFns.square(x) shouldBe DualRationalFns.pow(x, 2)
            }
        }

        test("cube implements (a + bε)^3 = a^3 + 3a^2b ε") {
            checkAll(arbDualRational) { x ->
                val actual =
                    DualRationalFns.cube(x)

                val expected =
                    dual(
                        f = x.f * x.f * x.f,
                        df = 3.toRational() * x.f * x.f * x.df
                    )

                actual shouldBe expected
            }
        }

        test("cube agrees with pow(x, 3)") {
            checkAll(arbDualRational) { x ->
                DualRationalFns.cube(x) shouldBe DualRationalFns.pow(x, 3)
            }
        }
    }

    context("Möbius / linear fractional transformation") {

        test("mobius implements M(u + vε) = M(u) + v M'(u) ε") {
            checkAll(arbMobiusCase) { case ->
                val x = case.x
                val a = case.a
                val b = case.b
                val c = case.c
                val d = case.d

                val denominator =
                    c * x.f + d

                val numerator =
                    a * x.f + b

                val determinant =
                    a * d - b * c

                val expected =
                    dual(
                        f = numerator / denominator,
                        df = x.df * determinant / (denominator * denominator)
                    )

                val actual =
                    DualRationalFns.mobius(
                        x = x,
                        a = a,
                        b = b,
                        c = c,
                        d = d
                    )

                actual shouldBe expected
            }
        }

        test("mobius with parameters 0, 1, 1, 0 agrees with inv") {
            checkAll(arbInvertibleDualRational) { x ->
                val actual =
                    DualRationalFns.mobius(
                        x = x,
                        a = Rational.ZERO,
                        b = Rational.ONE,
                        c = Rational.ONE,
                        d = Rational.ZERO
                    )

                actual shouldBe DualRationalFns.inv(x)
            }
        }

        test("mobius with c = 0 and d = 1 gives affine map ax + b") {
            checkAll(arbDualRational, arbRational, arbRational) { x, a, b ->
                val actual =
                    DualRationalFns.mobius(
                        x = x,
                        a = a,
                        b = b,
                        c = Rational.ZERO,
                        d = Rational.ONE
                    )

                val expected =
                    dual(
                        f = a * x.f + b,
                        df = x.df * a
                    )

                actual shouldBe expected
            }
        }

        test("mobius rejects zero denominator") {
            checkAll(arbDualRational) { x ->
                shouldThrow<IllegalArgumentException> {
                    DualRationalFns.mobius(
                        x = x,
                        a = Rational.ONE,
                        b = Rational.ZERO,
                        c = Rational.ONE,
                        d = -x.f
                    )
                }
            }
        }
    }
})