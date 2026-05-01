package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.core.math.Real
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.expm1
import kotlin.math.ln
import kotlin.math.ln1p
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

class DualRealFnsTest : StringSpec({

    fun assertDualClose(
        actual: Dual<Real>,
        expectedF: Real,
        expectedDf: Real,
        tolerance: Real = 1e-10
    ) {
        actual.f shouldBe expectedF.plusOrMinus(tolerance)
        actual.df shouldBe expectedDf.plusOrMinus(tolerance)
    }

    fun checkUnaryLift(
        input: Real,
        seed: Real,
        actual: (Dual<Real>) -> Dual<Real>,
        expectedF: (Real) -> Real,
        expectedDf: (Real) -> Real,
        tolerance: Real = 1e-10
    ) {
        val y = actual(dual(input, seed))

        assertDualClose(
            actual = y,
            expectedF = expectedF(input),
            expectedDf = seed * expectedDf(input),
            tolerance = tolerance
        )
    }

    "sin lifts sine" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::sin,
            expectedF = ::sin,
            expectedDf = ::cos
        )
    }

    "cos lifts cosine" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::cos,
            expectedF = ::cos,
            expectedDf = { a -> -sin(a) }
        )
    }

    "tan lifts tangent" {
        checkUnaryLift(
            input = 0.4,
            seed = -1.75,
            actual = DualRealFns::tan,
            expectedF = ::tan,
            expectedDf = { a ->
                val c = cos(a)
                1.0 / (c * c)
            }
        )
    }

    "sec lifts secant" {
        checkUnaryLift(
            input = 0.4,
            seed = 2.0,
            actual = DualRealFns::sec,
            expectedF = { a -> 1.0 / cos(a) },
            expectedDf = { a ->
                val sec = 1.0 / cos(a)
                sec * tan(a)
            }
        )
    }

    "cot lifts cotangent" {
        checkUnaryLift(
            input = 0.8,
            seed = 2.0,
            actual = DualRealFns::cot,
            expectedF = { a -> cos(a) / sin(a) },
            expectedDf = { a ->
                val s = sin(a)
                -1.0 / (s * s)
            }
        )
    }

    "csc lifts cosecant" {
        checkUnaryLift(
            input = 0.8,
            seed = 2.0,
            actual = DualRealFns::csc,
            expectedF = { a -> 1.0 / sin(a) },
            expectedDf = { a ->
                val csc = 1.0 / sin(a)
                val cot = cos(a) / sin(a)
                -csc * cot
            }
        )
    }

    "exp lifts exponential" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::exp,
            expectedF = ::exp,
            expectedDf = ::exp
        )
    }

    "expm1 lifts exponential minus one" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::expm1,
            expectedF = ::expm1,
            expectedDf = ::exp
        )
    }

    "log lifts natural logarithm" {
        checkUnaryLift(
            input = 1.7,
            seed = 2.5,
            actual = DualRealFns::log,
            expectedF = ::ln,
            expectedDf = { a -> 1.0 / a }
        )
    }

    "log1p lifts natural logarithm of one plus x" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::log1p,
            expectedF = ::ln1p,
            expectedDf = { a -> 1.0 / (1.0 + a) }
        )
    }

    "sqrt lifts square root away from zero" {
        checkUnaryLift(
            input = 2.25,
            seed = 2.5,
            actual = DualRealFns::sqrt,
            expectedF = ::sqrt,
            expectedDf = { a -> 1.0 / (2.0 * sqrt(a)) }
        )
    }

    "sqrt handles zero with zero tangent" {
        val y = DualRealFns.sqrt(dual(0.0, 0.0))

        assertDualClose(
            actual = y,
            expectedF = 0.0,
            expectedDf = 0.0
        )
    }

    "asin lifts arcsine away from boundary" {
        checkUnaryLift(
            input = 0.4,
            seed = 2.5,
            actual = DualRealFns::asin,
            expectedF = ::asin,
            expectedDf = { a -> 1.0 / sqrt(1.0 - a * a) }
        )
    }

    "acos lifts arccosine away from boundary" {
        checkUnaryLift(
            input = 0.4,
            seed = 2.5,
            actual = DualRealFns::acos,
            expectedF = ::acos,
            expectedDf = { a -> -1.0 / sqrt(1.0 - a * a) }
        )
    }

    "asin handles boundary with zero tangent" {
        assertDualClose(
            actual = DualRealFns.asin(dual(1.0, 0.0)),
            expectedF = asin(1.0),
            expectedDf = 0.0
        )

        assertDualClose(
            actual = DualRealFns.asin(dual(-1.0, 0.0)),
            expectedF = asin(-1.0),
            expectedDf = 0.0
        )
    }

    "acos handles boundary with zero tangent" {
        assertDualClose(
            actual = DualRealFns.acos(dual(1.0, 0.0)),
            expectedF = acos(1.0),
            expectedDf = 0.0
        )

        assertDualClose(
            actual = DualRealFns.acos(dual(-1.0, 0.0)),
            expectedF = acos(-1.0),
            expectedDf = 0.0
        )
    }

    "atan lifts arctangent" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::atan,
            expectedF = ::atan,
            expectedDf = { a -> 1.0 / (1.0 + a * a) }
        )
    }

    "sinh lifts hyperbolic sine" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::sinh,
            expectedF = ::sinh,
            expectedDf = ::cosh
        )
    }

    "cosh lifts hyperbolic cosine" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::cosh,
            expectedF = ::cosh,
            expectedDf = ::sinh
        )
    }

    "tanh lifts hyperbolic tangent" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::tanh,
            expectedF = ::tanh,
            expectedDf = { a ->
                val t = tanh(a)
                1.0 - t * t
            }
        )
    }

    "sech lifts hyperbolic secant" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::sech,
            expectedF = { a -> 1.0 / cosh(a) },
            expectedDf = { a ->
                val sech = 1.0 / cosh(a)
                -sech * tanh(a)
            }
        )
    }

    "coth lifts hyperbolic cotangent" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::coth,
            expectedF = { a -> cosh(a) / sinh(a) },
            expectedDf = { a ->
                val s = sinh(a)
                -1.0 / (s * s)
            }
        )
    }

    "csch lifts hyperbolic cosecant" {
        checkUnaryLift(
            input = 0.7,
            seed = 2.5,
            actual = DualRealFns::csch,
            expectedF = { a -> 1.0 / sinh(a) },
            expectedDf = { a ->
                val csch = 1.0 / sinh(a)
                val coth = cosh(a) / sinh(a)
                -csch * coth
            }
        )
    }

    "inv lifts reciprocal" {
        checkUnaryLift(
            input = 1.7,
            seed = 2.5,
            actual = DualRealFns::inv,
            expectedF = { a -> 1.0 / a },
            expectedDf = { a -> -1.0 / (a * a) }
        )
    }

    "sigmoid lifts logistic sigmoid" {
        fun sigmoid(a: Real): Real =
            if (a >= 0.0) {
                val z = exp(-a)
                1.0 / (1.0 + z)
            } else {
                val z = exp(a)
                z / (1.0 + z)
            }

        checkUnaryLift(
            input = -3.0,
            seed = 2.5,
            actual = DualRealFns::sigmoid,
            expectedF = ::sigmoid,
            expectedDf = { a ->
                val s = sigmoid(a)
                s * (1.0 - s)
            }
        )
    }

    "integer pow lifts nonnegative integer powers" {
        val y = DualRealFns.pow(
            x = dual(2.0, 3.0),
            n = 4
        )

        assertDualClose(
            actual = y,
            expectedF = 16.0,
            expectedDf = 3.0 * 4.0 * 8.0
        )
    }

    "integer pow handles zero exponent" {
        val y = DualRealFns.pow(
            x = dual(2.0, 3.0),
            n = 0
        )

        assertDualClose(
            actual = y,
            expectedF = 1.0,
            expectedDf = 0.0
        )
    }

    "real pow lifts real powers" {
        val r = 1.7

        checkUnaryLift(
            input = 2.3,
            seed = 2.5,
            actual = { x -> DualRealFns.pow(x, r) },
            expectedF = { a -> a.pow(r) },
            expectedDf = { a -> r * a.pow(r - 1.0) }
        )
    }

    "composition differentiates exp(sin(x))" {
        val x = dual(
            f = 0.4,
            df = 1.0
        )

        val y = DualRealFns.exp(DualRealFns.sin(x))

        assertDualClose(
            actual = y,
            expectedF = exp(sin(0.4)),
            expectedDf = exp(sin(0.4)) * cos(0.4)
        )
    }

    "sqrt rejects negative real part" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.sqrt(dual(-1.0, 0.0))
        }
    }

    "sqrt rejects nonzero tangent at zero" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.sqrt(dual(0.0, 1.0))
        }
    }

    "log rejects nonpositive real part" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.log(dual(0.0, 1.0))
        }

        shouldThrow<IllegalArgumentException> {
            DualRealFns.log(dual(-1.0, 1.0))
        }
    }

    "log1p rejects real part less than or equal to negative one" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.log1p(dual(-1.0, 1.0))
        }

        shouldThrow<IllegalArgumentException> {
            DualRealFns.log1p(dual(-2.0, 1.0))
        }
    }

    "asin rejects outside domain" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.asin(dual(1.1, 0.0))
        }

        shouldThrow<IllegalArgumentException> {
            DualRealFns.asin(dual(-1.1, 0.0))
        }
    }

    "asin rejects nonzero tangent at boundary" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.asin(dual(1.0, 1.0))
        }

        shouldThrow<IllegalArgumentException> {
            DualRealFns.asin(dual(-1.0, 1.0))
        }
    }

    "acos rejects outside domain" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.acos(dual(1.1, 0.0))
        }

        shouldThrow<IllegalArgumentException> {
            DualRealFns.acos(dual(-1.1, 0.0))
        }
    }

    "acos rejects nonzero tangent at boundary" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.acos(dual(1.0, 1.0))
        }

        shouldThrow<IllegalArgumentException> {
            DualRealFns.acos(dual(-1.0, 1.0))
        }
    }

    "inv rejects zero real part" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.inv(dual(0.0, 1.0))
        }
    }

    "pow rejects negative integer exponent" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.pow(dual(2.0, 1.0), -1)
        }
    }

    "real pow rejects nonpositive real part" {
        shouldThrow<IllegalArgumentException> {
            DualRealFns.pow(dual(0.0, 1.0), 1.7)
        }

        shouldThrow<IllegalArgumentException> {
            DualRealFns.pow(dual(-2.0, 1.0), 1.7)
        }
    }
})