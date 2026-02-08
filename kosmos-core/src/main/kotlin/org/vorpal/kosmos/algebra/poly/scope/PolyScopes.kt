package org.vorpal.kosmos.algebra.poly.scope

import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.functional.datastructures.Option

/**
 * Semiring coefficients: only + and * available on Poly.
 */
fun <A : Any, R> Semiring<A>.polyScope(
    eqA: Eq<A>,
    name: String = "x",
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None,
    block: PolyScope<A>.() -> R
): R {
    val kit = PolyKit.fromSemiring(this, eqA, name, addSymbol, mulSymbol)
    return PolyScope(kit).block()
}

/**
 * Ring coefficients: +, *, unary -, and subtraction available. Also supports Int literals
 * because Ring<A> implements HasFromBigInt<A> in your hierarchy.
 */
fun <A : Any, R> Ring<A>.polyScope(
    eqA: Eq<A>,
    name: String = "x",
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None,
    block: PolyIntLiteralScope<A>.() -> R
): R {
    val kit = PolyKit.fromRing(this, eqA, name, addSymbol, mulSymbol)
    return PolyIntLiteralScope(kit, this).block()
}

/**
 * Commutative ring coefficients: same as Ring, but uses the commutative poly factory.
 */
fun <A : Any, R> CommutativeRing<A>.polyScope(
    eqA: Eq<A>,
    name: String = "x",
    addSymbol: Option<String> = Option.None,
    mulSymbol: Option<String> = Option.None,
    block: PolyIntLiteralScope<A>.() -> R
): R {
    val kit = PolyKit.fromCommutativeRing(this, eqA, name, addSymbol, mulSymbol)
    return PolyIntLiteralScope(kit, this).block()
}
