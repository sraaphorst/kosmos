package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.ArbQuaternion
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.laws.homomorphism.UnitalRingHomomorphismLaws
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat

class QuaternionToComplexMatrixMonomorphismSpec : StringSpec({

    "QuaternionToComplexMatrixMonomorphism satisfies UnitalRingHomomorphismLaws" {
        val mono = QuaternionAlgebras.QuaternionToComplexMatrixMonomorphism
        val prA: Printable<Quaternion> = Printable.default()
        val prC: Printable<Complex> = Printable.default()
        val laws = UnitalRingHomomorphismLaws(
            hom = { q -> mono.map(q) },
            domain = mono.domain,
            codomain = mono.codomain,
            arb = ArbQuaternion.quaternion,
            eqB = DenseMatAlgebras.liftEq(ComplexAlgebras.eqComplex),
            prA = prA,
            prB = DenseMatAlgebras.liftPrintablePretty(prC)
        )

        laws.fullTest().throwIfFailed()
    }

    "QuaternionToComplexMatrixMonomorphism respects conjugation as conjugate-transpose" {
        val mono = QuaternionAlgebras.QuaternionToComplexMatrixMonomorphism
        val complexField = ComplexAlgebras.ComplexField

        fun conjTranspose2x2(m: DenseMat<Complex>): DenseMat<Complex> {
            require(m.rows == 2 && m.cols == 2) { "expected 2x2, got ${m.rows}${Symbols.TIMES}${m.cols}" }
            return DenseMat.ofRows(
                listOf(
                    listOf(complexField.conj(m[0, 0]), complexField.conj(m[1, 0])),
                    listOf(complexField.conj(m[0, 1]), complexField.conj(m[1, 1]))
                )
            )
        }

        fun conjQuaternion(q: Quaternion): Quaternion =
            mono.domain.conj(q)

        val eqMat = Eq.default<DenseMat<Complex>>()
        val prQ = Printable.default<Quaternion>()
        val prM = Printable.default<DenseMat<Complex>>()

        checkAll(ArbQuaternion.quaternion) { q ->
            val left = mono.map(conjQuaternion(q))
            val right = conjTranspose2x2(mono.map(q))

            withClue("Φ(conj(q)) should equal Φ(q)^*, q=${prQ(q)}, got left=${prM(left)}, right=${prM(right)}") {
                check(eqMat(left, right))
            }
        }
    }

    "QuaternionToComplexMatrixMonomorphism trace equals 2*Re(q) and det equals normSq(q)" {
        val mono = QuaternionAlgebras.QuaternionToComplexMatrixMonomorphism
        val complexField = ComplexAlgebras.ComplexField
        val eqC = Eq.default<Complex>()
        val prQ = Printable.default<Quaternion>()
        val prC = Printable.default<Complex>()

        fun require2x2(m: DenseMat<Complex>) {
            require(m.rows == 2 && m.cols == 2) {
                "expected 2x2, got ${m.rows}${Symbols.TIMES}${m.cols}"
            }
        }

        fun sub(x: Complex, y: Complex): Complex =
            complexField.add(x, complexField.add.inverse(y))

        fun trace2x2(m: DenseMat<Complex>): Complex {
            require2x2(m)
            return complexField.add(m[0, 0], m[1, 1])
        }

        fun det2x2(m: DenseMat<Complex>): Complex {
            require2x2(m)
            val ad = complexField.mul(m[0, 0], m[1, 1])
            val bc = complexField.mul(m[0, 1], m[1, 0])
            return sub(ad, bc)
        }

        fun twoRe(z: Complex): Complex =
            complexField.add(z, complexField.conj(z))

        fun normSqAsComplex(q: Quaternion): Complex =
            complex(mono.domain.normSq(q), 0.0)

        checkAll(ArbQuaternion.quaternion) { q ->
            val m = mono.map(q)

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
})