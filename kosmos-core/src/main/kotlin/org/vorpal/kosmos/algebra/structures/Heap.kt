package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.TernOp

interface Heap<H : Any> {
    val op: TernOp<H>

    companion object {
        fun <H : Any> of(op: TernOp<H>): Heap<H> = object : Heap<H> {
            override val op = op
        }
    }
}

/**
 * Convert a [Heap] to a [Group] by identifying a biunitary element as the group identity.
 *
 * Since, in a heap, every element must be biunitary, any element can be used as the identity.
 */
fun <H : Any> Heap<H>.toGroup(identity: H, symbol: String = Symbols.ASTERISK): Group<H> = Group.of(
    identity = identity,
    op = BinOp(symbol){ a, b -> op(a, identity, b) },
    inverse = Endo { a -> op(identity, a, identity) }
)

/**
 * Convert a [Group] to a [Heap] by forgetting the identity and making the heap operation:
 * ```text
 * [x, y, z] -> xy⁻¹z
 * ```
 */
fun <A : Any> Group<A>.toHeap(): Heap<A> = Heap.of(
    op = TernOp { x, y, z -> op(op(x, inverse(y)), z) }
)
