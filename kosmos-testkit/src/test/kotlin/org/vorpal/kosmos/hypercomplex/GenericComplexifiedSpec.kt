package org.vorpal.kosmos.hypercomplex

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.bioctonion.ArbBioctonion
import org.vorpal.kosmos.hypercomplex.bioctonion.Bioctonion
import org.vorpal.kosmos.hypercomplex.bioctonion.BioctonionAlgebras
import org.vorpal.kosmos.hypercomplex.bioctonion.bioctonion
import org.vorpal.kosmos.hypercomplex.biquaternion.ArbBiquaternion
import org.vorpal.kosmos.hypercomplex.biquaternion.Biquaternion
import org.vorpal.kosmos.hypercomplex.biquaternion.BiquaternionAlgebras
import org.vorpal.kosmos.hypercomplex.biquaternion.biquaternion
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.multi.ArbBicomplex
import org.vorpal.kosmos.hypercomplex.multi.Bicomplex
import org.vorpal.kosmos.hypercomplex.multi.BicomplexAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.ArbRationalOctonion
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.splitoctonion.ArbSplitOctonion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The generic `Bicomplex<R>`, `Biquaternion<R>` and `Bioctonion<R>` constructions.
 *
 * - Over ℚ every law is checked *exactly*: these are the facts that the floating-point specs for
 *   `𝔹`, `ℍ ⊗ ℂ` and `𝕆 ⊗ ℂ` can only check approximately.
 * - Over the finite field `𝔽₃` the ring laws and the reciprocal contract are checked.
 * - Over ℝ the generic constructions are checked to be the same operations as the named ones.
 */
