package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.OpTag
import org.vorpal.kosmos.core.props.Commutative
import org.vorpal.kosmos.core.props.Idempotent

/** A quasigroup: for all a,b there exist unique x,y with a⋆x=b and y⋆a=b. */
interface Quasigroup<A, TAG : OpTag> : BinOp<A> {
    /** Left division: the unique x with a ⋆ x = b. */
    fun ldiv(a: A, b: A): A
    /** Right division: the unique y with y ⋆ a = b. */
    fun rdiv(b: A, a: A): A
}

/** Commutative quasigroup: a⋆b = b⋆a. */
interface CommutativeQuasigroup<A, TAG : OpTag> :
    Quasigroup<A, TAG>, Commutative

/** Idempotent quasigroup: a⋆a = a. */
interface IdempotentQuasigroup<A, TAG : OpTag> :
    Quasigroup<A, TAG>, Idempotent

/** Both properties. */
interface CommutativeIdempotentQuasigroup<A, TAG : OpTag> :
    CommutativeQuasigroup<A, TAG>, IdempotentQuasigroup<A, TAG>
