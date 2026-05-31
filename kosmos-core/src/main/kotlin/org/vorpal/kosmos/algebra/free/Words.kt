package org.vorpal.kosmos.algebra.free

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.ops.BinOp

object Words {
    /**
     * Creates the free monoid on alphabet [A], whose elements are finite words
     * and whose operation is concatenation.
     */
    fun <A : Any> monoid(): Monoid<Word<A>> =
        Monoid.of(
            identity = Word.empty(),
            op = BinOp { left, right -> left concatenate right }
        )
}
