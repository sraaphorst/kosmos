package org.vorpal.kosmos.algebra.structures

/**
 * A Group can be considered:
 * A Monoid with inverses.
 * A Loop with inverses.
 * Since a Group is a Loop, which is a Quasigroup, we satisfy the Quasigroup operations here.
 */
interface Group<A> : Monoid<A>, Loop<A> {
    val inv: (A) -> A
    override fun ldiv(a: A, b: A): A = inv(a).let { op.combine(it, b) }
    override fun rdiv(b: A, a: A): A = inv(a).let { op.combine(b, it) }
}

/**
 * Since AbelianGroups are special in the sense that they play so many roles in other algebraic structures,
 * they are included as an extension of Group even though they add no inherent properties apart from being tagged
 * as being necessarily commutative.
 */
interface AbelianGroup<A> : Group<A>
