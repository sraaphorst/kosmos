package org.vorpal.kosmos.laws.module

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.util.midfix

fun <R : Any, M : Any> actionDistributesOverAdditionLaw(
    addM: BinOp<M>,
    act: Action<R, M>,
    arbR: Arb<R>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    label: String = "module: r${act.symbol.midfix()}(x${addM.symbol.midfix()}y) = " +
        "r${act.symbol.midfix()}x${addM.symbol.midfix()}r${act.symbol.midfix()}y"
): TestingLaw =
    TestingLaw.named(label) {
        val pairM = TestingLaw.arbPair(arbM)

        checkAll(arbR, pairM) { r, (x, y) ->
            val left = act(r, addM(x, y))
            val right = addM(act(r, x), act(r, y))

            withClue(
                buildString {
                    val sr = prR(r)
                    val sx = prM(x)
                    val sy = prM(y)
                    appendLine("Scalar distributivity over vector addition failed:")
                    appendLine("${act.symbol}($sr, $sx ${addM.symbol} $sy) = ${prM(left)}")
                    appendLine("${act.symbol}($sr, $sx) ${addM.symbol} ${act.symbol}($sr, $sy) = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }

fun <R : Any, M : Any> additionDistributesOverActionLaw(
    addR: BinOp<R>,
    addM: BinOp<M>,
    act: Action<R, M>,
    arbR: Arb<R>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    label: String = "module: (r+s)·x=r·x+s·x"
): TestingLaw =
    TestingLaw.named(label) {
        val pairR = TestingLaw.arbPair(arbR)

        checkAll(pairR, arbM) { (r, s), x ->
            val left = act(addR(r, s), x)
            val right = addM(act(r, x), act(s, x))

            withClue(
                buildString {
                    val sr = prR(r)
                    val ss = prR(s)
                    val sx = prM(x)
                    appendLine("Scalar addition distributivity failed:")
                    appendLine("${act.symbol}($sr ${addR.symbol} $ss, $sx) = ${prM(left)}")
                    appendLine("${act.symbol}($sr, $sx) ${addM.symbol} ${act.symbol}($ss, $sx) = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }

fun <R : Any, M : Any> actionAssociatesWithScalarMultiplicationLaw(
    mulR: BinOp<R>,
    act: Action<R, M>,
    arbR: Arb<R>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    label: String = "module: (rs)·x=r·(s·x)"
): TestingLaw =
    TestingLaw.named(label) {
        val pairR = TestingLaw.arbPair(arbR)

        checkAll(pairR, arbM) { (r, s), x ->
            val left = act(mulR(r, s), x)
            val right = act(r, act(s, x))

            withClue(
                buildString {
                    val sr = prR(r)
                    val ss = prR(s)
                    val sx = prM(x)
                    appendLine("Scalar multiplication associativity failed:")
                    appendLine("${act.symbol}($sr ${mulR.symbol} $ss, $sx) = ${prM(left)}")
                    appendLine("${act.symbol}($sr, ${act.symbol}($ss, $sx)) = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }

fun <R : Any, M : Any> unitActsAsIdentityLaw(
    oneR: R,
    act: Action<R, M>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    label: String = "module: 1·x=x"
): TestingLaw =
    TestingLaw.named(label) {
        checkAll(arbM) { x ->
            val result = act(oneR, x)

            withClue(
                buildString {
                    val sx = prM(x)
                    appendLine("Unit action failed:")
                    appendLine("${act.symbol}(${prR(oneR)}, $sx) = ${prM(result)}")
                    appendLine("Expected: $sx")
                }
            ) {
                check(eqM(result, x))
            }
        }
    }