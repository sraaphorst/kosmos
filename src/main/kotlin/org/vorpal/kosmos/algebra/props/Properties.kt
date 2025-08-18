package org.vorpal.kosmos.algebra.props

import org.vorpal.kosmos.algebra.ops.BinOp
import org.vorpal.kosmos.algebra.ops.OpTag

/** Marker interfaces expressing laws the op claims to satisfy. */
interface Associative
interface Commutative
interface Idempotent

/** One op distributes over that the other op: A’s op distributes on the left over B. */
interface LeftDistributesOver<B : BinOp<*>>
interface RightDistributesOver<B : BinOp<*>>

/** Unital and invertible properties are relative to an op (distinguish by tag). */
interface Unital<A, TAG : OpTag> { val identity: A }
interface Invertible<A, TAG : OpTag> { fun inverse(a: A): A }