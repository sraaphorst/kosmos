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
                is Ior.Left -> Ior.Left(left(a.value, b.value))
                is Ior.Right -> Ior.Both(a.value, b.value)
                is Ior.Both -> Ior.Both(left(a.value, b.first), b.second)
            }

            is Ior.Right -> when (b) {
                is Ior.Left -> Ior.Both(b.value, a.value)
                is Ior.Right -> Ior.Right(right(a.value, b.value))
                is Ior.Both -> Ior.Both(b.first, right(a.value, b.second))
            }

            is Ior.Both -> when (b) {
                is Ior.Left -> Ior.Both(left(a.first, b.value), a.second)
                is Ior.Right -> Ior.Both(a.first, right(a.second, b.value))
                is Ior.Both -> Ior.Both(
                    left(a.first, b.first),
                    right(a.second, b.second)
                )
            }
        }
    }
}