class GenericComplexifiedSpec : FunSpec({
    lawTests(lawGraph("generic complexified algebras") {
        val q = RationalAlgebras.RationalField
        val complexQ = ComplexAlgebras.complexRing(q)

        val smallRational: Arb<Rational> =
            Arb.bind(Arb.int(-6..6), Arb.int(1..4)) { p, d -> Rational.of(p, d) }
        val arbComplex: Arb<Complex<Rational>> =
            Arb.bind(smallRational, smallRational) { a, b -> complex(a, b) }
        val arbBicomplex: Arb<Bicomplex<Rational>> =
            Arb.bind(arbComplex, arbComplex) { z1, z2 -> Bicomplex.ofStandard(z1, z2) }
        val arbBiquaternion: Arb<Biquaternion<Rational>> =
            Arb.bind(arbComplex, arbComplex, arbComplex, arbComplex) { w, x, y, z -> biquaternion(w, x, y, z) }
        val arbBioctonion: Arb<Bioctonion<Rational>> =
            Arb.list(arbComplex, 8..8).map { cs -> bioctonion(cs) }

        val zeroC = complexQ.zero
        fun real(r: Rational): Complex<Rational> = complex(r, Rational.ZERO)

        // ── Bicomplex<ℚ> ─────────────────────────────────────────────────────────

        val bicomplex = BicomplexAlgebras.bicomplexRing(q)

        branch("Bicomplex<ℚ> is exactly a commutative involutive ring") {
            suite(CommutativeRingLaws(bicomplex, arbBicomplex))
            suite(InvolutiveRingLaws(bicomplex, arbBicomplex))
        }

        local("Bicomplex<ℚ>: i² = j² = -1, k = ij, k² = +1, and (1 + k)(1 - k) = 0") {
            val (one, i, j, k) = BicomplexAlgebras.basis(q)
            one shouldBe bicomplex.one
            bicomplex.mul(i, i) shouldBe bicomplex.negOne
            bicomplex.mul(j, j) shouldBe bicomplex.negOne
            bicomplex.mul(i, j) shouldBe k
            bicomplex.mul(k, k) shouldBe one
            bicomplex.mul(bicomplex.add(one, k), bicomplex.add(one, bicomplex.add.inverse(k))) shouldBe bicomplex.zero
        }

        local("Bicomplex<ℚ>: the three conjugations are involutive automorphisms, and conj3 = conj1 ∘ conj2") {
            val conjugations = listOf(BicomplexAlgebras.conj1(q), BicomplexAlgebras.conj2(q), BicomplexAlgebras.conj3(q))
            lawCheckAll(arbBicomplex, arbBicomplex) { x, y ->
                conjugations.forEach { conj ->
                    conj(conj(x)) shouldBe x
                    conj(bicomplex.add(x, y)) shouldBe bicomplex.add(conj(x), conj(y))
                    conj(bicomplex.mul(x, y)) shouldBe bicomplex.mul(conj(x), conj(y))
                }
                conjugations[2](x) shouldBe conjugations[0](conjugations[1](x))
                conjugations[1](x) shouldBe bicomplex.conj(x)
            }
        }

        branch("Bicomplex<ℚ>: N2(z) = z1² + z2² is z·conj2(z), and is multiplicative") {
            val norm = BicomplexAlgebras.normByConj2(q)
            val embed = BicomplexAlgebras.complexToBicomplexMonomorphism(q)
            law(NormSqMultiplicativeLaw(bicomplex.mul.op, norm, complexQ.mul.op, arbBicomplex))
            local("N₂(z) is z·conj(z)") {
                lawCheckAll(arbBicomplex) { z ->
                    bicomplex.mul(z, bicomplex.conj(z)) shouldBe embed(norm(z))
                }
            }
        }

        branch("Bicomplex<ℚ>: the idempotent projections are ring homomorphisms and are jointly injective") {
            val alpha = BicomplexAlgebras.firstProjection(q)
            val beta = BicomplexAlgebras.secondProjection(q)
            law(injectivityLaw(hom = { z: Bicomplex<Rational> -> alpha(z) to beta(z) }, arbA = arbBicomplex))
            listOf("𝛂" to alpha, "𝜷" to beta).forEach { (name, projection) ->
                branch(name) {
                    suite(RingHomomorphismLaws(
                        hom = projection::invoke,
                        domain = bicomplex,
                        codomain = complexQ,
                        arb = arbBicomplex
                    ))
                }
            }
            local("the norm is the product of the idempotent coordinates") {
                lawCheckAll(arbBicomplex) { z ->
                    BicomplexAlgebras.normByConj2(q)(z) shouldBe complexQ.mul(alpha(z), beta(z))
                }
            }
        }

        branch("Bicomplex<ℚ>: module and star-algebra laws, and Complex<ℚ> ↪ Bicomplex<ℚ>") {
            suite(RModuleLaws(BicomplexAlgebras.bicomplexModule(q), smallRational, arbBicomplex))
            suite(StarAlgebraLaws(BicomplexAlgebras.bicomplexStarAlgebra(q), smallRational, arbBicomplex))
            val embed = BicomplexAlgebras.complexToBicomplexMonomorphism(q)
            suite(RingHomomorphismLaws(embed::invoke, complexQ, bicomplex, arbComplex))
            law(injectivityLaw(hom = embed::invoke, arbA = arbComplex))
            val action = BicomplexAlgebras.complexLeftAction(q)
            local("the ℂ-action is multiplication by the embedded scalar") {
                lawCheckAll(arbComplex, arbBicomplex) { c, z ->
                    action(c, z) shouldBe bicomplex.mul(embed(c), z)
                }
            }
        }

        // ── Biquaternion<ℚ> ──────────────────────────────────────────────────────

        val biquaternions = BiquaternionAlgebras.biquaternionRingWithReciprocal(q)

        branch("Biquaternion<ℚ> is exactly an associative involutive ring") {
            suite(InvolutiveRingLaws(BiquaternionAlgebras.biquaternionRing(q), arbBiquaternion))
        }

        local("Biquaternion<ℚ>: i² = j² = k² = ijk = -1, and h is central with h² = -1") {
            val (one, i, j, k) = BiquaternionAlgebras.basis(q)
            val h = BiquaternionAlgebras.centralUnit(q)
            one shouldBe biquaternions.one
            listOf(i, j, k, h).forEach { u -> biquaternions.mul(u, u) shouldBe biquaternions.negOne }
            biquaternions.mul(biquaternions.mul(i, j), k) shouldBe biquaternions.negOne
            biquaternions.mul(i, j) shouldBe biquaternions.add.inverse(biquaternions.mul(j, i))
            lawCheckAll(arbBiquaternion) { x ->
                biquaternions.mul(h, x) shouldBe biquaternions.mul(x, h)
            }
        }

        branch("Biquaternion<ℚ>: the norm w² + x² + y² + z² is q·conj(q), and is exactly multiplicative") {
            val embed = BiquaternionAlgebras.complexToBiquaternionMonomorphism(q)
            law(NormSqMultiplicativeLaw(biquaternions.mul.op, biquaternions.normSq, complexQ.mul.op, arbBiquaternion))
            local("the norm is the sum of squares and q·conj(q)") {
                lawCheckAll(arbBiquaternion) { x ->
                    val expected = x.coefficients().map { c -> complexQ.mul(c, c) }.reduce(complexQ.add::invoke)
                    biquaternions.normSq(x) shouldBe expected
                    biquaternions.mul(x, biquaternions.conj(x)) shouldBe embed(expected)
                }
            }
        }

        local("Biquaternion<ℚ>: the complex conjugate is an automorphism; the Hermitian conjugate reverses products") {
            val complexConj = BiquaternionAlgebras.complexConj(q)
            val hermitianConj = BiquaternionAlgebras.hermitianConj(q)
            lawCheckAll(arbBiquaternion, arbBiquaternion) { x, y ->
                complexConj(complexConj(x)) shouldBe x
                complexConj(biquaternions.mul(x, y)) shouldBe biquaternions.mul(complexConj(x), complexConj(y))
                hermitianConj(hermitianConj(x)) shouldBe x
                hermitianConj(biquaternions.mul(x, y)) shouldBe biquaternions.mul(hermitianConj(y), hermitianConj(x))
                hermitianConj(x) shouldBe biquaternions.conj(complexConj(x))
            }
        }

        local("Biquaternion<ℚ>: zero divisors, and exact reciprocals off the null cone") {
            val (one, i) = BiquaternionAlgebras.basis(q)
            val hi = biquaternions.mul(BiquaternionAlgebras.centralUnit(q), i)
            val plus = biquaternions.add(one, hi)
            val minus = biquaternions.add(one, biquaternions.add.inverse(hi))
            biquaternions.mul(plus, minus) shouldBe biquaternions.zero
            biquaternions.normSq(plus) shouldBe zeroC
            biquaternions.hasReciprocal(plus) shouldBe false
            shouldThrow<ArithmeticException> { biquaternions.reciprocal(plus) }
            shouldThrow<ArithmeticException> { biquaternions.reciprocal(biquaternions.zero) }

            lawCheckAll(arbBiquaternion) { x ->
                if (biquaternions.hasReciprocal(x)) {
                    val inverse = biquaternions.reciprocal(x)
                    biquaternions.mul(x, inverse) shouldBe one
                    biquaternions.mul(inverse, x) shouldBe one
                } else {
                    biquaternions.normSq(x) shouldBe zeroC
                }
            }
        }

        branch("Biquaternion<ℚ>: module over ℚ, star algebra over Complex<ℚ>, Hermitian star algebra over ℚ") {
            suite(RModuleLaws(BiquaternionAlgebras.biquaternionModule(q), smallRational, arbBiquaternion))
            suite(StarAlgebraLaws(BiquaternionAlgebras.biquaternionStarAlgebra(q), arbComplex, arbBiquaternion))
            suite(StarAlgebraLaws(BiquaternionAlgebras.biquaternionHermitianStarAlgebra(q), smallRational, arbBiquaternion))
        }

        branch("the four embeddings into Biquaternion<ℚ> are injective unital ring homomorphisms") {
            val target = BiquaternionAlgebras.biquaternionRing(q)

            val fromComplex = BiquaternionAlgebras.complexToBiquaternionMonomorphism(q)
            suite(RingHomomorphismLaws(fromComplex::invoke, complexQ, target, arbComplex))
            law(injectivityLaw(hom = fromComplex::invoke, arbA = arbComplex))
            val fromQuaternion = BiquaternionAlgebras.quaternionToBiquaternionMonomorphism(q)
            val arbQuaternion = ArbRationalQuaternion.smallRationalQuaternion
            suite(RingHomomorphismLaws(fromQuaternion::invoke, QuaternionAlgebras.quaternionRing(q), target, arbQuaternion))
            law(injectivityLaw(hom = fromQuaternion::invoke, arbA = arbQuaternion))
            val fromSplit = BiquaternionAlgebras.splitQuaternionToBiquaternionMonomorphism(q)
            val arbSplit = ArbSplitQuaternion.rationalSplitQuaternion
            suite(RingHomomorphismLaws(fromSplit::invoke, SplitQuaternionAlgebras.splitQuaternionRing(q), target, arbSplit))
            law(injectivityLaw(hom = fromSplit::invoke, arbA = arbSplit))
            val fromBicomplex = BiquaternionAlgebras.bicomplexToBiquaternionMonomorphism(q)
            suite(RingHomomorphismLaws(fromBicomplex::invoke, bicomplex, target, arbBicomplex))
            law(injectivityLaw(hom = fromBicomplex::invoke, arbA = arbBicomplex))
            local("images, fixed rings and bases") {
                // The quaternions are the fixed ring of the complex conjugate.
                lawCheckAll(arbQuaternion) { x ->
                    BiquaternionAlgebras.complexConj(q)(fromQuaternion(x)) shouldBe fromQuaternion(x)
                }
            }
        }

        // ── Bioctonion<ℚ> ────────────────────────────────────────────────────────

        val bioctonions = BioctonionAlgebras.bioctonionRingWithReciprocal(q)

        branch("Bioctonion<ℚ> is exactly a nonassociative involutive ring, alternative and Moufang") {
            val ring = BioctonionAlgebras.bioctonionRing(q)
            suite(NonAssociativeInvolutiveRingLaws(ring, arbBioctonion))
            suite(AlternativityLaw(ring.mul.op, arbBioctonion))
            suite(MoufangLaw(ring.mul.op, arbBioctonion))
        }

        local("Bioctonion<ℚ>: units square to -1, h is central, and the algebra is not associative") {
            val basis = BioctonionAlgebras.basis(q)
            val h = BioctonionAlgebras.centralUnit(q)
            basis.size shouldBe 8
            (basis.drop(1) + h).forEach { u -> bioctonions.mul(u, u) shouldBe bioctonions.negOne }
            lawCheckAll(arbBioctonion) { x ->
                bioctonions.mul(h, x) shouldBe bioctonions.mul(x, h)
            }
            val (e1, e2, e4) = listOf(1, 2, 4).map(basis::get)
            bioctonions.mul(bioctonions.mul(e1, e2), e4) shouldBe
                bioctonions.add.inverse(bioctonions.mul(e1, bioctonions.mul(e2, e4)))
        }

        branch("Bioctonion<ℚ>: the norm c₀² + … + c₇² is exactly multiplicative (a composition algebra)") {
            law(NormSqMultiplicativeLaw(bioctonions.mul.op, bioctonions.normSq, complexQ.mul.op, arbBioctonion))
            local("the norm is the sum of squares") {
                lawCheckAll(arbBioctonion) { x ->
                    bioctonions.normSq(x) shouldBe x.coefficients().map { c -> complexQ.mul(c, c) }.reduce(complexQ.add::invoke)
                }
            }
        }

        local("Bioctonion<ℚ>: zero divisors, and exact two-sided reciprocals off the null cone") {
            val basis = BioctonionAlgebras.basis(q)
            val he1 = bioctonions.mul(BioctonionAlgebras.centralUnit(q), basis[1])
            val plus = bioctonions.add(basis[0], he1)
            val minus = bioctonions.add(basis[0], bioctonions.add.inverse(he1))
            bioctonions.mul(plus, minus) shouldBe bioctonions.zero
            bioctonions.hasReciprocal(plus) shouldBe false
            shouldThrow<ArithmeticException> { bioctonions.reciprocal(plus) }

            lawCheckAll(arbBioctonion) { x ->
                if (bioctonions.hasReciprocal(x)) {
                    val inverse = bioctonions.reciprocal(x)
                    bioctonions.mul(x, inverse) shouldBe basis[0]
                    bioctonions.mul(inverse, x) shouldBe basis[0]
                }
            }
        }

        branch("Bioctonion<ℚ>: complex conjugate is an automorphism; module and star-algebra laws") {
            val complexConj = BioctonionAlgebras.complexConj(q)
            suite(RModuleLaws(BioctonionAlgebras.bioctonionModule(q), smallRational, arbBioctonion))
            suite(NonAssociativeStarAlgebraLaws(BioctonionAlgebras.bioctonionStarAlgebra(q), arbComplex, arbBioctonion))
            suite(NonAssociativeStarAlgebraLaws(
                BioctonionAlgebras.bioctonionHermitianStarAlgebra(q), smallRational, arbBioctonion
            ))
            local("the complex conjugate is an involutive automorphism") {
                lawCheckAll(arbBioctonion, arbBioctonion) { x, y ->
                    complexConj(complexConj(x)) shouldBe x
                    complexConj(bioctonions.mul(x, y)) shouldBe bioctonions.mul(complexConj(x), complexConj(y))
                }
            }
        }

        branch("the four embeddings into Bioctonion<ℚ> are injective unital homomorphisms") {
            val target = BioctonionAlgebras.bioctonionRing(q)

            val fromComplex = BioctonionAlgebras.complexToBioctonionMonomorphism(q)
            suite(NonAssociativeRingHomomorphismLaws(fromComplex::invoke, complexQ, target, arbComplex))
            law(injectivityLaw(hom = fromComplex::invoke, arbA = arbComplex))
            val fromOctonion = BioctonionAlgebras.octonionToBioctonionMonomorphism(q)
            val arbOctonion = ArbRationalOctonion.smallRationalOctonion
            suite(NonAssociativeRingHomomorphismLaws(fromOctonion::invoke, OctonionAlgebras.octonionRing(q), target, arbOctonion))
            law(injectivityLaw(hom = fromOctonion::invoke, arbA = arbOctonion))
            val fromSplit = BioctonionAlgebras.splitOctonionToBioctonionMonomorphism(q)
            val arbSplit = ArbSplitOctonion.rationalSplitOctonion
            suite(NonAssociativeRingHomomorphismLaws(
                fromSplit::invoke, SplitOctonionAlgebras.splitOctonionRing(q), target, arbSplit
            ))
            law(injectivityLaw(hom = fromSplit::invoke, arbA = arbSplit))
            val fromBiquaternion = BioctonionAlgebras.biquaternionToBioctonionMonomorphism(q)
            suite(NonAssociativeRingHomomorphismLaws(
                fromBiquaternion::invoke, BiquaternionAlgebras.biquaternionRing(q), target, arbBiquaternion
            ))
            law(injectivityLaw(hom = fromBiquaternion::invoke, arbA = arbBiquaternion))
            local("the embeddings send bases to bases") {
                BiquaternionAlgebras.basis(q).map(fromBiquaternion::invoke) shouldBe BioctonionAlgebras.basis(q).take(4)
            }
        }

        // ── A finite field ───────────────────────────────────────────────────────

        branch("over 𝔽₃ the three algebras satisfy their ring laws, and reciprocals follow the contract") {
            val f3 = PrimeField(BigInteger.valueOf(3))
            val arbF3 = Arb.int(0..2).map(Int::toBigInteger)
            val arbC = Arb.bind(arbF3, arbF3) { a, b -> complex(a, b) }
            val arbB = Arb.bind(arbC, arbC) { z1, z2 -> Bicomplex.ofStandard(z1, z2) }
            val arbH = Arb.bind(arbC, arbC, arbC, arbC) { w, x, y, z -> biquaternion(w, x, y, z) }
            val arbO = Arb.list(arbC, 8..8).map { cs -> bioctonion(cs) }

            suite(CommutativeRingLaws(BicomplexAlgebras.bicomplexRing(f3), arbB))
            suite(InvolutiveRingLaws(BiquaternionAlgebras.biquaternionRing(f3), arbH))
            suite(NonAssociativeInvolutiveRingLaws(BioctonionAlgebras.bioctonionRing(f3), arbO))
            suite(AlternativityLaw(BioctonionAlgebras.bioctonionRing(f3).mul.op, arbO))
            val ring = BiquaternionAlgebras.biquaternionRingWithReciprocal(f3)
            local("reciprocals follow the contract") {
                lawCheckAll(arbH) { x ->
                    if (ring.hasReciprocal(x)) {
                        ring.mul(x, ring.reciprocal(x)) shouldBe ring.one
                    } else {
                        shouldThrow<ArithmeticException> { ring.reciprocal(x) }
                    }
                }
            }
        }

        // ── Agreement with the named real structures ─────────────────────────────

        local("over ℝ the generic constructions are the named ones") {
            val real = RealAlgebras.RealField

            val genericB = BicomplexAlgebras.bicomplexRing(real)
            val namedB = BicomplexAlgebras.BicomplexCommutativeRing
            lawCheckAll(ArbBicomplex.bicomplex, ArbBicomplex.bicomplex) { x, y ->
                genericB.mul(x, y) shouldBe namedB.mul(x, y)
                genericB.add(x, y) shouldBe namedB.add(x, y)
                BicomplexAlgebras.conj1(real)(x) shouldBe namedB.conj1(x)
                BicomplexAlgebras.conj2(real)(x) shouldBe namedB.conj2(x)
                BicomplexAlgebras.conj3(real)(x) shouldBe namedB.conj3(x)
            }

            val genericH = BiquaternionAlgebras.biquaternionRing(real)
            val namedH = BiquaternionAlgebras.BiquaternionRing
            lawCheckAll(ArbBiquaternion.biquaternion, ArbBiquaternion.biquaternion) { x, y ->
                genericH.mul(x, y) shouldBe namedH.mul(x, y)
                genericH.conj(x) shouldBe namedH.conj(x)
                BiquaternionAlgebras.normSq(real)(x) shouldBe namedH.normSq(x)
                BiquaternionAlgebras.complexConj(real)(x) shouldBe namedH.complexConj(x)
                BiquaternionAlgebras.hermitianConj(real)(x) shouldBe namedH.hermitianConj(x)
            }
            BiquaternionAlgebras.centralUnit(real) shouldBe namedH.h
            BiquaternionAlgebras.basis(real) shouldBe namedH.basis

            val genericO = BioctonionAlgebras.bioctonionRing(real)
            val namedO = BioctonionAlgebras.BioctonionAlgebra
            lawCheckAll(ArbBioctonion.bioctonion, ArbBioctonion.bioctonion) { x, y ->
                genericO.mul(x, y) shouldBe namedO.mul(x, y)
                genericO.conj(x) shouldBe namedO.conj(x)
                BioctonionAlgebras.normSq(real)(x) shouldBe namedO.normSq(x)
                BioctonionAlgebras.complexConj(real)(x) shouldBe namedO.complexConj(x)
            }
            BioctonionAlgebras.centralUnit(real) shouldBe namedO.h
            BioctonionAlgebras.basis(real) shouldBe namedO.basis
        }

        local("generic Eq instances compare coordinatewise") {
            val eqR = RationalAlgebras.eqRational
            lawCheckAll(arbBicomplex, arbBiquaternion, arbBioctonion) { b, h, o ->
                BicomplexAlgebras.eqBicomplex(eqR)(b, b) shouldBe true
                BiquaternionAlgebras.eqBiquaternion(q, eqR)(h, h) shouldBe true
                BioctonionAlgebras.eqBioctonion(q, eqR)(o, o) shouldBe true
            }
            BiquaternionAlgebras.eqBiquaternion(q, eqR)(
                BiquaternionAlgebras.biquaternionRing(q).one, BiquaternionAlgebras.centralUnit(q)
            ) shouldBe false
            real(Rational.ONE) shouldBe complexQ.one
        }
    })
})
