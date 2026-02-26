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
 * Wheels are therefore not simply commutative rings with an inverse: they are a different equational
 * theory designed so that division/inversion is total while retaining information about invalid
 * expressions rather than erasing it.
 */
interface Wheel<A : Any> {
    val add: AbelianGroup<A>
    val mul: CommutativeMonoid<A>
    val inv: Endo<A>

    val zero: A
    val one: A
    val bottom: A
}

/**
 * A [WheelInf] is a wheel (a total-division algebra) that *distinguishes infinities* in addition to nullity.
 *
 * # Intuition
 * This is the "fraction model with a memory for explosions":
 *
 * - Ordinary values behave like fractions.
 * - Division / inversion is *total*: `inv(x)` is defined for every `x`.
 * - There is a distinguished **bottom / nullity** element (typically corresponding to `0/0`) that represents
 *   an undefined result and tends to propagate through operations.
 * - In addition, the structure contains two **infinite-like** elements:
 *   - [posInf] (typically `1/0`)
 *   - [negInf] (typically `-1/0`)
 *
 * Unlike a meadow, a wheel does *not* totalize division by forcing `inv(0) = 0`. Instead, it keeps track of
 * what went wrong:
 *
 * - `inv(0)` is typically [posInf] (since `inv(0/1) = 1/0` in the fraction picture).
 * - `inv(posInf)` is typically `0`.
 * - `posInf + negInf` is typically **bottom** (think: `1/0 + (-1)/0 = 0/0`).
 * - `0 * posInf` may be **bottom** (so the ring identity `0 * x = 0` is not valid globally).
 *
 * # Why this is not a CommutativeRing
 * Wheels intentionally allow some familiar ring identities to fail in the presence of infinities/nullity.
 * In particular, multiplication is still a commutative monoid, and addition is still an abelian group,
 * but distributivity / annihilation-by-zero behavior may not hold universally once infinities and nullity
 * participate.
 *
 * # Typical model
 * A common concrete model is "fractions with zero denominators allowed":
 *
 * - finite values: `n/d` with `d != 0` (reduced)
 * - infinities: `±1/0`
 * - bottom/nullity: `0/0`
 *
 * with `inv(n/d) = d/n` and bottom propagation.
 */
interface WheelInf<A : Any> : Wheel<A> {
    val posInf: A
    val negInf: A
}
