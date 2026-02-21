package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

/**
 * Note: since the map from the Rationals to the Reals is subject to floating point errors,
 * we do not provide a vector space, monomorphism, or converter.
 */
object RealAlgebras {
    val RealField: Field<Real> = Field.of(
        add = AbelianGroup.of(
            identity = 0.0,
            op = BinOp(Symbols.PLUS, Real::plus),
            inverse = Endo(Symbols.MINUS, Real::unaryMinus)
        ),
        mul = CommutativeMonoid.of(
            identity = 1.0,
            op = BinOp(Symbols.ASTERISK, Real::times)
        ),
        reciprocal = Endo(Symbols.INVERSE) { x ->
            require(eqRealApprox.neqv(x, 0.0) && x.isFinite()) { "0 has no reciprocal." }
            1.0 / x
        }
    )

    object RealStarField:
        Field<Real> by RealField,
        InvolutiveRing<Real>,
        RealNormedDivisionAlgebra<Real> {

        override val conj: Endo<Real> =
            Endo(Symbols.CONJ, Identity())

        override val normSq: Endo<Real> =
            Endo(Symbols.NORM_SQ_SYMBOL) { a -> a * a }

        // Disambiguate zero and one:
        override val zero: Real
            get() = RealField.zero
        override val one: Real
            get() = RealField.one
    }

    val ZToRMonomorphism: RingMonomorphism<BigInteger, Real> = RingMonomorphism.of(
        IntegerAlgebras.ZCommutativeRing,
        RealField,
        UnaryOp { z -> z.toReal() }
    )

    val eqRealApprox: Eq<Real> = Eqs.realApprox()
    val eqRealStrict: Eq<Real> = Eq { r1, r2 -> r1 == r2 }
}
