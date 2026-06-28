package org.vorpal.kosmos.linear.values

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.map

/**
 * Arbitraries for the fixed-arity tuple types [Vec1], [Vec2], [Vec3], [Vec4].
 *
 * Each [Vec`n`] generator is parameterized by the element [Arb], so the same
 * generator works regardless of the scalar field (rationals, reals, complex, etc.).
 *
 * [Vec0] needs no generator: it has a single value, so `Arb.constant(Vec0<A>())` suffices
 * at the call site.
 */

/** Generate a random [Vec1] with component drawn from [arbA]. */
fun <A : Any> arbVec1(arbA: Arb<A>): Arb<Vec1<A>> =
    arbA.map { Vec1(it) }

/** Generate a random [Vec2] with components drawn from [arbA]. */
fun <A : Any> arbVec2(arbA: Arb<A>): Arb<Vec2<A>> =
    Arb.bind(arbA, arbA) { x, y -> Vec2(x, y) }

/** Generate a random [Vec3] with components drawn from [arbA]. */
fun <A : Any> arbVec3(arbA: Arb<A>): Arb<Vec3<A>> =
    Arb.bind(arbA, arbA, arbA) { x, y, z -> Vec3(x, y, z) }

/** Generate a random [Vec4] with components drawn from [arbA]. */
fun <A : Any> arbVec4(arbA: Arb<A>): Arb<Vec4<A>> =
    Arb.bind(arbA, arbA, arbA, arbA) { x, y, z, w -> Vec4(x, y, z, w) }
