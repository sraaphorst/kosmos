package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.structures.instances.Real
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

/**
 * A ring coupled with an involution referred to as conjugation, which has the following properties:
 * 1. `conj(conj(a)) = a`
 * 2. `conj(add(a, b)) == add(conj(a), conj(b))`
 * 3. `conj(mul(a, b)) == mul(conj(b), conj(a))`
 * 4. `conj(mul.identity) == conj(mult.identity)`
 *
 * More legibly:
 * 1. `(a*)* == a`
 * 2. `(a + b)* == a* + b*`
 * 3. `(ab)* == b*a*`
 * 4. `1* = 1`
 */
interface InvolutiveRing<A : Any> : Ring<A> {
    /**
     * The involution of the ring, x ↦ x*.
     */
    val conj: Endo<A>
}

object RealInvolutiveRing: InvolutiveRing<Double>, Field<Double> by RealField {
    override val conj: Endo<Double> = Endo("conj", Identity())
}

/**
 * We get the scalar ring, CommutativeRing<R> from the Algebra<R, A>.
 * We get the ring on A and conj: Endo<A> from InvolutiveRing<A>.
 */
interface StarAlgebra<R : Any, A : Any> : Algebra<R, A>, InvolutiveRing<A>

/**
 * The Cayley-Dickson elements: a Pair over the CommutativeRing.
 */
data class CD<A: Any>(val a: A, val b: A)

/**
 * The generic doubling step.
 *
 * It always gives (at least) an InvolutiveRing<CD<A>>.
 *
 * It doesn't try to decide "this is a Field, this is only a Ring, etc."
 * We will do tha that per stage when we know the algebraic facts by wrapping
 * it, e.g., in a Field<Complex>.
 */
class CayleyDickson<A : Any>(private val base: InvolutiveRing<A>) : InvolutiveRing<CD<A>> {
    override val add: AbelianGroup<CD<A>> = AbelianGroup.of<CD<A>>(
        identity = CD(base.add.identity, base.add.identity),
        op = BinOp(Symbols.PLUS) { (a, b), (c, d) ->
            CD(base.add.op(a, b), base.add.op(c, d)) },
        inverse = Endo(Symbols.MINUS) { (a, b) ->
            CD(base.add.inverse(a), base.add.inverse(b))}
    )

    override val mul: Monoid<CD<A>> = Monoid.of(
        identity = CD(base.mul.identity, base.add.identity),
        op = BinOp<CD<A>>(Symbols.ASTERISK) { (a, b), (c, d) ->
            // (a, b)(c, d) = (ac - db*, a*d + cb)
            val ac = base.mul.op(a, c)
            val dbs = base.add.inverse(base.mul.op(d, base.conj(b)))
            val first = base.add.op(ac, dbs)

            val asd = base.mul.op(base.conj(a), d)
            val cb = base.mul.op(c, b)
            val second = base.add.op(asd, cb)

            CD(first, second)
        }
    )

    override fun fromBigInt(n: BigInteger): CD<A> =
        CD(base.fromBigInt(n), base.add.identity)

    override val conj = Endo<CD<A>>("conj") { (a, b) ->
        CD(base.conj(a), base.add.inverse(b))
    }
}

typealias Complex = CD<Double>

object ComplexField: Field<Complex>, InvolutiveRing<Complex> {
    internal val ComplexInvolutiveRing: InvolutiveRing<Complex> = CayleyDickson(RealInvolutiveRing)

    override val add = ComplexInvolutiveRing.add

    override val mul: CommutativeMonoid<Complex> = CommutativeMonoid.of(
        identity = ComplexInvolutiveRing.mul.identity,
        op = ComplexInvolutiveRing.mul.op,
    )

    override val reciprocal: Endo<Complex> = Endo(Symbols.SLASH) { (a, b) ->
        val normSq = a * a + b * b
        require(normSq != 0.0) { "Zero has no multiplicative inverse" }
        CD(a / normSq, -b / normSq)
    }

    override fun fromBigInt(n: BigInteger) = ComplexInvolutiveRing.fromBigInt(n)
    override val conj = ComplexInvolutiveRing.conj
}

val Complex.re: Real get() = a
val Complex.im: Real get() = b
fun complex(re: Real, im: Real): Complex = Complex(re, im)
