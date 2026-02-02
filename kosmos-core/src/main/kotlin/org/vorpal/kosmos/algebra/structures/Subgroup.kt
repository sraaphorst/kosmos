package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.morphisms.GroupMonomorphism
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

/**
 * A [Subgroup] of a given [Group]. Contains a reference to the parent [Group] and a function to determine
 * membership within this subgroup.
 */
interface Subgroup<A : Any> : Group<A> {
    val parent: Group<A>
    val isMember: (A) -> Boolean
}

class PredicateSubgroup<A : Any>(
    override val parent: Group<A>,
    override val isMember: (A) -> Boolean,
) : Subgroup<A> {

    init {
        require(isMember(parent.identity)) {
            "identity of a group is not a member of the subgroup"
        }
    }

    private fun requireMember(x: A) {
        require(isMember(x)) {
            "tried to use $x as an element of a subgroup, yet it is not in the subgroup"
        }
    }

    override val identity: A =
        parent.identity

    override val op: BinOp<A> =
        BinOp(parent.op.symbol) { x, y ->
            requireMember(x)
            requireMember(y)
            val z = parent.op(x, y)
            requireMember(z)
            z
        }

    override val inverse: Endo<A> =
        Endo(parent.inverse.symbol) { x ->
            requireMember(x)
            val inv = parent.inverse(x)
            requireMember(inv)
            inv
        }
}

fun <A : Any> Group<A>.subgroup(
    isMember: (A) -> Boolean
): Subgroup<A> =
    PredicateSubgroup(
        parent = this,
        isMember = isMember
    )

/**
 * Once we have a [Subgroup] of a [Group], then we inherently have a [GroupMonomorphism]
 * from the [Subgroup] into the [Group].
 */
fun <A : Any> Subgroup<A>.inclusion(): GroupMonomorphism<A, A> = GroupMonomorphism.of(
    domain = this,
    codomain = parent,
    map = Identity()
)
