package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.algebra.structures.CommutativeRng
import org.vorpal.kosmos.linear.values.Vec3

/**
 * Operations specific to the fixed-size vectors [org.vorpal.kosmos.linear.values.Vec0] through
 * [org.vorpal.kosmos.linear.values.Vec4].
 *
 * Operations that make sense in every dimension (dot products, norms, scaling, and so on) are not
 * repeated here: the fixed-size vectors are [org.vorpal.kosmos.linear.values.VecLike], so use
 * [DenseVecOps] for those. Products of [org.vorpal.kosmos.linear.values.Vec2] with
 * [org.vorpal.kosmos.linear.values.Mat2] are in [FixedMatOps].
 */
object FixedVecOps {
    /**
     * The three-dimensional cross product over a commutative rng:
     * ```text
     * v × w = (v₂w₃ − v₃w₂,  v₃w₁ − v₁w₃,  v₁w₂ − v₂w₁).
     * ```
     * It is bilinear and antisymmetric, `v × w = −(w × v)`, and satisfies the Jacobi identity, so
     * `(R³, ×)` is a Lie algebra. Commutativity of [rng] is what makes `v × v = 0` and
     * `v · (v × w) = 0`: each coordinate of `v × v` is `vᵢvⱼ − vⱼvᵢ`.
     *
     * Over ℝ, a bilinear product with `|v × w|² = |v|²|w|² − (v · w)²` exists only in dimensions 3
     * and 7. Only the three-dimensional one is provided here.
     */
    fun <R : Any> cross(
        rng: CommutativeRng<R>,
        v: Vec3<R>,
        w: Vec3<R>
    ): Vec3<R> =
        rng.run {
            Vec3(
                add(mul(v.y, w.z), add.inverse(mul(v.z, w.y))),
                add(mul(v.z, w.x), add.inverse(mul(v.x, w.z))),
                add(mul(v.x, w.y), add.inverse(mul(v.y, w.x))),
            )
        }
}
