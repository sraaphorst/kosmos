package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.relations.Poset

/**
 * Boolean algebra = distributive bounded lattice + involutive complement (negation).
 *
 * Structure:
 *  - join (∨) : BinOp<A>
 *  - meet (∧) : BinOp<A>
 *  - bottom ⊥ and top ⊤
 *  - not (¬) : UnaryOp<A>, a complement satisfying
 *      a ∨ ¬a = ⊤  and  a ∧ ¬a = ⊥
 *
 * Laws (checked in kosmos-laws / lawkit):
 *  - (A, ∨) is a join-semilattice (assoc/comm/idemp)
 *  - (A, ∧) is a meet-semilattice  (assoc/comm/idemp)
 *  - Absorption:  x ∧ (x ∨ y) = x and x ∨ (x ∧ y) = x
 *  - Bounded:     ⊥ ≤ x ≤ ⊤
 *  - Distributive: ∧ distributes over ∨ and vice-versa
 *  - Complement:  x ∨ ¬x = ⊤, x ∧ ¬x = ⊥, and De Morgan dualities
 *
 * The canonical order on a Boolean algebra is the join-induced order
 * (equivalently the meet-induced order under the laws).
 */
interface BooleanAlgebra<A: Any> : DistributiveLattice<A> {
    val not: Endo<A> // ¬
    override val join: BinOp<A>
    override val meet: BinOp<A>
    override val bottom: A
    override val top: A

    override val poset: Poset<A>
        get() = super.poset

    // ---- Useful derived operations ----
    /** a → b  ::=  ¬a ∨ b */
    fun implies(a: A, b: A): A =
        join(not(a), b)

    /** (a XOR b) ::= (a ∨ b) ∧ ¬(a ∧ b) */
    fun xor(a: A, b: A): A =
        meet(join(a, b), not(meet(a, b)))

    /** (a ↔ b)  ::= (a ∧ b) ∨ (¬a ∧ ¬b) */
    fun iff(a: A, b: A): A =
        join(meet(a, b), meet(not(a), not(b)))

    /** a \ b  ::=  a ∧ ¬b */
    fun minus(a: A, b: A): A =
        meet(a, not(b))

    /** NAND(a,b) ::= ¬(a ∧ b) */
    fun nand(a: A, b: A): A =
        not(meet(a, b))

    /** NOR(a,b) ::= ¬(a ∨ b) */
    fun nor(a: A, b: A): A =
        not(join(a, b))
}

/* ---------- Lightweight builder ---------- */

object BooleanAlgebras {
    /**
     * Build a Boolean algebra from its operations. Caller promises the laws.
     */
    fun <A : Any> of(
        join: BinOp<A>,
        meet: BinOp<A>,
        bottom: A,
        top: A,
        not: Endo<A>
    ): BooleanAlgebra<A> =
        object : BooleanAlgebra<A> {
            override val join: BinOp<A> = join
            override val meet: BinOp<A> = meet
            override val bottom: A = bottom
            override val top: A = top
            override val not: Endo<A> = not
        }
}

/* ---------- Primitive Boolean instance (handy for specs) ---------- */

object BooleanInstances {
    /**
     * Boolean algebra on Kotlin Boolean:
     *  - ⊥ = false, ⊤ = true
     *  - ∨ = OR, ∧ = AND, ¬ = NOT
     */
    val booleanAlgebra: BooleanAlgebra<Boolean> =
        BooleanAlgebras.of(
            join = BinOp(symbol = Symbols.BOOL_OR) { a, b -> a || b },
            meet = BinOp(symbol = Symbols.BOOL_AND) { a, b -> a && b },
            bottom = false,
            top = true,
            not = Endo(symbol = Symbols.NOT) { a -> !a }
        )
}