package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo


/**
 * A [Wheel] is a total-division algebra intended to model division by zero without collapsing it to 0.
 *
 * Conceptually: instead of forcing `inv(0) = 0` (as in a meadow), a wheel introduces a distinguished
 * nullity / bottom value (corresponding to `0/0`) and allows "infinite-like" values (e.g. 1/0).
 * Operations propagate nullity, and some ring identities are not valid globally; in particular,
 * `0 * x` need not equal 0 (e.g. `0 * (1/0)` can become nullity).
 *
 * Wheels are therefore not simply commutative rings with inverse: they are a different equational
 * theory designed so that division/inversion is total while retaining information about invalid
 * expressions rather than erasing it.
 *
 * Additionally, an infinite-like value exists (1/0), but is not specifically named in the algebra:
 * hence, the main focus is on the [CarlstromWheel] model below.
 */
interface Wheel<A : Any> {
    val add: CommutativeMonoid<A>
    val mul: CommutativeMonoid<A>
    val inv: Endo<A>

    val zero: A
    val one: A
    val bottom: A
}

/**
 * A [CarlstromWheel] (named after Carlström) is a wheel (a total-division algebra) that
 * *distinguishes infinity* in addition to nullity.
 *
 * # Intuition
 * This is the "fraction model with a memory for explosions":
 *
 * - Ordinary values behave like fractions.
 * - Division / inversion is *total*: `inv(x)` is defined for every `x`.
 * - There is a distinguished **bottom / nullity** element (typically corresponding to `0/0`) that represents
 *   an undefined result and tends to propagate through operations.
 * - In addition, the structure contains an **infinite-like** element:
 *   - [inf] (typically `1/0`)
 *
 * Unlike a meadow, a wheel does *not* totalize division by forcing `inv(0) = 0`. Instead, it keeps track of
 * what went wrong:
 *
 * - `inv(0)` is typically [inf] (since `inv(0/1) = 1/0` in the fraction picture).
 * - `inv(inf)` is typically `0`.
 * - `inf + inf`, `inf - inf`, `0 * inf`, `inf * 0`, and `inf / inf` are typically **bottom**.
 * - We cannot specifically test `inf - inf` in the wheel test suite since we do not have an additive
 *   inverse / subtraction operation.
 * - `inf * inf = inf`
 * - These are not compatible with ring identity laws, which is why the [Wheel] is not a ring.
 *
 * # Why this is not a CommutativeRing
 * Wheels intentionally allow some familiar ring identities to fail in the presence of infinity / nullity.
 * In particular, addition and multiplication are still commutative monoids,
 * but distributivity / annihilation-by-zero behavior may not hold universally once infinity and nullity
 * participate.
 *
 * # Typical model
 * A common concrete model is "fractions with zero denominators allowed":
 *
 * - finite values: `n/d` with `d != 0` (reduced)
 * - infinity: `±1/0`
 * - bottom/nullity: `0/0`
 *
 * with `inv(n/d) = d/n` and bottom propagation.
 *
 * References:
 * - J. Carlström, “Wheels – on division by zero,” Math. Struct. Comput. Sci. 14(1):143–184, 2004. doi:10.1017/S0960129503004110.
 * - J. Carlström, “Wheels - On Division by Zero,” Research Reports in Mathematics 2001:11, Stockholm Univ., 2001 (PDF).
 * - https://en.wikipedia.org/wiki/Wheel_(mathematics)
 */
interface CarlstromWheel<A : Any> : Wheel<A> {
    val inf: A
}
