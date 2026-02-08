package org.vorpal.kosmos.algebra.poly.structures

import org.vorpal.kosmos.algebra.poly.Poly
import org.vorpal.kosmos.algebra.poly.kernel.PolyKernel
import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.getOrElse

fun <A : Any> Semiring<A>.polySemiring(
    eqA: Eq<A>,
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None
): Semiring<Poly<A>> {
    val zeroA = zero

    val zeroP = Poly.zero<A>()
    val oneP = PolyKernel.constant(one, eqA, zeroA)

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

/**
 * Used to take a [Ring] or a DivisionRing and make it into a polynomial ring.
 */
fun <A : Any> Ring<A>.polyRing(
    eqA: Eq<A>,
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None
): Ring<Poly<A>> {
    val zeroA = zero

    val zeroP = Poly.zero<A>()
    val oneP = PolyKernel.constant(one, eqA, zeroA)

    val addSym = addSymbol.getOrElse(add.op.symbol)
    val addP = BinOp<Poly<A>>(addSym) { p, q ->
        PolyKernel.addPoly(p, q, add.op, eqA, zeroA)
    }
    val addInvP = Endo<Poly<A>>(Symbols.MINUS) { p ->
        PolyKernel.negPoly(p, add.inverse, eqA, zeroA)
    }
    val addAbelianGroup: AbelianGroup<Poly<A>> = AbelianGroup.of(
        zeroP, addP, addInvP
    )

    val mulSym = mulSymbol.getOrElse(mul.op.symbol)
    val mulP = BinOp<Poly<A>>(mulSym) { p, q ->
        PolyKernel.mulPoly(p, q, add.op, mul.op, eqA, zeroA)
    }
    val mulMonoid: Monoid<Poly<A>> = Monoid.of(oneP, mulP)

    return Ring.of(addAbelianGroup, mulMonoid)
}

fun <A : Any> Ring<A>.polyKit(
    eqA: Eq<A>,
    name: String = "x",
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None
): PolyKit<A> =
    PolyKit.fromRing(this, eqA, name, addSymbol, mulSymbol)


/**
 * Used to take a [CommutativeRing] or a Field and make it into a commutative polynomial ring.
 */
fun <A : Any> CommutativeRing<A>.polyCommutativeRing(
    eqA: Eq<A>,
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None
): CommutativeRing<Poly<A>> {
    val zeroA = zero

    val zeroP = Poly.zero<A>()
    val oneP = PolyKernel.constant(one, eqA, zeroA)

    val addSym = addSymbol.getOrElse(add.op.symbol)
    val addP = BinOp<Poly<A>>(addSym) { p, q ->
        PolyKernel.addPoly(p, q, add.op, eqA, zeroA)
    }
    val addInvP = Endo<Poly<A>>(Symbols.MINUS) { p ->
        PolyKernel.negPoly(p, add.inverse, eqA, zeroA)
    }
    val addGroup: AbelianGroup<Poly<A>> = AbelianGroup.of(
        zeroP, addP, addInvP
    )

    val mulSym = mulSymbol.getOrElse(mul.op.symbol)
    val mulP = BinOp<Poly<A>>(mulSym) { p, q ->
        PolyKernel.mulPoly(p, q, add.op, mul.op, eqA, zeroA)
    }
    val mulMonoid: CommutativeMonoid<Poly<A>> = CommutativeMonoid.of(
        oneP, mulP
    )

    return CommutativeRing.of(addGroup, mulMonoid)
}

fun <A : Any> CommutativeRing<A>.polyKit(
    eqA: Eq<A>,
    name: String = "x",
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None
): PolyKit<A> =
    PolyKit.fromCommutativeRing(this, eqA, name, addSymbol, mulSymbol)
