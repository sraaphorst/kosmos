package org.vorpal.kosmos.algebra.demo

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import org.vorpal.kosmos.algebra.laws.*
import org.vorpal.kosmos.algebra.ops.Add
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.list
import org.vorpal.kosmos.std.arbRational
import org.vorpal.kosmos.std.nonzeroRational

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

/* --------------- Spec registering laws --------------- */

class LawDemoSpec : StringSpec({
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
})