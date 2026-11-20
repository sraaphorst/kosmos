package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

/**
 * Given a binary operation `op` and an inverse `inv`, the restricted inverse law states that for all `x`:
 * ```kotlin
 * op(x, op(x, inv(x))) = x
 * ```
 */
class RestrictedInverseLaw<A : Any>(
    val op: BinOp<A>,
    val inv: Endo<A>,
    val arb: Arb<A>,
    val eq: Eq<A> = Eq.default(),
    val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val name = "restricted inverse law (RIL)"

    override suspend fun test() {
        lawCheckAll(arb, render = renderWith(pr)) { x ->
            val xInv = inv(x)
            val xxInv = op(x, xInv)
            val xxxInv = op(x, xxInv)

            lawCheck(eq(xxxInv, x)) {
                buildString {
                    val sx = pr(x)
                    val sxInv = pr(xInv)
                    val sxxInv = pr(xxInv)
                    val sxxxInv = pr(xxxInv)

                    appendLine("Restricted inverse law failed:")
                    appendLine("x * (x * inv(x)) = $sx * ($sx * $sxInv) = $sx * $sxxInv = $sxxxInv (expected $sx)")
                }
            }
        }
    }
}
