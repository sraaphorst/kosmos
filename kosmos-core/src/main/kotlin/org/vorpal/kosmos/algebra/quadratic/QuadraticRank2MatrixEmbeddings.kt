package org.vorpal.kosmos.algebra.quadratic

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * Given a rank-2 `R`-basis `{1, w}` with quadratic relation
 * ```text
 * w^2 = s + tw,
 * ```
 * construct the corresponding matrix embedding into `M_2(R)`.
 *
 * For an element written as
 * ```text
 * a * 1 + b * w = a + bw
 * ```
 * the associated matrix is
 * ```text
 * [a       sb]
 * [b   a + tb]
 * ```
 * whose columns are the coordinates of `(a + bw) * 1` and `(a + bw) * w`
 * relative to the basis `{1, w}`.
 *
 * The [coeffs] function must extract the coefficient pair `(a, b)` from an
 * element of the domain.
 */
fun <A : Any, R : Any> quadraticRank2MatrixEmbedding(
    domain: Ring<A>,
    coefficientRing: CommutativeRing<R>,
    s: R,
    t: R,
    coeffs: (A) -> Pair<R, R>
): RingMonomorphism<A, DenseMat<R>> =
    RingMonomorphism.of(
        domain = domain,
        codomain = DenseMatAlgebras.DenseMatRing(coefficientRing, 2),
        map = UnaryOp { z ->
            val (a, b) = coeffs(z)
            DenseMat.ofRows(
                listOf(
                    listOf(a, coefficientRing.mul(s, b)),
                    listOf(b, coefficientRing.add(a, coefficientRing.mul(t, b)))
                )
            )
        }
    )
