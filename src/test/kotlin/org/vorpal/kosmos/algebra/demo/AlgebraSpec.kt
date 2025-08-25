package org.vorpal.kosmos.algebra.demo

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import org.vorpal.kosmos.algebra.laws.*
import org.vorpal.kosmos.algebra.ops.Add
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.list
import org.vorpal.kosmos.core.set
import org.vorpal.kosmos.registerAbelianGroup
import org.vorpal.kosmos.registerCIQ
import org.vorpal.kosmos.registerField
import org.vorpal.kosmos.registerGroup
import org.vorpal.kosmos.registerModule
import org.vorpal.kosmos.registerMonoid
import org.vorpal.kosmos.registerRing
import org.vorpal.kosmos.registerSemigroup
import org.vorpal.kosmos.registerVectorSpace
import org.vorpal.kosmos.std.Int2
import org.vorpal.kosmos.std.Q2
import org.vorpal.kosmos.std.arbRational
import org.vorpal.kosmos.std.modules.ZModule_Int2
import org.vorpal.kosmos.std.nonzeroRational
import org.vorpal.kosmos.std.vectorspaces.Q2OverQ

/* --- Example instances (replace with your real ones) --- */

// Semigroup: string concatenation (no identity)
object StringConcatSemigroup : Semigroup<String> {
    override fun combine(a: String, b: String) = a + b
}

// Monoid: list concatenation
object IntListConcatMonoid : Monoid<List<Int>, Add> {
    override val identity: List<Int> = emptyList()
    override fun combine(a: List<Int>, b: List<Int>) = a + b
}

// Monoid: set union
object IntSetUnionMonoid: Monoid<Set<Int>, Add> {
    override val identity: Set<Int> = emptySet()
    override fun combine(a: Set<Int>, b: Set<Int>) = a + b
}

/* --------------- Spec registering laws --------------- */

class AlgebraSpec : StringSpec({
    registerSemigroup(
        name = "String (+) as semigroup",
        laws = SemigroupLaws(
            S = StringConcatSemigroup,
            arb = Arb.string(),
            EQ  = Eqs.string
        )
    )

    registerMonoid(
        name = "List<Int> concat monoid",
        laws = MonoidLaws(
            S = IntListConcatMonoid,
            arb = Arb.list(Arb.int(), 0..10),
            EQ  = Eqs.int.list()
        )
    )

    registerMonoid(
        name = "Set<Int> union monoid",
        laws = MonoidLaws(
            S = IntSetUnionMonoid,
            arb = Arb.set(Arb.int(), 0..10),
            EQ  = Eqs.int.set()
        )
    )

    registerGroup(
        name = "Int (+) group",
        laws = GroupLaws(
            S = AbelianGroups.IntAdd,
            arb = Arb.int(),
            EQ  = Eqs.int
        )
    )

    registerAbelianGroup(
        name = "Int (+) abelian group",
        laws = AbelianGroupLaws(
            S = AbelianGroups.IntAdd,
            arb = Arb.int(),
            EQ  = Eqs.int
        )
    )

    registerAbelianGroup(
        name = "Rational (+) abelian group",
        laws = AbelianGroupLaws(
            S = AbelianGroups.RationalAdd,
            arb = arbRational,
            EQ  = Eqs.rational
        )
    )

    registerAbelianGroup(
        name = "Rational (*) abelian group",
        laws = AbelianGroupLaws(
            S = AbelianGroups.RationalMul,
            arb = nonzeroRational,
            EQ  = Eqs.rational
        )
    )

    registerRing(
        name = "Int ring",
        laws = RingLaws(
            R   = Rings.IntRing,
            arb = Arb.int(),
            EQ  = Eqs.int
        )
    )

    registerField(
        name = "Rational field",
        laws = FieldLaws(
            F = Fields.RationalField,
            arb = arbRational,
            EQ  = Eqs.rational
        )
    )

    registerCIQ(
        name = "Fano plane CIQ",
        laws = CommutativeIdempotentQuasigroupLaws(
            S = Quasigroups.Fano,
            points = Quasigroups.FanoPoints,
            EQ = Eqs.int
        ),
    )

    registerModule(
        name = "Z-module on Int2",
        laws = ModuleLaws(
            M    = ZModule_Int2,
            arbS = Arb.int(),
            arbV = Arb.bind(Arb.int(), Arb.int()) { x, y -> Int2(x, y) },
            EQv  = { a, b -> Eqs.int.eqv(a.x, b.x) && Eqs.int.eqv(a.y, b.y) }
        )
    )

    registerVectorSpace(
        name = "Q2 over Q",
        laws = VectorSpaceLaws(
            VS   = Q2OverQ,
            arbS = arbRational,
            arbV = Arb.bind(arbRational, arbRational) { x, y -> Q2(x, y) },
            eqV  = { a, b -> Eqs.rational.eqv(a.x, b.x) && Eqs.rational.eqv(a.y, b.y) }
        )
    )
})