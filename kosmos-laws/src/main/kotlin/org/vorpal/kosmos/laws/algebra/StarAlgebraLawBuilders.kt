package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Star-algebra axiom over a scalar ring R (no conjugation on scalars):
 *
 *    (r ⊳ a)* = r ⊳ (a*)
 */
fun <R : Any, A : Any> starCommutesWithScalarActionLaw(
    act: LeftAction<R, A>,
    star: Endo<A>,
    arbR: Arb<R>,
    arbA: Arb<A>,
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
    label: String = "star: (r ⊳ a)* = r ⊳ (a*)"
): TestingLaw = TestingLaw.named(label) {
    checkAll(arbR, arbA) { r, a ->
        val left = star(act(r, a))
        val right = act(r, star(a))

        withClue(
            buildString {
                appendLine("Star failed to commute with scalar action:")
                appendLine("r = ${prR(r)}")
                appendLine("a = ${prA(a)}")
                appendLine("LHS: ${star.symbol}(${prA(act(r, a))}) = ${prA(left)}")
                appendLine("RHS: r ${act.symbol} ${star.symbol}(${prA(a)}) = ${prA(right)}")
            }
        ) {
            check(eqA(left, right))
        }
    }
}