package org.vorpal.kosmos.hypercomplex.bioctonion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.hypercomplex.complex.Complex

/**
 * A bioctonion over a commutative base ring [R].
 * ```text
 * Bioctonion(R) = Octonion(R) ⊗_R Complex(R) ≅ Octonion(Complex(R)).
 * ```
 * Elements have the form `c₀ + c₁e₁ + … + c₇e₇` with coefficients in `Complex(R)`. The complex
 * scalars commute and associate with everything. As for the biquaternions, the imaginary unit of
 * the coefficient ring is written `h` in this package's documentation, to keep it apart from the
 * octonion units `e₁, …, e₇`.
 *
 * This is a nonassociative, alternative algebra of rank 16 over `R`: the Cayley-Dickson doubling of
 * the biquaternions over `R`. It is a composition algebra for its `Complex(R)`-valued norm
 * `c₀² + c₁² + … + c₇²`.
 *
 * The classical case has a name: [RealBioctonion] `= Bioctonion<Real>`, the complex octonions
 * `𝕆 ⊗_ℝ ℂ`. Their norm is isotropic, so they have zero divisors, e.g.
 * `(1 + h·e₁)(1 - h·e₁) = 0`. Both the octonions and the split-octonions are real forms.
 *
 * [Bioctonion] is a value class rather than a typealias of [CD] so that it is a distinct Kotlin type
 * from the other algebras carried by nested Cayley-Dickson pairs.
 *
 * Algebraic operations are supplied by [BioctonionAlgebras].
 */
@JvmInline
value class Bioctonion<R : Any>(val cd: CD<CD<CD<Complex<R>>>>) {

    /**
     * The eight complex coefficients `[c₀, c₁, …, c₇]` in the basis `1, e₁, …, e₇`.
     */
    fun coefficients(): List<Complex<R>> =
        listOf(
            cd.a.a.a, cd.a.a.b, cd.a.b.a, cd.a.b.b,
            cd.b.a.a, cd.b.a.b, cd.b.b.a, cd.b.b.b
        )

    /**
     * The complex coefficient of the `n`-th basis element (`n = 0` is the scalar part).
     */
    operator fun get(n: Int): Complex<R> =
        coefficients()[n]

    /**
     * The sixteen base-ring coefficients, in the order `[Re c₀, Im c₀, …, Re c₇, Im c₇]`.
     */
    fun baseCoefficients(): List<R> =
        coefficients().flatMap { c -> listOf(c.re, c.im) }

    override fun toString(): String =
        "Bioctonion(${coefficients().joinToString(", ")})"
}

/** The complex octonions `𝕆 ⊗_ℝ ℂ`. */
typealias RealBioctonion = Bioctonion<Real>

/**
 * The bioctonion with complex coefficients `[c₀, c₁, …, c₇]` in the basis `1, e₁, …, e₇`.
 */
fun <R : Any> bioctonion(coefficients: List<Complex<R>>): Bioctonion<R> {
    require(coefficients.size == 8) { "A bioctonion has 8 complex coefficients: got ${coefficients.size}." }
    val (c0, c1, c2, c3) = coefficients.subList(0, 4)
    val (c4, c5, c6, c7) = coefficients.subList(4, 8)
    return Bioctonion(CD(CD(CD(c0, c1), CD(c2, c3)), CD(CD(c4, c5), CD(c6, c7))))
}
