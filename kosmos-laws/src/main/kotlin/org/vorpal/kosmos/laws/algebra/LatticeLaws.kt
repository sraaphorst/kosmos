package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb

import org.vorpal.kosmos.algebra.structures.JoinSemilattice
import org.vorpal.kosmos.algebra.structures.MeetSemilattice
import org.vorpal.kosmos.algebra.structures.Lattice
import org.vorpal.kosmos.algebra.structures.BoundedLattice
import org.vorpal.kosmos.algebra.structures.DistributiveLattice

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default

import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.property.IdempotencyLaw
import org.vorpal.kosmos.laws.property.AbsorptionLaw
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.property.AnnihilationLaw
import org.vorpal.kosmos.laws.property.DistributivityLaw

/**
 * Laws for a join-semilattice (A, ∨):
 *  - Associativity: (x ∨ y) ∨ z = x ∨ (y ∨ z)
 *  - Commutativity: x ∨ y = y ∨ x
 *  - Idempotency:   x ∨ x = x
 */
class JoinSemilatticeLaws<A : Any>(
    private val joinSemilattice: JoinSemilattice<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val symbol: String = "∨"
) {

    fun laws(): List<TestingLaw> =
        listOf(
            AssociativityLaw(
                op = joinSemilattice.join,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            ),
            CommutativityLaw(
                op = joinSemilattice.join,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            ),
            IdempotencyLaw(
                op = joinSemilattice.join,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            )
        )
}

/**
 * Laws for a meet-semilattice (A, ∧):
 *  - Associativity: (x ∧ y) ∧ z = x ∧ (y ∧ z)
 *  - Commutativity: x ∧ y = y ∧ x
 *  - Idempotency:   x ∧ x = x
 */
class MeetSemilatticeLaws<A : Any>(
    private val meetSemilattice: MeetSemilattice<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val symbol: String = "∧"
) {

    fun laws(): List<TestingLaw> =
        listOf(
            AssociativityLaw(
                op = meetSemilattice.meet,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            ),
            CommutativityLaw(
                op = meetSemilattice.meet,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            ),
            IdempotencyLaw(
                op = meetSemilattice.meet,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            )
        )
}

/**
 * Lattice (A, ∧, ∨) = meet-semilattice + join-semilattice + absorption.
 *
 * Absorption:
 *  - x ∧ (x ∨ y) = x
 *  - x ∨ (x ∧ y) = x
 */
class LatticeLaws<A : Any>(
    private val lattice: Lattice<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val meetSymbol: String = "∧",
    private val joinSymbol: String = "∨"
) {

    fun laws(): List<TestingLaw> =
        JoinSemilatticeLaws(
            joinSemilattice = lattice,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = joinSymbol
        ).laws() +
                MeetSemilatticeLaws(
                    meetSemilattice = lattice,
                    arb = arb,
                    eq = eq,
                    pr = pr,
                    symbol = meetSymbol
                ).laws() +
                listOf(
                    AbsorptionLaw(
                        absorb = lattice.meet,
                        over = lattice.join,
                        arb = arb,
                        eq = eq,
                        pr = pr,
                        absorbSymbol = meetSymbol,
                        overSymbol = joinSymbol
                    )
                )
}


/**
 * Bounded lattice (A, ∧, ∨, 0, 1):
 *
 *  - 0 is identity for ∨ and annihilator for ∧:
 *      x ∨ 0 = x,    x ∧ 0 = 0
 *  - 1 is identity for ∧ and annihilator for ∨:
 *      x ∧ 1 = x,    x ∨ 1 = 1
 */
class BoundedLatticeLaws<A : Any>(
    private val lattice: BoundedLattice<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val meetSymbol: String = "∧",
    private val joinSymbol: String = "∨"
) {

    fun laws(): List<TestingLaw> =
        LatticeLaws(
            lattice = lattice,
            arb = arb,
            eq = eq,
            pr = pr,
            meetSymbol = meetSymbol,
            joinSymbol = joinSymbol
        ).laws() + listOf(
            // Identities
            IdentityLaw(
                op = lattice.join,
                identity = lattice.bottom,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = joinSymbol
            ),
            IdentityLaw(
                op = lattice.meet,
                identity = lattice.top,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = meetSymbol
            ),
            // Annihilators
            AnnihilationLaw(
                op = lattice.meet,
                zero = lattice.bottom,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = meetSymbol
            ),
            AnnihilationLaw(
                op = lattice.join,
                zero = lattice.top,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = joinSymbol
            )
        )
}

/**
 * Distributive lattice:
 *
 *  - x ∧ (y ∨ z) = (x ∧ y) ∨ (x ∧ z)
 *  - x ∨ (y ∧ z) = (x ∨ y) ∧ (x ∨ z)
 */
class DistributiveLatticeLaws<A : Any>(
    private val lattice: DistributiveLattice<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val meetSymbol: String = "∧",
    private val joinSymbol: String = "∨"
) {

    fun laws(): List<TestingLaw> =
        BoundedLatticeLaws(
            lattice = lattice,
            arb = arb,
            eq = eq,
            pr = pr,
            meetSymbol = meetSymbol,
            joinSymbol = joinSymbol
        ).laws() + listOf(
            DistributivityLaw(
                mul = lattice.meet,
                add = lattice.join,
                arb = arb,
                eq = eq,
                pr = pr,
                mulSymbol = meetSymbol,
                addSymbol = joinSymbol
            ),
            DistributivityLaw(
                mul = lattice.join,
                add = lattice.meet,
                arb = arb,
                eq = eq,
                pr = pr,
                mulSymbol = joinSymbol,
                addSymbol = meetSymbol
            )
        )
}