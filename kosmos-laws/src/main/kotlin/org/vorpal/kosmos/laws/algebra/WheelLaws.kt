package org.vorpal.kosmos.org.vorpal.kosmos.laws.algebra

import org.vorpal.kosmos.algebra.structures.WheelInf

/**
 * [WheelInf] law suite (this instance has ⊥ and ±∞):
 *
 * Structure laws:
 * - Abelian group laws on add (for non-bottom arithmetic; ⊥ is handled by extra laws below)
 * - Commutative monoid laws on mul
 * - Involution law on `inv`: `inv(inv(x)) = x`
 *
 * Bottom / nullity behavior:
 * - `inv(⊥) = ⊥`
 * - `⊥` is absorbing for both operations (tested via [AnnihilationLaw]):
 *   `⊥ + x = ⊥` and `x + ⊥ = ⊥`
 *   `⊥ * x = ⊥` and `x * ⊥ = ⊥`
 *
 * Infinity behavior (WheelZ policy: infinities are ±1/0, bottom is 0/0):
 * - `inv(0) = +∞` (since `inv(0/1) = 1/0)
 * - `inv(+∞) = 0`, and `inv(-∞) = 0`
 * - `(+∞) + (+∞) = +∞`, and `(-∞) + (-∞) = -∞`
 * - `(+∞) + (-∞) = ⊥` (equivalently: `(+∞) - (+∞)` = ⊥ and `(-∞) - (-∞) = ⊥`)
 * - `0 * (+∞) = ⊥` and `0 * (-∞) = ⊥`
 *
 * Notes:
 * - Wheels are ring-like but not rings: identities such as `0 * x = 0` do not hold globally once
 *   infinities / nullity are present.
 *   [oai_citation:2‡Cambridge University Press & Assessment](https://www.cambridge.org/core/journals/mathematical-structures-in-computer-science/article/wheels-on-division-by-zero/183248B486FBFAF27E8AE3EE1EEA4717?utm_source=chatgpt.com)
 * - The “annihilation” laws here are really “absorbing element” laws; wheels use a designated
 *   nullity element (often `0/0`) that propagates through operations.
 *   [oai_citation:3‡Mathematics Stack Exchange](https://math.stackexchange.com/questions/3003703/what-are-the-mathematical-properties-of-%E2%8A%A5-in-wheel-theory?utm_source=chatgpt.com)
 */
class WheelLaws<A : Any> {
    val wheel: WheelInf<A>,
}