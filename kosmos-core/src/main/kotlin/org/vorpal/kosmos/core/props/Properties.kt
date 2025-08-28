package org.vorpal.kosmos.core.props

import org.vorpal.kosmos.core.ops.OpTag

/** Marker interfaces expressing laws the op claims to satisfy. */
interface Associative
interface Commutative
interface Idempotent

/** One op distributes over that the other op: A’s op distributes on the left over B. */
interface LeftDistributesOver<Over : OpTag>
interface RightDistributesOver<Over: OpTag>
interface Distributivity<Over : OpTag> : LeftDistributesOver<Over>, RightDistributesOver<Over>
/** Unital and invertible properties are relative to an op (distinguish by tag). */
interface Unital<A, TAG : OpTag> { val identity: A }
interface Invertible<A, TAG : OpTag> { fun inverse(a: A): A }