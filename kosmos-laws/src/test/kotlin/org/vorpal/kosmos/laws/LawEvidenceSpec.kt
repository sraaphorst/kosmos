package org.vorpal.kosmos.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.algebra.MonoidLaws

class LawEvidenceSpec : FunSpec({
    val ints = Arb.int(-9..9)
    val eq = Eq<Int> { a, b -> a == b }
    val pr = Printable<Int> { "$it" }

    test("a passing suite report is evidence with its scope, view, seed and settings") {
        val config = LawRunConfig(seed = 17)
        val e = MonoidLaws(Monoid.of(0, BinOp("+") { a, b -> a + b }), ints, eq, pr).verify(LawMode.FULL, config).evidence()
        e.scope shouldBe "Monoid (+)"
        e.mode shouldBe LawMode.FULL
        e.seed shouldBe 17
        e.config shouldBe config
        e.laws shouldBe listOf(
            "Monoid (+) > Semigroup (+) > Magma (+) > totality (+)",
            "Monoid (+) > Semigroup (+) > associativity (+)",
            "Monoid (+) > identity (+)"
        )
        e.executions shouldBe 3
    }

    test("a failing or empty report is not evidence") {
        shouldThrow<IllegalStateException> {
            MonoidLaws(Monoid.of(0, BinOp("-") { a, b -> a - b }), ints, eq, pr).verify().evidence()
        }
        shouldThrow<IllegalStateException> { LawNode.branch("Empty").verify().evidence() }
    }
})
