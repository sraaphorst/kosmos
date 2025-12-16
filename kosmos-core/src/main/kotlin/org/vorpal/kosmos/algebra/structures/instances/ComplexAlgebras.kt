package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveAlgebra
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp

typealias Complex = CD<Real>

val Complex.re: Real get() = a
val Complex.im: Real get() = b
fun complex(re: Real, im: Real): Complex = Complex(re, im)

object ComplexAlgebras {
    fun Complex.normSq(): Real =
        re * re + im * im

    object ComplexField:
        Field<Complex>,
        InvolutiveRing<Complex>,
        NormedDivisionAlgebra<Complex> {
        private val base: InvolutiveAlgebra<Complex> =
            CayleyDickson(RealAlgebras.RealInvolutiveRing)

        override val add = base.add

        override val mul: CommutativeMonoid<Complex> = CommutativeMonoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override val reciprocal: Endo<Complex> = Endo(Symbols.SLASH) { c ->
            val normSq = c.normSq()

            // TODO: We probably want a tolerance check here.
            require(normSq != 0.0 && normSq.isFinite()) { "Zero has no multiplicative inverse in ${Symbols.BB_C}." }
            CD(c.re / normSq, -c.im / normSq)
        }

        override val conj = base.conj

        override val normSq: UnaryOp<Complex, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ it.normSq() }

        // Disambiguate zero.
        override val zero = base.add.identity
        override val one: Complex
            get() = mul.identity
        val i = Complex(0.0, 1.0)
    }

    // Scalars: Real, act componentwise on (a, b)
    val ComplexModule : RModule<Real, Complex> = RModule.of(
        scalars = RealField,
        group = ComplexField.add,
        leftAction = LeftAction { r, (a, b) -> complex(r * a, r * b) }
    )

    object ComplexStarAlgebra:
        StarAlgebra<Real, Complex>,
        InvolutiveRing<Complex> by ComplexField,
        RModule<Real, Complex> by ComplexModule {
            override val one = ComplexField.one
        }
}

val eqComplexStrict: Eq<Complex> = CD.eq(Eqs.realStrict)
val eqComplex: Eq<Complex> = CD.eq(Eqs.realApprox())
