package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.BoundedLattice
import org.vorpal.kosmos.algebra.structures.DistributiveLattice
import org.vorpal.kosmos.algebra.structures.JoinSemilattice
import org.vorpal.kosmos.algebra.structures.Lattice
import org.vorpal.kosmos.algebra.structures.MeetSemilattice
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.pairOp

object JoinSemilattices {
    fun <L : Any, R : Any> product(
        left: JoinSemilattice<L>,
        right: JoinSemilattice<R>
    ): JoinSemilattice<Pair<L, R>> = object : JoinSemilattice<Pair<L, R>> {
        override val join: BinOp<Pair<L, R>> = pairOp(left.join, right.join)
    }

    fun <A : Any> double(
        obj: JoinSemilattice<A>
    ) = product(obj, obj)
}

object MeetSemilattices {
    fun <L : Any, R : Any> product(
        left: MeetSemilattice<L>,
        right: MeetSemilattice<R>
    ): MeetSemilattice<Pair<L, R>> = object : MeetSemilattice<Pair<L, R>> {
        override val meet: BinOp<Pair<L, R>> = pairOp(left.meet, right.meet)
    }

    fun <A : Any> double(
        obj: MeetSemilattice<A>
    ) = product(obj, obj)
}

object Lattices {
    fun <L : Any, R : Any> product(
        left: Lattice<L>,
        right: Lattice<R>
    ): Lattice<Pair<L, R>> = object : Lattice<Pair<L, R>> {
        override val join: BinOp<Pair<L, R>> = pairOp(left.join, right.join)
        override val meet: BinOp<Pair<L, R>> = pairOp(left.meet, right.meet)
    }

    fun <A : Any> double(
        obj: Lattice<A>
    ) = product(obj, obj)
}

object BoundedLattices {
    fun <L : Any, R : Any> product(
        left: BoundedLattice<L>,
        right: BoundedLattice<R>
    ): BoundedLattice<Pair<L, R>> = object : BoundedLattice<Pair<L, R>> {
        override val join: BinOp<Pair<L, R>> = pairOp(left.join, right.join)
        override val meet: BinOp<Pair<L, R>> = pairOp(left.meet, right.meet)
        override val top: Pair<L, R> = Pair(left.top, right.top)
        override val bottom: Pair<L, R> = Pair(left.bottom, right.bottom)
    }

    fun <A : Any> double(
        obj: BoundedLattice<A>
    ) = product(obj, obj)
}

object DistributiveLattices {
    fun <L : Any, R : Any> product(
        left: DistributiveLattice<L>,
        right: DistributiveLattice<R>
    ): DistributiveLattice<Pair<L, R>> = object : DistributiveLattice<Pair<L, R>> {
        override val join: BinOp<Pair<L, R>> = pairOp(left.join, right.join)
        override val meet: BinOp<Pair<L, R>> = pairOp(left.meet, right.meet)
        override val top: Pair<L, R> = Pair(left.top, right.top)
        override val bottom: Pair<L, R> = Pair(left.bottom, right.bottom)
    }

    fun <A : Any> double(
        obj: DistributiveLattice<A>
    ) = product(obj, obj)
}
