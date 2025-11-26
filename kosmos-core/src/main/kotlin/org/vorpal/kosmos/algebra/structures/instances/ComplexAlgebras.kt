package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveAlgebra
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
        private val base: InvolutiveAlgebra<Complex> =
            CayleyDickson(RealAlgebras.RealInvolutiveRing)

        override val add = base.add

        override val mul: CommutativeMonoid<Complex> = CommutativeMonoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override val reciprocal: Endo<Complex> = Endo(Symbols.SLASH) { c ->
            val normSq = c.normSq()
            require(normSq != 0.0) { "Zero has no multiplicative inverse" }
            CD(c.re / normSq, -c.im / normSq)
        }

        override fun fromBigInt(n: BigInteger) = base.fromBigInt(n)
        override val conj = base.conj

        val i = Complex(0.0, 1.0)
        val zero = Complex(0.0, 0.0)
        val one = Complex(1.0, 0.0)
    }

    val Complex.re: Real get() = a
    val Complex.im: Real get() = b
    fun complex(re: Real, im: Real): Complex = Complex(re, im)

    // Scalars: Double, act componentwise on (a, b)
    val ComplexModule : RModule<Double, Complex> = RModule.of(
        ring = RealField,
        group = ComplexField.add,
        action = Action { r, (a, b) -> complex(r * a, r * b) }
    )

    object ComplexStarAlgebra:
        StarAlgebra<Real, Complex>,
        InvolutiveRing<Complex> by ComplexField,
        RModule<Real, Complex> by ComplexModule
}