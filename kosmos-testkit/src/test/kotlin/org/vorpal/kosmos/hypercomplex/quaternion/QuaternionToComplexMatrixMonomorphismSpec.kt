package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat

class QuaternionToComplexMatrixMonomorphismSpec : StringSpec({
    val mono = QuaternionAlgebras.QuaternionToComplexMatrixMonomorphism
    val eqQ = QuaternionAlgebras.eqQuaternion
    val prQ = QuaternionAlgebras.printableQuaternionPretty
    val eqC = ComplexAlgebras.eqComplex
    val eqM = DenseMatAlgebras.liftEq(eqC)
    val prC = ComplexAlgebras.printableComplexPretty
    val prM = DenseMatAlgebras.liftPrintablePretty(prC)
    val complex = ComplexAlgebras.ComplexStarAlgebra

    "QuaternionToComplexMatrixMonomorphism satisfies UnitalRingHomomorphismLaws" {
        val laws = RingHomomorphismLaws(
            hom = mono::invoke,
            domain = mono.domain,
            codomain = mono.codomain,
            arb = ArbQuaternion.quaternion,
            eqB = eqM,
            prA = prQ,
            prB = prM
        )

        laws.fullTest().throwIfFailed()
    }

    "QuaternionToComplexMatrixMonomorphism respects conjugation as conjugate-transpose" {
        fun conjTranspose2x2(m: DenseMat<Complex>): DenseMat<Complex> {
            require(m.rows == 2 && m.cols == 2) { "expected 2x2, got ${m.rows}${Symbols.TIMES}${m.cols}" }
            return DenseMat.ofRows(
                listOf(
                    listOf(complex.conj(m[0, 0]), complex.conj(m[1, 0])),
                    listOf(complex.conj(m[0, 1]), complex.conj(m[1, 1]))
                )
            )
        }

        checkAll(ArbQuaternion.quaternion) { q ->
            val left = mono(mono.domain.conj(q))
            val right = conjTranspose2x2(mono(q))

            withClue("Φ(conj(q)) should equal Φ(q)^*, q=${prQ(q)}, got left=${prM(left)}, right=${prM(right)}") {
                check(eqM(left, right))
            }
        }
    }

    "QuaternionToComplexMatrixMonomorphism trace equals 2*Re(q) and det equals normSq(q)" {
        fun require2x2(m: DenseMat<Complex>) {
            require(m.rows == 2 && m.cols == 2) {
                "expected 2x2, got ${m.rows}${Symbols.TIMES}${m.cols}"
            }
        }

        fun sub(x: Complex, y: Complex): Complex =
            complex.add(x, complex.add.inverse(y))

        fun trace2x2(m: DenseMat<Complex>): Complex {
            require2x2(m)
            return complex.add(m[0, 0], m[1, 1])
        }

        fun det2x2(m: DenseMat<Complex>): Complex {
            require2x2(m)
            val ad = complex.mul(m[0, 0], m[1, 1])
            val bc = complex.mul(m[0, 1], m[1, 0])
            return sub(ad, bc)
        }

        fun twoRe(z: Complex): Complex =
            complex.add(z, complex.conj(z))

        fun normSqAsComplex(q: Quaternion): Complex =
            complex(mono.domain.normSq(q), 0.0)

        checkAll(ArbQuaternion.quaternion) { q ->
            val m = mono(q)

            val tr = trace2x2(m)
            val expectedTr = twoRe(q.a)

            withClue(
                "trace(Φ(q)) should equal 2*Re(q), q=${prQ(q)}, got tr=${prC(tr)}, expected=${prC(expectedTr)}"
            ) {
                check(eqC(tr, expectedTr))
            }

            val det = det2x2(m)
            val expectedDet = normSqAsComplex(q)

            withClue(
                "det(Φ(q)) should equal normSq(q), q=${prQ(q)}, got det=${prC(det)}, expected=${prC(expectedDet)}"
            ) {
                check(eqC(det, expectedDet))
            }
        }
    }

    "QuaternionToComplexMatrixMonomorphism is injective on tested inputs" {
        injectivityLaw(
            hom = mono::invoke,
            arbA = ArbQuaternion.quaternion,
            eqA = eqQ,
            eqB = eqM,
            prA = prQ,
            prB = prM
        ).test()
    }
})
