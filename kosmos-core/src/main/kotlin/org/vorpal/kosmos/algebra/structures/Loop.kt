package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.OpTag

/** A Loop is a Quasigroup with an identity element over the operation. */
interface Loop<A, TAG: OpTag> : Quasigroup<A, TAG> {
    val identity: A
}