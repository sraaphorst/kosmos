package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.BoundedLattice
import org.vorpal.kosmos.algebra.structures.DistributiveLattice
import org.vorpal.kosmos.algebra.structures.JoinSemilattice
import org.vorpal.kosmos.algebra.structures.Lattice
import org.vorpal.kosmos.algebra.structures.MeetSemilattice
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.AbsorbOverLaw
import org.vorpal.kosmos.laws.property.AnnihilationLaw
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.property.DistributivityLaw
import org.vorpal.kosmos.laws.property.IdempotencyLaw
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.property.OverAbsorbLaw
import org.vorpal.kosmos.laws.suiteName

class JoinSemilatticeLaws<A : Any>(
    semilattice: JoinSemilattice<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName("JoinSemilattice", semilattice.join.symbol)

    private val laws: List<TestingLaw> = listOf(
        AssociativityLaw(semilattice.join, arb, eq, pr),
        CommutativityLaw(semilattice.join, arb, eq, pr),
        IdempotencyLaw(semilattice.join, arb, eq, pr)
    )

    override fun laws(): List<TestingLaw> = laws
    override fun fullLaws(): List<TestingLaw> = laws
}

class MeetSemilatticeLaws<A : Any>(
    semilattice: MeetSemilattice<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName("MeetSemilattice", semilattice.meet.symbol)

    private val laws: List<TestingLaw> = listOf(
        AssociativityLaw(semilattice.meet, arb, eq, pr),
        CommutativityLaw(semilattice.meet, arb, eq, pr),
        IdempotencyLaw(semilattice.meet, arb, eq, pr)
    )

    override fun laws(): List<TestingLaw> = laws
    override fun fullLaws(): List<TestingLaw> = laws
}

class LatticeLaws<A : Any>(
    lattice: Lattice<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName(
        "Lattice",
        lattice.join.symbol,
        lattice.meet.symbol
    )

    private val joinLaws = JoinSemilatticeLaws(lattice, arb, eq, pr)
    private val meetLaws = MeetSemilatticeLaws(lattice, arb, eq, pr)

    private val structureLaws: List<TestingLaw> = listOf(
        AbsorbOverLaw(absorb = lattice.meet, over = lattice.join, arb = arb, eq = eq, pr = pr),
        OverAbsorbLaw(absorb = lattice.meet, over = lattice.join, arb = arb, eq = eq, pr = pr)
    )

    override fun laws(): List<TestingLaw> =
        joinLaws.laws() +
            meetLaws.laws() +
            structureLaws

    override fun fullLaws(): List<TestingLaw> =
        joinLaws.fullLaws() +
            meetLaws.fullLaws() +
            structureLaws
}

class BoundedLatticeLaws<A : Any>(
    lattice: BoundedLattice<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName(
        "BoundedLattice",
        lattice.join.symbol,
        lattice.meet.symbol,
        "⊥",
        "⊤"
    )

    private val latticeLaws = LatticeLaws(lattice, arb, eq, pr)

    private val boundsLaws: List<TestingLaw> = listOf(
        // join identity is bottom: x ∨ ⊥ = x
        IdentityLaw(lattice.join, lattice.bottom, arb, eq, pr),
        // meet identity is top: x ∧ ⊤ = x
        IdentityLaw(lattice.meet, lattice.top, arb, eq, pr),

        // meet annihilator is bottom: x ∧ ⊥ = ⊥
        AnnihilationLaw(lattice.meet, lattice.bottom, arb, eq, pr),
        // join annihilator is top: x ∨ ⊤ = ⊤
        AnnihilationLaw(lattice.join, lattice.top, arb, eq, pr)
    )

    override fun laws(): List<TestingLaw> =
        latticeLaws.laws() + boundsLaws

    override fun fullLaws(): List<TestingLaw> =
        latticeLaws.fullLaws() + boundsLaws
}

class DistributiveLatticeLaws<A : Any>(
    lattice: DistributiveLattice<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName(
        "DistributiveLattice",
        lattice.join.symbol,
        lattice.meet.symbol
    )

    private val bounded = BoundedLatticeLaws(lattice, arb, eq, pr)

    private val distributivity: List<TestingLaw> = listOf(
        // x ∧ (y ∨ z) = (x ∧ y) ∨ (x ∧ z)
        DistributivityLaw(
            mul = lattice.meet,
            add = lattice.join,
            arb = arb,
            eq = eq,
            pr = pr
        ),
        // x ∨ (y ∧ z) = (x ∨ y) ∧ (x ∨ z)
        DistributivityLaw(
            mul = lattice.join,
            add = lattice.meet,
            arb = arb,
            eq = eq,
            pr = pr
        )
    )

    override fun laws(): List<TestingLaw> =
        bounded.laws() + distributivity

    override fun fullLaws(): List<TestingLaw> =
        bounded.fullLaws() + distributivity
}