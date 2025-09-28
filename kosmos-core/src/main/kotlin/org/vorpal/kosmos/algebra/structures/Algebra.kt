package org.vorpal.kosmos.algebra.structures

/**
 * An Algebra A over a commutative ring R is:
 *  - an R-module (scalar multiplication by R on A)
 *  - a ring (with its own multiplication on A)
 *  - such that multiplication in A is R-bilinear.
 *
 *  Bilinearity means linear in each argument separately, i.e. for r in R, and a, b, c in A:
 *
 *  Left linearity:
 *  - (ra)b = r(ab)
 *  - (a + b)c = ac + bc
 *
 *  Right linearity:
 *  - a(rb) = r(ab)
 *  - a(b + c) = ab + ac
 *
 *  For example, R[[x]] is an algebra over R.
 *  Another common example is M_n(R), the nxn matrices over R, which is an algebra over R:
 *  - Matrices can be scaled by elements of R
 *  - Matrices can be multiplied
 *  - The order doesn't matter: (rA)B = r(AB) = A(rB), i.e. bilinearity.
 *
 *  Another simple example: C is an algebra over R.
 */
interface Algebra<R, A> : RModule<R, A>, Ring<A> {
    override val ring: CommutativeRing<R>  // scalar ring must be commutative
}
