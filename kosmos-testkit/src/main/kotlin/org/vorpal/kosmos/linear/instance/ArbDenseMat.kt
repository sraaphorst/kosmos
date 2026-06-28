package org.vorpal.kosmos.linear.instance

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * Given an arbitrary and a size n, create an Arb<DenseMat<F>> to generate nxn matrices where all elements are the same.
 */
fun <F : Any> arbConstMat(
    arbF: Arb<F>,
    n: Int,
): Arb<DenseMat<F>> {
    require(n > 0) { "Matrix dimensions must be positive, got $n" }
    return arbF.map { f -> DenseMat.tabulate(n, n) { _, _ -> f} }
}

/**
 * Generate an arbitrary `rows×cols` [DenseMat] whose entries are drawn from [arbF].
 *
 * Both [rows] and [cols] may be zero, which yields an empty matrix of that shape.
 */
fun <F : Any> arbDenseMat(
    arbF: Arb<F>,
    rows: Int,
    cols: Int,
): Arb<DenseMat<F>> {
    require(rows >= 0) { "rows must be nonnegative, got $rows" }
    require(cols >= 0) { "cols must be nonnegative, got $cols" }
    val size = rows * cols
    return Arb.list(arbF, size..size).map { entries ->
        DenseMat.tabulate(rows, cols) { r, c -> entries[r * cols + c] }
    }
}

/**
 * Generate an arbitrary square `n×n` [DenseMat] whose entries are drawn from [arbF].
 */
fun <F : Any> arbSquareDenseMat(
    arbF: Arb<F>,
    n: Int,
): Arb<DenseMat<F>> = arbDenseMat(arbF, n, n)

/**
 * Generate an arbitrary square [DenseMat] whose order is uniformly chosen in `[minN, maxN]`
 * and whose entries are drawn from [arbF].
 *
 * This is handy for property tests over the determinant and other size-polymorphic operations.
 */
fun <F : Any> arbSquareDenseMatOfVaryingSize(
    arbF: Arb<F>,
    minN: Int,
    maxN: Int,
): Arb<DenseMat<F>> {
    require(minN >= 0) { "minN must be nonnegative, got $minN" }
    require(maxN >= minN) { "maxN must be >= minN, got minN=$minN, maxN=$maxN" }
    return Arb.int(minN..maxN).flatMap { n -> arbSquareDenseMat(arbF, n) }
}

/**
 * Generate a pair of square matrices of the *same* (uniformly chosen) order in `[minN, maxN]`,
 * each with entries drawn from [arbF].
 *
 * Useful for laws relating two equally-shaped matrices (e.g. `det(AB) = det(A)·det(B)`).
 */
fun <F : Any> arbSquareDenseMatPair(
    arbF: Arb<F>,
    minN: Int,
    maxN: Int,
): Arb<Pair<DenseMat<F>, DenseMat<F>>> {
    require(minN >= 0) { "minN must be nonnegative, got $minN" }
    require(maxN >= minN) { "maxN must be >= minN, got minN=$minN, maxN=$maxN" }
    return Arb.int(minN..maxN).flatMap { n ->
        arbSquareDenseMat(arbF, n).flatMap { a ->
            arbSquareDenseMat(arbF, n).map { b -> a to b }
        }
    }
}

/**
 * Generate a random rational matrix whose dimensions are each uniformly chosen in `[1, maxDim]`,
 * with entries drawn from [ArbRational.small].
 *
 * Convenience for property tests over [Rational]-valued matrices.
 */
fun arbRationalMat(maxDim: Int = 5): Arb<DenseMat<Rational>> {
    require(maxDim >= 1) { "maxDim must be >= 1, got $maxDim" }
    return Arb.bind(Arb.int(1..maxDim), Arb.int(1..maxDim)) { r, c -> r to c }
        .flatMap { (r, c) -> arbDenseMat(ArbRational.small, r, c) }
}

/**
 * Generate a random square rational matrix whose order is uniformly chosen in `[1, maxDim]`,
 * with entries drawn from [ArbRational.small].
 *
 * Convenience for property tests over square [Rational]-valued matrices (determinants, REF/RREF
 * of full-rank inputs, etc.).
 */
fun arbSquareRationalMat(maxDim: Int = 5): Arb<DenseMat<Rational>> {
    require(maxDim >= 1) { "maxDim must be >= 1, got $maxDim" }
    return Arb.int(1..maxDim).flatMap { n -> arbDenseMat(ArbRational.small, n, n) }
}
