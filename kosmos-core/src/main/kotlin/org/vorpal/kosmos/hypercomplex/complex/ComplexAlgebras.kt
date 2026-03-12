package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable

/**
 * [ComplexAlgebras] contains the algebraic structures over the [Complex] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [ComplexField]: a complex field with involution (conjugation).
 * - [ComplexStarAlgebra]: the complex star algebra.
 * - [ComplexRealVectorSpace]: the two-dimensional vector space of complex numbers over the real numbers.
 *
 * We have the following homomorphisms:
 * - [RealToComplexMonomorphism]: from the real numbers to the complex numbers.
 * - [Real.toComplex]: convenience method for this monomorphism.
 *
 * We also have the following [Eq]s:
 * - [eqComplexStrict]: strict equality on complex numbers.
 * - [eqComplex]: approximate equality on complex numbers.
 */
object ComplexAlgebras {

    object ComplexField:
        Field<Complex>,
        InvolutiveRing<Complex>,
        RealNormedDivisionAlgebra<Complex> {

        private val base: NonAssociativeInvolutiveRing<Complex> =
            CayleyDickson.usual(RealAlgebras.RealStarField)

        override val add = base.add

        override val mul: CommutativeMonoid<Complex> = CommutativeMonoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override val reciprocal: Endo<Complex> = Endo(Symbols.SLASH) { c ->
            val n2 = normSq(c)
            require(RealAlgebras.eqRealApprox.neqv(n2, 0.0) && n2.isFinite()) { "Zero has no multiplicative inverse in ${Symbols.BB_C}." }
            CD(c.re / n2, -c.im / n2)
        }

        override val conj = base.conj

        override val normSq: UnaryOp<Complex, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ c -> mul(c, conj(c)).re }

        // Disambiguate identities.
        override val zero = base.add.identity
        override val one: Complex = mul.identity
        val i = Complex(0.0, 1.0)
    }

    // Scalars: Real, act componentwise on (a, b)
    val ComplexRealVectorSpace : FiniteVectorSpace<Real, Complex> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = ComplexField.add,
        dimension = 2,
        leftAction = LeftAction { r, (a, b) -> complex(r * a, r * b) }
    )

    val ComplexStarAlgebra: StarAlgebra<Real, Complex> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = ComplexField,
        leftAction = ComplexRealVectorSpace.leftAction
    )

    /** Monomorphisms and type conversions **/
    object RealToComplexMonomorphism: RingMonomorphism<Real, Complex> {
        override val domain = RealAlgebras.RealField
        override val codomain = ComplexField
        override val map = UnaryOp<Real, Complex> { r -> complex(r, 0.0) }
    }

    fun Real.toComplex(): Complex =
        RealToComplexMonomorphism(this)

    /** EQs **/
    val eqComplexStrict: Eq<Complex> = CD.eq(RealAlgebras.eqRealStrict)
    val eqComplex: Eq<Complex> = CD.eq(RealAlgebras.eqRealApprox)

    private fun printableComplexGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>,
    ): Printable<Complex> =
        ComplexPrintable.complexLikePrintable(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            re = { it.re },
            im = { it.im },
            basis = Symbols.IMAGINARY_I,
            prA = prReal,
            eqA = eqReal
        )

    val printableComplex: Printable<Complex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableReal,
            eqReal = RealAlgebras.eqRealApprox
        )

    val printableComplexStrict: Printable<Complex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableRealStrict,
            eqReal = RealAlgebras.eqRealStrict
        )

    val printableComplexPretty: Printable<Complex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableRealPretty,
            eqReal = RealAlgebras.eqRealApprox
        )
}


fun main() {
    val e1 = Complex(1.0, 0.0)
    val e2 = Complex(0.0, 1.0)
    val e3 = Complex(1.0, 1.0)
    val e4 = Complex(1.0, 2.5)
    val e5 = complex(-10.0, 10.0)
    val e6 = complex(10.5, -1.0)
    val e7 = complex(-0.0, -0.0)
    listOf(e1, e2, e3, e4, e5, e6, e7).forEach {
        println(ComplexAlgebras.printableComplexPretty(it))
    }
}
