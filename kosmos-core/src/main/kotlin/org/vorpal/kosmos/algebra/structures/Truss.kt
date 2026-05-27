package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols

/**
 * A [Truss] is the affine analogue of a [Rng],
 * obtained by replacing the additive [AbelianGroup] with an [AbelianHeap] while
 * retaining a distributive, not-necessarily-associative multiplication in the form of a [Semigroup].
 */
interface Truss<A : Any> : NonAssociativeTruss<A> {
    override val mul: Semigroup<A>

    companion object {
        fun <A : Any> of(
            add: AbelianHeap<A>,
            mul: Semigroup<A>
        ): Truss<A> = object : Truss<A> {
            override val add = add
            override val mul = mul
        }
    }
}

/**
 * Convert a [Truss] to a [Rng] by identifying a biunitary element
 * as the identity of the additive group.
 */
fun <A : Any> Truss<A>.toRng(
    identity: A,
    symbol: String = Symbols.PLUS
): Rng<A> = Rng.of(
    add = add.toAbelianGroup(identity, symbol),
    mul = mul
)

/**
 * Convert a [Rng] to a [Truss] by forgetting the identity and converting the
 * additive [AbelianGroup] to an [AbelianHeap].
 */
fun <A : Any> Rng<A>.toTruss(): Truss<A> = Truss.of(
    add = add.toAbelianHeap(),
    mul = mul
)
