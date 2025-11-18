package org.vorpal.kosmos.functional.datastructures.algebras

import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.functional.datastructures.Ior


fun <L : Any, R : Any> iorSemigroup(
    left: Semigroup<L>,
    right: Semigroup<R>,
): Semigroup<Ior<L, R>> = object : Semigroup<Ior<L, R>> {
    override val op: BinOp<Ior<L, R>> = BinOp { a, b ->
        when (a) {
            is Ior.Left -> when (b) {
                is Ior.Left -> Ior.Left(left.op(a.value, b.value))
                is Ior.Right -> Ior.Both(a.value, b.value)
                is Ior.Both -> Ior.Both(left.op(a.value, b.first), b.second)
            }

            is Ior.Right -> when (b) {
                is Ior.Left -> Ior.Both(b.value, a.value)
                is Ior.Right -> Ior.Right(right.op(a.value, b.value))
                is Ior.Both -> Ior.Both(b.first, right.op(a.value, b.second))
            }

            is Ior.Both -> when (b) {
                is Ior.Left -> Ior.Both(left.op(a.first, b.value), a.second)
                is Ior.Right -> Ior.Both(a.first, right.op(a.second, b.value))
                is Ior.Both -> Ior.Both(
                    left.op(a.first, b.first),
                    right.op(a.second, b.second)
                )
            }
        }
    }
}

