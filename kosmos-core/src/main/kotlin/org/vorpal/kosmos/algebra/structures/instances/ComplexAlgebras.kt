package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
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
    private val eqRealApprox = Eqs.realApprox()

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
            require(eqRealApprox.neqv(n2, 0.0) && n2.isFinite()) { "Zero has no multiplicative inverse in ${Symbols.BB_C}." }
            CD(c.re / n2, -c.im / n2)
        }

        override val conj = base.conj

        override val normSq: UnaryOp<Complex, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ c -> mul(c, conj(c)).re }

        // Disambiguate zero.
        override val zero = base.add.identity
        override val one: Complex = mul.identity
        val i = Complex(0.0, 1.0)
    }

    // Scalars: Real, act componentwise on (a, b)
    val ComplexRealVectorSpace : FiniteVectorSpace<Real, Complex> = FiniteVectorSpace.of(
        scalars = RealField,
        add = ComplexField.add,
        dimension = 2,
        leftAction = LeftAction { r, (a, b) -> complex(r * a, r * b) }
    )

    val ComplexStarAlgebra: StarAlgebra<Real, Complex> = StarAlgebra.of(
        scalars = RealField,
        involutiveRing = ComplexField,
        leftAction = ComplexRealVectorSpace.leftAction
    )
}

val eqComplexStrict: Eq<Complex> = CD.eq(Eqs.realStrict)
val eqComplex: Eq<Complex> = CD.eq(Eqs.realApprox())
