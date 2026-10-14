package org.vorpal.kosmos.algebra.structures

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.FiniteFieldAlgebras
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.laws.algebra.FieldLaws
import java.math.BigInteger

/**
 * The prime fields `𝔽_p`, and their adherence to the [HasReciprocal] contract.
 */
class PrimeFieldSpec : StringSpec({

    listOf(2, 3, 7, 101).forEach { p ->
        val field = PrimeField(p.toBigInteger())
        val arb: Arb<BigInteger> = Arb.int(0 until p).map(Int::toBigInteger)

        "𝔽_$p satisfies FieldLaws" {
            FieldLaws(field, arb).fullTest().throwIfFailed()
        }

        "𝔽_$p: every nonzero residue has a reciprocal, and a·a⁻¹ = 1" {
            (1 until p).map(Int::toBigInteger).forEach { a ->
                field.hasReciprocal(a) shouldBe true
                field.mul(a, field.reciprocal(a)) shouldBe field.one
                field.reciprocalOption(a) shouldBe Option.Some(field.reciprocal(a))
            }
        }

        "𝔽_$p: 0 and every other multiple of $p are rejected with ArithmeticException" {
            listOf(0, p, -p, 5 * p).map(Int::toBigInteger).forEach { a ->
                field.hasReciprocal(a) shouldBe false
                shouldThrow<ArithmeticException> { field.reciprocal(a) }
                field.reciprocalOption(a) shouldBe Option.None
            }
        }

        "𝔽_$p: the reciprocal is taken on the canonical representative" {
            checkAll(Arb.int(-1000..1000)) { n ->
                val a = n.toBigInteger()
                if (field.hasReciprocal(a)) {
                    field.mul(a, field.reciprocal(a)) shouldBe field.one
                }
            }
        }

        "𝔽_$p has characteristic $p, and fromBigInt reduces mod $p" {
            field.characteristic shouldBe p.toBigInteger()
            field.fromBigInt(p.toBigInteger()) shouldBe BigInteger.ZERO
            field.fromBigInt((p + 1).toBigInteger()) shouldBe field.one
        }
    }

    "a composite modulus is rejected" {
        shouldThrow<IllegalArgumentException> { PrimeField(BigInteger.valueOf(15)) }
    }

    "FiniteFieldAlgebras.F2 is 𝔽₂, where 1 + 1 = 0 is not invertible" {
        val f2 = FiniteFieldAlgebras.F2
        val two = f2.add(f2.one, f2.one)
        two shouldBe f2.zero
        f2.hasReciprocal(two) shouldBe false
    }
})
