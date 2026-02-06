package org.vorpal.kosmos.algebra.poly.structures

import org.vorpal.kosmos.algebra.poly.Poly
import org.vorpal.kosmos.algebra.poly.kernel.PolyKernel
import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.getOrElse

fun <A : Any> Semiring<A>.polySemiring(
    eqA: Eq<A>,
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None
): Semiring<Poly<A>> {
    val zeroA = zero
    val oneA = one

    val zeroP = Poly.zero<A>()
    val oneP = PolyKernel.constant(oneA, eqA, zeroA)

    val addSym = addSymbol.getOrElse(add.op.symbol)
    val addP = BinOp<Poly<A>>(addSym) { p, q ->
        PolyKernel.addPoly(p, q, add.op, eqA, zeroA)
    }
    val addMonoid: CommutativeMonoid<Poly<A>> = CommutativeMonoid.of(zeroP, addP)

    val mulSym = mulSymbol.getOrElse(mul.op.symbol)
    val mulP = BinOp<Poly<A>>(mulSym) { p, q ->
        PolyKernel.mulPoly(p, q, add.op, mul.op, eqA, zeroA)
    }
    val mulMonoid: Monoid<Poly<A>> = Monoid.of(oneP, mulP)

    return Semiring.of(addMonoid, mulMonoid)
}

fun <A : Any> Semiring<A>.polyKit(
    eqA: Eq<A>,
    name: String = "x",
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None
): PolyKit<A> =
    PolyKit.fromSemiring(this, eqA, name, addSymbol, mulSymbol)
