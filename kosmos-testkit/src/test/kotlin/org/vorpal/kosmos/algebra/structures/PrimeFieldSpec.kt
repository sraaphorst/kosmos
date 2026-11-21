package org.vorpal.kosmos.algebra.structures

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.instances.FiniteFieldAlgebras
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import org.vorpal.kosmos.testing.laws.throwsA
import java.math.BigInteger

/**
 * The prime fields `𝔽_p`, and their adherence to the [HasReciprocal] contract.
 */
class PrimeFieldSpec : FunSpec({

    lawTests(lawGraph("prime fields") {
        listOf(2, 3, 7, 101).forEach { p ->
            val field = PrimeField(p.toBigInteger())
            val arb: Arb<BigInteger> = Arb.int(0 until p).map(Int::toBigInteger)

            branch("𝔽_$p") {
                suite(FieldLaws(field, arb))

                local("every nonzero residue has a reciprocal, and a·a⁻¹ = 1 (exhaustive)") {
                    (1 until p).map(Int::toBigInteger).forEach { a ->
                        lawCheck(field.hasReciprocal(a)) { "hasReciprocal($a) is false" }
                        lawCheck(field.mul(a, field.reciprocal(a)) == field.one) { "$a · $a⁻¹ ≠ 1" }
                        lawCheck(field.reciprocalOption(a) == Option.Some(field.reciprocal(a))) { "reciprocalOption($a)" }
                    }
                }

                local("0 and every other multiple of $p are rejected with ArithmeticException") {
                    listOf(0, p, -p, 5 * p).map(Int::toBigInteger).forEach { a ->
                        lawCheck(!field.hasReciprocal(a)) { "hasReciprocal($a) is true" }
                        lawCheck(throwsA<ArithmeticException> { field.reciprocal(a) }) { "reciprocal($a) did not throw" }
                        lawCheck(field.reciprocalOption(a) == Option.None) { "reciprocalOption($a) is not None" }
                    }
                }

                local("the reciprocal is taken on the canonical representative") {
                    lawCheckAll(Arb.int(-1000..1000)) { n ->
                        val a = n.toBigInteger()
                        if (field.hasReciprocal(a)) {
                            lawCheck(field.mul(a, field.reciprocal(a)) == field.one) { "$a · $a⁻¹ ≠ 1" }
                        }
                    }
                }

                local("characteristic $p, and fromBigInt reduces mod $p") {
                    lawCheck(field.characteristic == p.toBigInteger()) { "characteristic = ${field.characteristic}" }
                    lawCheck(field.fromBigInt(p.toBigInteger()) == BigInteger.ZERO) { "fromBigInt($p) ≠ 0" }
                    lawCheck(field.fromBigInt((p + 1).toBigInteger()) == field.one) { "fromBigInt(${p + 1}) ≠ 1" }
                }
            }
        }

        local("a composite modulus is rejected") {
            lawCheck(throwsA<IllegalArgumentException> { PrimeField(BigInteger.valueOf(15)) }) { "PrimeField(15) did not throw" }
        }

        local("FiniteFieldAlgebras.F2 is 𝔽₂, where 1 + 1 = 0 is not invertible") {
            val f2 = FiniteFieldAlgebras.F2
            val two = f2.add(f2.one, f2.one)
            lawCheck(two == f2.zero) { "1 + 1 = $two" }
            lawCheck(!f2.hasReciprocal(two)) { "1 + 1 has a reciprocal" }
        }
    })
})
