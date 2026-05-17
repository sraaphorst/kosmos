package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.TernOp

interface AbelianHeap<H : Any> : Heap<H> {
    companion object {
        fun <H : Any> of(op: TernOp<H>): AbelianHeap<H> = object : AbelianHeap<H> {
            override val op = op
        }
    }
}

/**
 * Convert an [AbelianHeap] to an [AbelianGroup] by identifying a biunitary element as the group identity.
 *
 * Since, in a heap, every element must be biunitary, any element can be used as the identity.
 */
fun <H : Any> AbelianHeap<H>.toAbelianGroup(identity: H, symbol: String = Symbols.PLUS): AbelianGroup<H> = AbelianGroup.of(
    identity = identity,
    op = BinOp(symbol){ a, b -> op(a, identity, b) },
    inverse = Endo { a -> op(identity, a, identity) }
)

/**
 * Convert an [AbelianGroup] to an [AbelianHeap] by forgetting the identity and making the heap operation:
 * ```text
 * [x, y, z] -> xy⁻¹z
 * ```
 */
fun <A : Any> AbelianGroup<A>.toAbelianHeap(): AbelianHeap<A> = AbelianHeap.of(
    op = TernOp { x, y, z -> op(op(x, inverse(y)), z) }
)
