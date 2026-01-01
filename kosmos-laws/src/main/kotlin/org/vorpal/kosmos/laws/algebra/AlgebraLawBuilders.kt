package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Algebra axiom (left homogeneity of multiplication):
 *
 *    (r ⊳ a) * b = r ⊳ (a * b)
 */
fun <R : Any, A : Any> leftScalarHomogeneityOfMulLaw(
    act: LeftAction<R, A>,
    mulA: BinOp<A>,
    arbR: Arb<R>,
    arbA: Arb<A>,
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
    label: String = "algebra: (r ⊳ a) * b = r ⊳ (a * b)"
): TestingLaw = TestingLaw.named(label) {
    checkAll(Arb.triple(arbR, arbA, arbA)) { (r, a, b) ->
        val left = mulA(act(r, a), b)
        val right = act(r, mulA(a, b))

        withClue(
            buildString {
                appendLine("Left scalar homogeneity of multiplication failed:")
                appendLine("r = ${prR(r)}")
                appendLine("a = ${prA(a)}")
                appendLine("b = ${prA(b)}")
                appendLine("LHS: (r ${act.symbol} a) ${mulA.symbol} b = ${prA(left)}")
                appendLine("RHS: r ${act.symbol} (a ${mulA.symbol} b) = ${prA(right)}")
            }
        ) {
            check(eqA(left, right))
        }
    }
}

/**
 * Algebra axiom (right homogeneity of multiplication):
 *
 *    a * (r ⊳ b) = r ⊳ (a * b)
 */
fun <R : Any, A : Any> rightScalarHomogeneityOfMulLaw(
    act: LeftAction<R, A>,
    mulA: BinOp<A>,
    arbR: Arb<R>,
    arbA: Arb<A>,
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
    label: String = "algebra: a * (r ⊳ b) = r ⊳ (a * b)"
): TestingLaw = TestingLaw.named(label) {
    checkAll(Arb.triple(arbR, arbA, arbA)) { (r, a, b) ->
        val left = mulA(a, act(r, b))
        val right = act(r, mulA(a, b))

        withClue(
            buildString {
                appendLine("Right scalar homogeneity of multiplication failed:")
                appendLine("r = ${prR(r)}")
                appendLine("a = ${prA(a)}")
                appendLine("b = ${prA(b)}")
                appendLine("LHS: a ${mulA.symbol} (r ${act.symbol} b) = ${prA(left)}")
                appendLine("RHS: r ${act.symbol} (a ${mulA.symbol} b) = ${prA(right)}")
            }
        ) {
            check(eqA(left, right))
        }
    }
}

/**
 * Algebra axioms:
 *
 *    (r ⊳ a) * b = r ⊳ (a * b)
 *    a * (r ⊳ b) = r ⊳ (a * b)
 */
fun <R : Any, A : Any> algebraMulBilinearityLaws(
    act: LeftAction<R, A>,
    mulA: BinOp<A>,
    arbR: Arb<R>,
    arbA: Arb<A>,
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
): List<TestingLaw> = listOf(
    leftScalarHomogeneityOfMulLaw(act, mulA, arbR, arbA, eqA, prR, prA),
    rightScalarHomogeneityOfMulLaw(act, mulA, arbR, arbA, eqA, prR, prA),
)
