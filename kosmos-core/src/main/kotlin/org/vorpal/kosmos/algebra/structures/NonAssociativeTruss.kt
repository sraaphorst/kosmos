package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols

/**
 * A [NonAssociativeTruss] is the affine analogue of a [NonAssociativeRng],
 * obtained by replacing the additive [AbelianGroup] with an [AbelianHeap] while
 * retaining a distributive, not-necessarily-associative multiplication in the form of a [Magma].
 */
interface NonAssociativeTruss<A : Any> {
    val add: AbelianHeap<A>
    val mul: Magma<A>

    companion object {
        fun <A : Any> of(
            add: AbelianHeap<A>,
            mul: Magma<A>
        ): NonAssociativeTruss<A> = object : NonAssociativeTruss<A> {
            override val add = add
            override val mul = mul
        }
    }
}

/**
 * Convert a [NonAssociativeTruss] to a [NonAssociativeRng] by identifying a biunitary element
 * as the identity of the additive group.
 */
fun <A : Any> NonAssociativeTruss<A>.toNonAssociativeRng(
    identity: A,
    symbol: String = Symbols.PLUS
    ): NonAssociativeRng<A> = NonAssociativeRng.of(
        add = add.toAbelianGroup(identity, symbol),
        mul = mul
    )

/**
 * Convert a [NonAssociativeRng] to a [NonAssociativeTruss] by forgetting the identity and converting the
 * additive [AbelianGroup] to an [AbelianHeap].
 */
fun <A : Any> NonAssociativeRng<A>.toNonAssociativeTruss(): NonAssociativeTruss<A> = NonAssociativeTruss.of(
    add = add.toAbelianHeap(),
    mul = mul
)
