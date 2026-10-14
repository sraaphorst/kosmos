package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CarrierTransport
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.contramap
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.Complex

/**
 * The quaternion-level construction over a commutative ring of scalars `S`, delivered on a public
 * carrier `Q`.
 *
 * Every quaternion-level algebra in Kosmos is two Cayley-Dickson doublings of its scalars:
 * ```text
 * S   ──commutative, σ = 1──▶   S[i] = CD⟨S⟩   ──associative, σ = ±1──▶   CD⟨CD⟨S⟩⟩
 * ```
 * and differs from the others only in the scalars, the sign of the second doubling, and the Kotlin
 * type that names the result:
 * ```text
 * Quaternion<R>        usual   over R        ℍ (R = ℝ), ℍ_ℚ (R = ℚ), Lipschitz (R = ℤ)
 * SplitQuaternion<R>   split   over R
 * Biquaternion         usual   over ℂ
 * ```
 * A [QuaternionTier] holds everything that depends only on those choices: the associative ring,
 * the basis `1, i, j, k`, coordinates `[w, x, y, z]`, the coordinatewise scalar action, the reduced
 * norm, the embeddings of the complex-like level, an [Eq] and a printable. All of it is expressed on
 * the public carrier `Q`, so the `*Algebras` objects never handle a raw [CD].
 *
 * The public `*Algebras` objects then add only what genuinely differs: a reciprocal, a vector space
 * or `ℤ`-module, extra conjugations, and conversions.
 *
 * @param scalars the coefficient ring `S`.
 * @param wrap names a raw Cayley-Dickson pair as a `Q`. Must be inverse to [unwrap].
 * @param unwrap the raw Cayley-Dickson pair of a `Q`.
 * @param split `false` for the usual doubling (`j² = k² = -1`), `true` for the split doubling
 *   (`j² = k² = +1`).
 */
internal class QuaternionTier<S : Any, Q : Any>(
    val scalars: CommutativeRing<S>,
    val wrap: (CD<CD<S>>) -> Q,
    val unwrap: (Q) -> CD<CD<S>>,
    val split: Boolean = false
) {
    /**
     * The complex-like level `S[i]/(i² + 1)`: the commutative doubling of the scalars, with its
     * conjugation `a + bi ↦ a - bi`.
     */
    val complexRing: CommutativeInvolutiveRing<CD<S>> =
        CayleyDickson.commutative(scalars, scalars.one)

    /**
     * The associative Cayley-Dickson double on the raw carrier: usual (`σ = 1`) or split (`σ = -1`).
     * The octonion tier doubles this once more.
     */
    val rawRing: InvolutiveRing<CD<CD<S>>> =
        if (split) CayleyDickson.associativeSplit(complexRing)
        else CayleyDickson.associative(complexRing)

    /**
     * The ring on the public carrier.
     */
    val ring: InvolutiveRing<Q> =
        CarrierTransport<CD<CD<S>>, Q>(
            wrap = UnaryOp { cd -> wrap(cd) },
            unwrap = UnaryOp { q -> unwrap(q) }
        ).involutiveRing(rawRing)

    /**
     * The coordinates `[w, x, y, z]` of `w + xi + yj + zk = (w + xi, y + zi)`.
     */
    val coordinates: UnaryOp<Q, List<S>> =
        UnaryOp { q ->
            val cd = unwrap(q)
            listOf(cd.a.a, cd.a.b, cd.b.a, cd.b.b)
        }

    /**
     * The quaternion `w + xi + yj + zk` from its coordinates `[w, x, y, z]`.
     */
    val fromCoordinates: UnaryOp<List<S>, Q> =
        UnaryOp { cs ->
            require(cs.size == 4) { "A quaternion has 4 coordinates: got ${cs.size}." }
            wrap(CD(CD(cs[0], cs[1]), CD(cs[2], cs[3])))
        }

    /**
     * The standard basis `1, i, j, k`, obtained by doubling the basis `1, i` of `S[i]`.
     */
    val basis: List<Q> =
        CayleyDickson.doubledBasis(
            zero = complexRing.zero,
            baseBasis = CayleyDickson.doubledBasis(scalars.zero, listOf(scalars.one))
        ).map(wrap)

    /**
     * The coordinatewise scalar action `s ⊳ (w + xi + yj + zk) = sw + sx·i + sy·j + sz·k`.
     *
     * The scalars are central, so this is also left (and right) multiplication by the embedded scalar.
     */
    val scalarAction: LeftAction<S, Q> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { s, q ->
            fromCoordinates(coordinates(q).map { c -> scalars.mul(s, c) })
        }

    /**
     * The reduced norm `N(q) = q·conj(q)`, read off the scalar coordinate of the Cayley-Dickson
     * product: `w² + x² + y² + z²` for the usual doubling and `w² + x² - y² - z²` for the split one.
     */
    val normSq: UnaryOp<Q, S> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { q ->
            val cd = unwrap(q)
            rawRing.mul(cd, rawRing.conj(cd)).a.a
        }

    /**
     * The embedding of the scalars, `s ↦ s + 0i + 0j + 0k`.
     */
    val scalarEmbeddingMap: UnaryOp<S, Q> =
        UnaryOp { s -> fromCoordinates(listOf(s, scalars.zero, scalars.zero, scalars.zero)) }

    /**
     * The map `Complex<S> → Q` sending the imaginary unit to the chosen signed axis `±i`, `±j`, or `±k`.
     *
     * A ring homomorphism only when the chosen axis squares to `-1`: always for the usual doubling,
     * and for the `i` axis alone in the split one.
     */
    fun complexEmbeddingMap(
        emb: AxisSignEmbeddings.AxisSignEmbedding
    ): UnaryOp<Complex<S>, Q> =
        UnaryOp { c ->
            QuaternionEmbeddingKit.embedComplexLike(
                axisSign = emb,
                re = c.re,
                im = c.im,
                zero = scalars.zero,
                negate = scalars.add.inverse,
                mkQuaternion = fromCoordinates
            )
        }

    /**
     * Coordinatewise equality on the public carrier.
     */
    fun eq(eqS: Eq<S>): Eq<Q> =
        CD.eq(CD.eq(eqS)).contramap { q: Q -> unwrap(q) }

    /**
     * The printable `w + xi + yj + zk`, dropping zero terms.
     */
    fun printable(
        signed: SignedOps<S>,
        prS: Printable<S>,
        eqS: Eq<S>
    ): Printable<Q> =
        QuaternionPrintable.quaternionPrintable(
            signed = signed,
            zero = scalars.zero,
            one = scalars.one,
            prA = prS,
            eqA = eqS,
            decompose = coordinates
        )
}
