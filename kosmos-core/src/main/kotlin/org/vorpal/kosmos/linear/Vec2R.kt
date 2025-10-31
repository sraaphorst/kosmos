// ---- Vec2R.kt (replace the old data class) ----
package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField

// 1) Specialize by aliasing the generic to Double.
typealias Vec2R = Vec2<Double>

// 2) A ctor-like factory so you can still write `Vec2R(…, …)`.
fun Vec2R(x: Double, y: Double): Vec2R =
    Vec2(x, y, RealField)

// 3) Zero convenient constant.
val Vec2R_ZERO: Vec2R = Vec2(0.0, 0.0, RealField)