// ---- Vec2R.kt (replace the old data class) ----
package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.linear.values.Vec2

// 1) Specialize by aliasing the generic to Real.
typealias Vec2R = Vec2<Real>

// 2) A ctor-like factory so you can still write `Vec2R(…, …)`.
//fun Vec2R(x: Real, y: Real): Vec2R =
//    Vec2(x, y)

// 3) Zero convenient constant.
val Vec2R_ZERO: Vec2R = Vec2(0.0, 0.0)
