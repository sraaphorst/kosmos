package org.vorpal.kosmos.hypercomplex.bioctonion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.hypercomplex.complex.Complex

/**
 * A bioctonion (complex octonion): an octonion `c₀ + c₁e₁ + … + c₇e₇` with *complex* coefficients,
 * ```text
 * 𝕆 ⊗_ℝ ℂ.
 * ```
 * The complex scalars commute and associate with everything. As for the biquaternions, the
 * imaginary unit of the coefficient field ℂ is written `h` in this package's documentation, to keep
 * it apart from the octonion units `e₁, …, e₇`.
 *
 * Algebraically this is the Cayley-Dickson doubling of the biquaternions,
 * ```text
 * Bioctonion = CD⟨ℍ ⊗ ℂ⟩,
 * ```
 * i.e. the octonion algebra over ℂ. It is nonassociative but alternative, and it is a composition
 * algebra for its complex-valued norm `c₀² + c₁² + … + c₇²`. That norm is isotropic, so the
 * bioctonions have zero divisors, e.g. `(1 + h·e₁)(1 - h·e₁) = 0`. Both the octonions and the
 * split-octonions are real forms of it.
 *
 * [Bioctonion] is a value class rather than a typealias of [CD] because its carrier
 * `CD<CD<CD<Complex>>> = CD<Octonion>` is also the carrier that the sedenions will use.
 *
 * This file defines only carrier-level naming and accessors. The algebraic structure is supplied
 * by [BioctonionAlgebras].
 */
@JvmInline
value class Bioctonion(val cd: CD<CD<CD<Complex>>>) {

    /**
     * The complex coefficients `[c₀, c₁, …, c₇]` in the basis `1, e₁, …, e₇`.
     */
    fun coefficients(): List<Complex> =
        listOf(
            cd.a.a.a, cd.a.a.b, cd.a.b.a, cd.a.b.b,
            cd.b.a.a, cd.b.a.b, cd.b.b.a, cd.b.b.b
        )

    /**
     * The complex coefficient of the `n`-th basis element (`n = 0` is the scalar part).
     */
    operator fun get(n: Int): Complex =
        coefficients()[n]

    override fun toString(): String =
        "Bioctonion(${coefficients().joinToString(", ")})"
}

/**
 * The bioctonion with complex coefficients `[c₀, c₁, …, c₇]` in the basis `1, e₁, …, e₇`.
 */
fun bioctonion(coefficients: List<Complex>): Bioctonion {
    require(coefficients.size == 8) { "A bioctonion has 8 complex coefficients: got ${coefficients.size}." }
    val (c0, c1, c2, c3) = coefficients.subList(0, 4)
    val (c4, c5, c6, c7) = coefficients.subList(4, 8)
    return Bioctonion(CD(CD(CD(c0, c1), CD(c2, c3)), CD(CD(c4, c5), CD(c6, c7))))
}
