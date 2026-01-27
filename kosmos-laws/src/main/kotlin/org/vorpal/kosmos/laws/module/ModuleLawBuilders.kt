package org.vorpal.kosmos.laws.module

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.LeftRModule
import org.vorpal.kosmos.algebra.structures.RightRModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.RightAction
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Left module axiom:
 *
 *   r ⊳ (x + y) = (r ⊳ x) + (r ⊳ y)
 */
fun <R : Any, M : Any> leftActionDistributesOverAdditionLaw(
    act: LeftAction<R, M>,
    addM: BinOp<M>,
    arbR: Arb<R>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    label: String = "module: scalar distributes over addition"
): TestingLaw {
    val pairM = TestingLaw.arbPair(arbM)

    return TestingLaw.named(label) {
        checkAll(arbR, pairM) { r, (x, y) ->
            val left = act(r, addM(x, y))
            val right = addM(act(r, x), act(r, y))

            withClue(
                buildString {
                    val sr = prR(r)
                    val sx = prM(x)
                    val sy = prM(y)
                    appendLine("Left module distributivity failed:")
                    appendLine("${act.symbol} = scalar action, ${addM.symbol} = addition")
                    appendLine("r = $sr")
                    appendLine("x = $sx")
                    appendLine("y = $sy")
                    appendLine("LHS: r ${act.symbol} (x ${addM.symbol} y) = ${prM(left)}")
                    appendLine("RHS: (r ${act.symbol} x) ${addM.symbol} (r ${act.symbol} y) = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }
}

/**
 * Left module axiom:
 *
 *   (r + s) ⊳ x = (r ⊳ x) + (s ⊳ x)
 */
fun <R : Any, M : Any> leftActionRespectsScalarAdditionLaw(
    addR: BinOp<R>,
    act: LeftAction<R, M>,
    addM: BinOp<M>,
    arbR: Arb<R>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    label: String = "module: action respects scalar addition"
): TestingLaw {
    val pairR = TestingLaw.arbPair(arbR)

    return TestingLaw.named(label) {
        checkAll(pairR, arbM) { (r, s), x ->
            val left = act(addR(r, s), x)
            val right = addM(act(r, x), act(s, x))

            withClue(
                buildString {
                    val sr = prR(r)
                    val ss = prR(s)
                    val sx = prM(x)
                    appendLine("Left module scalar-additivity failed:")
                    appendLine("${addR.symbol} = scalar addition, ${act.symbol} = scalar action, ${addM.symbol} = addition")
                    appendLine("r = $sr")
                    appendLine("s = $ss")
                    appendLine("x = $sx")
                    appendLine("LHS: (r ${addR.symbol} s) ${act.symbol} x = ${prM(left)}")
                    appendLine("RHS: (r ${act.symbol} x) ${addM.symbol} (s ${act.symbol} x) = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }
}

/**
 * Left module axiom:
 *
 *   (r * s) ⊳ x = r ⊳ (s ⊳ x)
 */
fun <R : Any, M : Any> leftActionAssociatesWithScalarMultiplicationLaw(
    mulR: BinOp<R>,
    act: LeftAction<R, M>,
    arbR: Arb<R>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    label: String = "module: action associates with scalar multiplication"
): TestingLaw {
    val pairR = TestingLaw.arbPair(arbR)

    return TestingLaw.named(label) {
        checkAll(pairR, arbM) { (r, s), x ->
            val left = act(mulR(r, s), x)
            val right = act(r, act(s, x))

            withClue(
                buildString {
                    val sr = prR(r)
                    val ss = prR(s)
                    val sx = prM(x)
                    appendLine("Left module associativity failed:")
                    appendLine("${mulR.symbol} = scalar multiplication, ${act.symbol} = scalar action")
                    appendLine("r = $sr")
                    appendLine("s = $ss")
                    appendLine("x = $sx")
                    appendLine("LHS: (r ${mulR.symbol} s) ${act.symbol} x = ${prM(left)}")
                    appendLine("RHS: r ${act.symbol} (s ${act.symbol} x) = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }
}

/**
 * Left module axiom:
 *
 *   1 ⊳ x = x
 */
fun <R : Any, M : Any> leftUnitActsAsIdentityLaw(
    oneR: R,
    act: LeftAction<R, M>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    label: String = "module: scalar unit acts as identity"
): TestingLaw =
    TestingLaw.named(label) {
        checkAll(arbM) { x ->
            val left = act(oneR, x)

            withClue(
                buildString {
                    val s1 = prR(oneR)
                    val sx = prM(x)
                    appendLine("Left module unit law failed:")
                    appendLine("${act.symbol} = scalar action")
                    appendLine("1 = $s1")
                    appendLine("x = $sx")
                    appendLine("LHS: 1 ${act.symbol} x = ${prM(left)}")
                    appendLine("RHS: x = $sx")
                }
            ) {
                check(eqM(left, x))
            }
        }
    }

/**
 * Right module axiom:
 *
 *   (x + y) ⊲ r = (x ⊲ r) + (y ⊲ r)
 */
fun <M : Any, R : Any> rightActionDistributesOverAdditionLaw(
    act: RightAction<M, R>,
    addM: BinOp<M>,
    arbM: Arb<M>,
    arbR: Arb<R>,
    eqM: Eq<M> = Eq.default(),
    prM: Printable<M> = Printable.default(),
    prR: Printable<R> = Printable.default(),
    label: String = "module: addition distributes over scalar action"
): TestingLaw {
    val pairM = TestingLaw.arbPair(arbM)

    return TestingLaw.named(label) {
        checkAll(pairM, arbR) { (x, y), r ->
            val left = act(addM(x, y), r)
            val right = addM(act(x, r), act(y, r))

            withClue(
                buildString {
                    val sx = prM(x)
                    val sy = prM(y)
                    val sr = prR(r)
                    appendLine("Right module distributivity failed:")
                    appendLine("${act.symbol} = scalar action, ${addM.symbol} = addition")
                    appendLine("x = $sx")
                    appendLine("y = $sy")
                    appendLine("r = $sr")
                    appendLine("LHS: (x ${addM.symbol} y) ${act.symbol} r = ${prM(left)}")
                    appendLine("RHS: (x ${act.symbol} r) ${addM.symbol} (y ${act.symbol} r) = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }
}

/**
 * Right module axiom:
 *
 *   x ⊲ (r + s) = (x ⊲ r) + (x ⊲ s)
 */
fun <M : Any, R : Any> rightActionRespectsScalarAdditionLaw(
    addR: BinOp<R>,
    act: RightAction<M, R>,
    addM: BinOp<M>,
    arbM: Arb<M>,
    arbR: Arb<R>,
    eqM: Eq<M> = Eq.default(),
    prM: Printable<M> = Printable.default(),
    prR: Printable<R> = Printable.default(),
    label: String = "module: action respects scalar addition"
): TestingLaw {
    val pairR = TestingLaw.arbPair(arbR)

    return TestingLaw.named(label) {
        checkAll(arbM, pairR) { x, (r, s) ->
            val left = act(x, addR(r, s))
            val right = addM(act(x, r), act(x, s))

            withClue(
                buildString {
                    val sx = prM(x)
                    val sr = prR(r)
                    val ss = prR(s)
                    appendLine("Right module scalar-additivity failed:")
                    appendLine("${addR.symbol} = scalar addition, ${act.symbol} = scalar action, ${addM.symbol} = addition")
                    appendLine("x = $sx")
                    appendLine("r = $sr")
                    appendLine("s = $ss")
                    appendLine("LHS: x ${act.symbol} (r ${addR.symbol} s) = ${prM(left)}")
                    appendLine("RHS: (x ${act.symbol} r) ${addM.symbol} (x ${act.symbol} s) = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }
}

/**
 * Right module axiom:
 *
 *   x ⊲ (r * s) = (x ⊲ r) ⊲ s
 */
fun <M : Any, R : Any> rightActionAssociatesWithScalarMultiplicationLaw(
    mulR: BinOp<R>,
    act: RightAction<M, R>,
    arbM: Arb<M>,
    arbR: Arb<R>,
    eqM: Eq<M> = Eq.default(),
    prM: Printable<M> = Printable.default(),
    prR: Printable<R> = Printable.default(),
    label: String = "module: action associates with scalar multiplication"
): TestingLaw {
    val pairR = TestingLaw.arbPair(arbR)

    return TestingLaw.named(label) {
        checkAll(arbM, pairR) { x, (r, s) ->
            val left = act(x, mulR(r, s))
            val right = act(act(x, r), s)

            withClue(
                buildString {
                    val sx = prM(x)
                    val sr = prR(r)
                    val ss = prR(s)
                    appendLine("Right module associativity failed:")
                    appendLine("${mulR.symbol} = scalar multiplication, ${act.symbol} = scalar action")
                    appendLine("x = $sx")
                    appendLine("r = $sr")
                    appendLine("s = $ss")
                    appendLine("LHS: x ${act.symbol} (r ${mulR.symbol} s) = ${prM(left)}")
                    appendLine("RHS: (x ${act.symbol} r) ${act.symbol} s = ${prM(right)}")
                }
            ) {
                check(eqM(left, right))
            }
        }
    }
}

/**
 * Right module axiom:
 *
 *   x ⊲ 1 = x
 */
fun <M : Any, R : Any> rightUnitActsAsIdentityLaw(
    oneR: R,
    act: RightAction<M, R>,
    arbM: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prM: Printable<M> = Printable.default(),
    prR: Printable<R> = Printable.default(),
    label: String = "module: scalar unit acts as identity"
): TestingLaw =
    TestingLaw.named(label) {
        checkAll(arbM) { x ->
            val left = act(x, oneR)

            withClue(
                buildString {
                    val sx = prM(x)
                    val s1 = prR(oneR)
                    appendLine("Right module unit law failed:")
                    appendLine("${act.symbol} = scalar action")
                    appendLine("x = $sx")
                    appendLine("1 = $s1")
                    appendLine("LHS: x ${act.symbol} 1 = ${prM(left)}")
                    appendLine("RHS: x = $sx")
                }
            ) {
                check(eqM(left, x))
            }
        }
    }

/**
 * Bimodule compatibility.
 *
 *    (r ⊳ m) ⊲ s = r ⊳ (m ⊲ s)
 */
fun <R : Any, M : Any, S : Any> bimoduleCompatibilityLaw(
    leftAct: LeftAction<R, M>,
    rightAct: RightAction<M, S>,
    arbR: Arb<R>,
    arbM: Arb<M>,
    arbS: Arb<S>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    prS: Printable<S> = Printable.default()
): TestingLaw = TestingLaw.named("bimodule compatibility: (r ⊳ m) ⊲ s = r ⊳ (m ⊲ s)") {
    checkAll(arbR, arbM, arbS) { r, m, s ->
        val left = rightAct(leftAct(r, m), s)
        val right = leftAct(r, rightAct(m, s))
        check(eqM(left, right)) {
            "Compatibility failed:\n" +
                "(r ⊳ m) ⊲ s = ${prM(left)}\n" +
                "r ⊳ (m ⊲ s) = ${prM(right)}\n" +
                "r=${prR(r)}, m=${prM(m)}, s=${prS(s)}"
        }
    }
}

/**
 * Convenience function to build the [TestingLaw]s for a [LeftRModule].
 */
fun <R : Any, M : Any> leftRModuleLaws(
    module: LeftRModule<R, M>,
    scalarArb: Arb<R>,
    vectorArb: Arb<M>,
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default()
): List<TestingLaw> = listOf(
    leftActionDistributesOverAdditionLaw(
        addM = module.add.op,
        act = module.leftAction,
        arbR = scalarArb,
        arbM = vectorArb,
        eqM = eqM,
        prR = prR,
        prM = prM
    ),
    leftActionRespectsScalarAdditionLaw(
        addR = module.leftScalars.add.op,
        addM = module.add.op,
        act = module.leftAction,
        arbR = scalarArb,
        arbM = vectorArb,
        eqM = eqM,
        prR = prR,
        prM = prM
    ),
    leftActionAssociatesWithScalarMultiplicationLaw(
        mulR = module.leftScalars.mul.op,
        act = module.leftAction,
        arbR = scalarArb,
        arbM = vectorArb,
        eqM = eqM,
        prR = prR,
        prM = prM
    ),
    leftUnitActsAsIdentityLaw(
        oneR = module.leftScalars.mul.identity,
        act = module.leftAction,
        arbM = vectorArb,
        eqM = eqM,
        prR = prR,
        prM = prM
    )
)

/**
 * Convenience function to build the [TestingLaw]s for a [RightRModule].
 */
fun <M : Any, S : Any> rightRModuleLaws(
    module: RightRModule<M, S>,
    vectorArb: Arb<M>,
    scalarArb: Arb<S>,
    eqM: Eq<M> = Eq.default(),
    prM: Printable<M> = Printable.default(),
    prS: Printable<S> = Printable.default(),
): List<TestingLaw> = listOf(
    rightActionDistributesOverAdditionLaw(
        act = module.rightAction,
        addM = module.add.op,
        arbM = vectorArb,
        arbR = scalarArb,
        eqM = eqM,
        prM = prM,
        prR = prS
    ),
    rightActionRespectsScalarAdditionLaw(
        addR = module.rightScalars.add.op,
        act = module.rightAction,
        addM = module.add.op,
        arbM = vectorArb,
        arbR = scalarArb,
        eqM = eqM,
        prM = prM,
        prR = prS
    ),
    rightActionAssociatesWithScalarMultiplicationLaw(
        mulR = module.rightScalars.mul.op,
        act = module.rightAction,
        arbM = vectorArb,
        arbR = scalarArb,
        eqM = eqM,
        prM = prM,
        prR = prS
    ),
    rightUnitActsAsIdentityLaw(
        oneR = module.rightScalars.mul.identity,
        act = module.rightAction,
        arbM = vectorArb,
        eqM = eqM,
        prM = prM,
        prR = prS
    )
)
