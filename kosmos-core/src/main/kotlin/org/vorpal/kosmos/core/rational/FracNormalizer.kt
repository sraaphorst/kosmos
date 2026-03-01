package org.vorpal.kosmos.core.rational

/**
 * This interface defines a normalization strategy for rational fractions over a type A.
 * We use algebraic Carlström Wheel conventions to indicate the result of the normalization:
 * - If the numerator and denominator are zero, the result is considered to be the bottom element (0/0).
 * - If the denominator is zero and the numerator is not, the result is considered to be the infinity element (1/0).
 * - If the numerator is zero and the denominator is not, the result is considered to be the zero element (0/1).
 * - Else the normalized fraction, i.e. the fraction with the numerator and denominator reduced modulo their gcd
 *   is returned. If reducing the numerator or the denominator by their gcd results in a nonzero remainder,
 *   the operation is considered invalid and an exception may be thrown.
 *
 * EuclideanDomains are able to create a FracNormalizer that uses their gcd method.
 */
interface FracNormalizer<A : Any> {
    fun normalize(n: A, d: A): Pair<A, A>
}
