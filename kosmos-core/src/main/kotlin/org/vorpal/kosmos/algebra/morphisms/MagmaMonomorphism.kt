package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Magma
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

interface MagmaMonomorphism<A : Any, B : Any> : MagmaHomomorphism<A, B> {
    infix fun <C : Any> andThen(other: MagmaMonomorphism<B, C>): MagmaMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: MagmaMonomorphism<C, A>): MagmaMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Magma<A>,
            codomain: Magma<B>,
            map: UnaryOp<A, B>,
        ): MagmaMonomorphism<A, B> = object : MagmaMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Magma<A>,
            codomain: Magma<B>,
            map: (A) -> B,
        ): MagmaMonomorphism<A, B> = object : MagmaMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}

fun main() {
    val m: Magma<BigInteger> = IntegerAlgebras.IntegerCommutativeRing.add
    val id: (BigInteger) -> BigInteger = Identity()
    val mm = MagmaMonomorphism.of(m, m, id)
    val mh = MagmaHomomorphism.of(m, m, id)
    val mm2 = mm andThen mm
    val mh2 = mh andThen mh
    val mmmh = mm andThen mh
    val mhmm = mh andThen mm
    val mm2c = mm compose mm
    val mh2c = mh compose mh
    val mmmhc = mm compose mh
    val mhmmc = mh compose mm
}