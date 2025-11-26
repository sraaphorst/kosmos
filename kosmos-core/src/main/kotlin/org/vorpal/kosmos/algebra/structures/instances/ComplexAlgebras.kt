package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

typealias Complex = CD<Double>

object ComplexAlgebras {
    fun Complex.normSq(): Real =
        re * re + im * im

    object ComplexField: Field<Complex>, InvolutiveRing<Complex> {
        internal val ComplexInvolutiveRing: InvolutiveRing<Complex> =
            CayleyDickson(RealAlgebras.RealInvolutiveRing)

        override val add = ComplexInvolutiveRing.add

        override val mul: CommutativeMonoid<Complex> = CommutativeMonoid.of(
            identity = ComplexInvolutiveRing.mul.identity,
            op = ComplexInvolutiveRing.mul.op
        )

        override val reciprocal: Endo<Complex> = Endo(Symbols.SLASH) { c ->
            val normSq = c.normSq()
            require(normSq != 0.0) { "Zero has no multiplicative inverse" }
            CD(c.re / normSq, -c.im / normSq)
        }

        override fun fromBigInt(n: BigInteger) = ComplexInvolutiveRing.fromBigInt(n)
        override val conj = ComplexInvolutiveRing.conj

        val i = Complex(0.0, 1.0)
        val zero = Complex(0.0, 0.0)
        val one = Complex(1.0, 0.0)
    }

    val Complex.re: Real get() = a
    val Complex.im: Real get() = b
    fun complex(re: Real, im: Real): Complex = Complex(re, im)

    // Scalars: Double, act componentwise on (a, b)
    object ComplexModule : RModule<Double, Complex> {
        override val ring: CommutativeRing<Double> =
            RealField

        override val group: AbelianGroup<Complex> =
            ComplexField.add

        override val action: Action<Double, Complex> =
            Action { r, (a, b) -> complex(r * a, r * b) }
    }

    object ComplexStarAlgebra:
        StarAlgebra<Real, Complex>,
        InvolutiveRing<Complex> by ComplexField,
        RModule<Real, Complex> by ComplexModule
}