package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

/**
 * Boolean algebra = distributive bounded lattice + complement (negation).
 *
 * Structure:
 *  - join (∨) : BinOp<A>
 *  - meet (∧) : BinOp<A>
 *  - bottom ⊥ and top ⊤
 *  - not (¬) : Endo<A>, a complement satisfying
 *      a ∨ ¬a = ⊤  and  a ∧ ¬a = ⊥
 *
 * Laws (checked in kosmos-laws / lawkit):
 *  - (A, ∨) is a join-semilattice (assoc/comm/idemp)
 *  - (A, ∧) is a meet-semilattice  (assoc/comm/idemp)
 *  - Absorption:  x ∧ (x ∨ y) = x and x ∨ (x ∧ y) = x
 *  - Bounded:     ⊥ ≤ x ≤ ⊤
 *  - Distributive: ∧ distributes over ∨ and vice-versa
 *  - Complement:  x ∨ ¬x = ⊤, x ∧ ¬x = ⊥, and De Morgan dualities
 */
interface BooleanAlgebra<A : Any> : DistributiveLattice<A> {
    val not: Endo<A>
    override val join: BinOp<A>
    override val meet: BinOp<A>
    override val bottom: A
    override val top: A

    fun implies(a: A, b: A): A =
        join(not(a), b)

    fun xor(a: A, b: A): A =
        meet(join(a, b), not(meet(a, b)))

    fun iff(a: A, b: A): A =
        join(meet(a, b), meet(not(a), not(b)))

    fun minus(a: A, b: A): A =
        meet(a, not(b))

    fun nand(a: A, b: A): A =
        not(meet(a, b))

    fun nor(a: A, b: A): A =
        not(join(a, b))

    companion object {
        fun <A : Any> of(
            join: BinOp<A>,
            meet: BinOp<A>,
            bottom: A,
            top: A,
            not: Endo<A>
        ): BooleanAlgebra<A> = object : BooleanAlgebra<A> {
            override val join = join
            override val meet = meet
            override val bottom = bottom
            override val top = top
            override val not = not
        }
    }
}