package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.ops.BinOp
import org.vorpal.kosmos.algebra.ops.OpTag
import org.vorpal.kosmos.algebra.ops.Star
import org.vorpal.kosmos.algebra.props.Commutative
import org.vorpal.kosmos.algebra.props.Idempotent
import org.vorpal.kosmos.categories.FiniteSet
import org.vorpal.kosmos.std.steinerFromBlocks

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


object Quasigroups {
    val FanoPoints = FiniteSet.of(0..6)
    val FanoBlocks = listOf(
        Triple(0, 1, 2),
        Triple(0, 3, 4),
        Triple(0, 5, 6),
        Triple(1, 3, 5),
        Triple(1, 4, 6),
        Triple(2, 3, 6),
        Triple(2, 4, 5)
    )

    val Fano: CommutativeIdempotentQuasigroup<Int, Star> =
        steinerFromBlocks(FanoPoints, FanoBlocks)
}