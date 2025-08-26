package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.ops.BinOp
import org.vorpal.kosmos.algebra.props.Associative

/** A semigroup is an associative Magma, which is simply a BinOp. No new members added. */
fun interface Semigroup<A> : BinOp<A>, Associative
