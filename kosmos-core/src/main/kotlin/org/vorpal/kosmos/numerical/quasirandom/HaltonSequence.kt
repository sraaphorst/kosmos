package org.vorpal.kosmos.numerical.quasirandom

import org.vorpal.kosmos.combinatorics.sequences.VanDerCorput
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.numbertheory.primes.PrimeSequence
import java.math.BigInteger

/**
 * **Halton sequence** in arbitrary dimension, built from Van der Corput components.
 *
 * For a list of bases `b₁, …, b_d` with `bᵢ ≥ 2`, the Halton sequence is the
 * `d`-dimensional low–discrepancy sequence
 *
 * ```
 * Hₙ = ( φ_{b₁}(n), φ_{b₂}(n), …, φ_{b_d}(n) ),   n = 0, 1, 2, …
 * ```
 *
 * where `φ_{bᵢ}` is the one–dimensional Van der Corput radical inverse map in
 * base `bᵢ`. Intuitively, each coordinate is a Van der Corput sequence in a
 * different base, and Halton simply evaluates all of them at the same index `n`.
 *
 * This implementation:
 *
 * * uses [VanDerCorput] from the combinatorics layer as an exact, rational
 *   primitive,
 * * converts each coordinate to `Double` via `Rational.toDouble()`,
 * * returns each point as a `List<Double>` of length `dimension`.
 *
 * The resulting sequence lives in `[0,1]^dimension` and is well–suited for
 * quasi–Monte Carlo and stratified sampling tasks.
 *
 * ### Dimension and bases
 *
 * The dimension of the sequence is the length of [bases]. Typical usage is:
 *
 * - `dimension = 2` with `bases = [2, 3]`       for 2D sampling,
 * - `dimension = 3` with `bases = [2, 3, 5]`    for 3D sampling,
 * - or any higher dimension with distinct bases.
 *
 * It is **recommended (but not enforced)** that the bases be pairwise coprime,
 * and in practice one usually chooses the first `d` prime numbers to reduce
 * correlations between coordinates. This can be done by:
 *
 * ```kotlin
 * val haltonN = HaltonSequence(PrimeSequence.take(n))
 * ```
 *
 * ### Example
 *
 * ```kotlin
 * import org.vorpal.kosmos.combinatorics.sequences.VanDerCorput
 * import org.vorpal.kosmos.numerical.quasirandom.HaltonSequence
 *
 * // 2D Halton sequence using bases 2 and 3
 * val halton2d = HaltonSequence(intArrayOf(2, 3))
 *
 * // First few points in [0,1]^2
 * val p0: DoubleArray = halton2d.closedForm(0)  // [0.0,      0.0     ]
 * val p1: DoubleArray = halton2d.closedForm(1)  // [0.5,      1/3 ≈ 0.3333]
 * val p2: DoubleArray = halton2d.closedForm(2)  // [0.25,     2/3 ≈ 0.6667]
 * val p3: DoubleArray = halton2d.closedForm(3)  // [0.75,     1/9 ≈ 0.1111]
 *
 * // Iterate a bunch of points, e.g. for quasi-Monte Carlo integration
 * for (n in 0 until 1024) {
 *     val point = halton2d.closedForm(n)
 *     val x = point[0]
 *     val y = point[1]
 *     // use (x, y) as a sample in [0,1]^2
 * }
 * ```
 *
 * In rendering or numerical experiments, you’ll often wrap these points into a
 * vector type (e.g. `Vec2D`, `Vec3D`) or transform them into other domains
 * (e.g. using inverse CDFs or domain mappings).
 *
 * ### Relationship to [VanDerCorput]
 *
 * The Halton sequence is built entirely from [VanDerCorput] components. For each
 * base `bᵢ` in [bases], an internal Van der Corput sequence `φ_{bᵢ}` is created,
 * and the `n`-th Halton point is:
 *
 * ```
 * Hₙ[i] = φ_{bᵢ}(n).toDouble()
 * ```
 *
 * Thus:
 *
 * - The **combinatorics layer** provides exact rational evaluations.
 * - The **numerical/quasirandom layer** converts them to `Double` and organizes
 *   them into points in `[0,1]^d`.
 *
 * ### Caching and performance
 *
 * [HaltonSequence] implements [CachedClosedForm], and internally each component
 * [VanDerCorput] also caches its own closed form. This means:
 *
 * - Each coordinate value `Hₙ[i]` is computed at most once per `n`,
 * - Subsequent calls to [closedForm] for the same `n` are served from cache,
 * - Different coordinates reuse the underlying Van der Corput caches.
 *
 * In typical usage (thousands or millions of samples), this provides a very
 * lightweight, structured alternative to pseudorandom sampling for many
 * integration and sampling tasks.
 *
 * @property bases array of bases `bᵢ ≥ 2`, one per dimension. The dimension
 * of the sequence is `bases.size`. For best low–discrepancy behaviour, choose
 * distinct, preferably prime, bases.
 *
 * @see VanDerCorput for the underlying one–dimensional radical inverse sequence
 * used to construct each coordinate.
 */
class HaltonSequence(
    bases: List<BigInteger>
) : CachedClosedFormImplementation<List<Double>>() {

    // Defensive copy and conversion.
    val bases: List<BigInteger> = bases.toList()

    private val baseInts: IntArray = bases.map { b ->
        require(b >= BigInteger.TWO) { "Base must be at least 2, but was $b." }
        require(b <= BigInteger.valueOf(Int.MAX_VALUE.toLong())) {
            "Base $b is too large to fit into an Int."
        }
        b.toInt()
    }.toIntArray()

    private val components = baseInts.map { VanDerCorput(it) }

    init {
        require(bases.isNotEmpty()) { "At least one base is required for a Halton sequence." }
    }

    override fun closedFormCalculator(n: Int): List<Double> {
        require(n >= 0) { "Index n must be non-negative, but was $n." }
        return components.map { it.closedForm(n).toDouble() }
    }
}
