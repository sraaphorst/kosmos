package org.vorpal.kosmos.functional.core

/**
 * Marker interface for a higher-kinded value of the form F<A>.
 *
 * Kotlin lacks native HKTs, so we encode them with a phantom type `F`
 * (a *tag* like `ForOption`) and the element type `A`.
 *
 * Example:
 *   - `Option<out A>` implements `Kind<ForOption, A>`
 *   - functions that are generic over "any F" accept `Kind<F, A>`
 */
interface Kind<F, out A>

/**
 * Marker interface for a higher-kinded value of the form F<A, B>.
 *
 * Kotlin lacks native HKTs, so we encode them with a phantom type `F`
 * (a *tag* like `ForEither`) and the element types `A` and `B`.
 *
 * Example:
 *   - `Either<out A, out B>` implements `Kind2<ForEither, A, B>`
 *   - functions that are generic over "any F" accept `Kind2<F, A, B>`
 */
interface Kind2<F, out A, out B>
