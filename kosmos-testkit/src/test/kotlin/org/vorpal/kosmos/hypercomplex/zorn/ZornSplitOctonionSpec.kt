package org.vorpal.kosmos.hypercomplex.zorn

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.FiniteFieldAlgebras
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitoctonion.ArbSplitOctonion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.linear.values.Vec3
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The Zorn vector-matrices are a model of the split-octonions.
 *
 * Over ℚ, `φ : SplitOctonion<ℚ> → Zorn<ℚ>` is checked to be an isomorphism of rings with
 * involution that carries the split norm to `ad − v·w`. Over ℤ it is injective but not onto, over
 * 𝔽₃ it is still a homomorphism, and over 𝔽₂ it is not injective, so the isomorphism is refused.
 */
class ZornSplitOctonionSpec : FunSpec({
    lawTests(lawGraph("Zorn ≅ 𝕆′") {
        val q = RationalAlgebras.RationalField
        val splitOctonions = SplitOctonionAlgebras.splitOctonionRing(q)
        val zorns = ZornAlgebras.zornRing(q)
        val mono = ZornAlgebras.splitOctonionToZornHomomorphism(q)
        val iso = ZornAlgebras.splitOctonionZornIsomorphism(q)
        val arbO = ArbSplitOctonion.rationalSplitOctonion
        val arbZ = ArbZorn.rationalZorn

        branch("φ : SplitOctonion<ℚ> → Zorn<ℚ> is a unital ring homomorphism") {
            suite(NonAssociativeRingHomomorphismLaws(
                hom = mono::invoke,
                domain = splitOctonions,
                codomain = zorns,
                arb = arbO
            ))
        }

        local("φ(pq) = φ(p)φ(q) and φ(1) = 1") {
            mono(splitOctonions.one) shouldBe zorns.one
            lawCheckAll(arbO, arbO) { p, r ->
                mono(splitOctonions.mul(p, r)) shouldBe zorns.mul(mono(p), mono(r))
            }
        }

        local("φ commutes with conjugation") {
            lawCheckAll(arbO) { o ->
                mono(splitOctonions.conj(o)) shouldBe zorns.conj(mono(o))
            }
        }

        local("φ carries the split norm to the Zorn determinant: N(φ(o)) = w² + x² + y² + z² − u² − v² − s² − t²") {
            val splitNorm = SplitOctonionAlgebras.normSq(q)
            val zornNorm = ZornAlgebras.normSq(q)
            lawCheckAll(arbO) { o ->
                zornNorm(mono(o)) shouldBe splitNorm(o)
            }
        }

        law("φ is injective over ℚ", injectivityLaw(hom = mono::invoke, arbA = arbO))

        local("the isomorphism and its inverse are mutually inverse") {
            lawCheckAll(arbO) { o ->
                iso.backward(iso(o)) shouldBe o
            }
            lawCheckAll(arbZ) { x ->
                iso(iso.backward(x)) shouldBe x
            }
        }

        branch("the inverse Zorn<ℚ> → SplitOctonion<ℚ> is a unital ring homomorphism") {
            suite(NonAssociativeRingHomomorphismLaws(
                hom = iso.backward::invoke,
                domain = zorns,
                codomain = splitOctonions,
                arb = arbZ
            ))
        }

        local("the diagonal idempotents correspond to (1 ± e₄)/2") {
            val half = Rational.of(1, 2)
            val zero = Rational.ZERO
            val zeroVec = Vec3(zero, zero, zero)
            val basis = SplitOctonionAlgebras.basis(q)
            val action = SplitOctonionAlgebras.splitOctonionLeftAction(q)
            iso.backward(Zorn(Rational.ONE, zeroVec, zeroVec, zero)) shouldBe
                action(half, splitOctonions.add(basis[0], basis[4]))
            iso.backward(Zorn(zero, zeroVec, zeroVec, Rational.ONE)) shouldBe
                action(half, splitOctonions.add(basis[0], splitOctonions.add.inverse(basis[4])))
        }

        branch("over ℤ, φ is an injective homomorphism but not onto: every image has a ≡ d (mod 2)") {
            val z = IntegerAlgebras.IntegerCommutativeRing
            val integerMono = ZornAlgebras.splitOctonionToZornHomomorphism(z)
            val arbIntO = ArbSplitOctonion.splitOctonionOf(Arb.int(-20..20).map(Int::toBigInteger))
            val two = BigInteger.TWO

            suite(NonAssociativeRingHomomorphismLaws(
                hom = integerMono::invoke,
                domain = SplitOctonionAlgebras.splitOctonionRing(z),
                codomain = ZornAlgebras.zornRing(z),
                arb = arbIntO
            ))
            law(injectivityLaw(hom = integerMono::invoke, arbA = arbIntO))
            val zeroVec = Vec3(Rational.ZERO, Rational.ZERO, Rational.ZERO)
            val preimage = iso.backward(Zorn(Rational.ONE, zeroVec, zeroVec, Rational.ZERO))
            local("a ≡ d (mod 2)") {
                lawCheckAll(arbIntO) { o ->
                    val x = integerMono(o)
                    (x.a - x.d).mod(two) shouldBe BigInteger.ZERO
                }

                // So the idempotent (1, 0, 0, 0) is not in the image: over ℚ its preimage is (1 + e₄)/2.
                preimage.w shouldBe Rational.of(1, 2)
                preimage.u shouldBe Rational.of(1, 2)
            }
        }

        branch("the Zorn ring over ℤ satisfies NonAssociativeInvolutiveRingLaws and is alternative") {
            val z = IntegerAlgebras.IntegerCommutativeRing
            val ring = ZornAlgebras.zornRing(z)
            suite(NonAssociativeInvolutiveRingLaws(ring, ArbZorn.integerZorn))
            suite(AlternativityLaw(ring.mul.op, ArbZorn.integerZorn))
        }

        branch("over 𝔽₃, the Zorn ring satisfies the laws and φ is an isomorphism") {
            val f3 = PrimeField(BigInteger.valueOf(3))
            val arbF3: Arb<BigInteger> = Arb.int(0..2).map(Int::toBigInteger)
            val ring = ZornAlgebras.zornRing(f3)
            val f3Iso = ZornAlgebras.splitOctonionZornIsomorphism(f3)
            val arbF3O = ArbSplitOctonion.splitOctonionOf(arbF3)
            val arbF3Z = ArbZorn.zornOf(arbF3)

            suite(NonAssociativeInvolutiveRingLaws(ring, arbF3Z))
            suite(AlternativityLaw(ring.mul.op, arbF3Z))
            suite(NonAssociativeRingHomomorphismLaws(
                hom = f3Iso::invoke,
                domain = SplitOctonionAlgebras.splitOctonionRing(f3),
                codomain = ring,
                arb = arbF3O
            ))
            local("φ is invertible") {
                lawCheckAll(arbF3O) { o -> f3Iso.backward(f3Iso(o)) shouldBe o }
                lawCheckAll(arbF3Z) { x -> f3Iso(f3Iso.backward(x)) shouldBe x }
            }
        }

        local("over 𝔽₂, φ is not injective: φ(1) = φ(e₄) = 1, and the isomorphism is refused") {
            val f2 = FiniteFieldAlgebras.F2
            val f2Mono = ZornAlgebras.splitOctonionToZornHomomorphism(f2)
            val basis = SplitOctonionAlgebras.basis(f2)
            (basis[0] == basis[4]) shouldBe false
            f2Mono(basis[0]) shouldBe f2Mono(basis[4])
            f2Mono(basis[4]) shouldBe ZornAlgebras.zornRing(f2).one

            shouldThrow<IllegalArgumentException> {
                ZornAlgebras.splitOctonionZornIsomorphism(f2)
            }
        }
    })
})
