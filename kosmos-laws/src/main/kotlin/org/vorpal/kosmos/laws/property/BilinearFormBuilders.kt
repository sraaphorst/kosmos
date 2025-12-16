package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BilinearForm
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

fun <F : Any, V : Any> bilinearAddLeftLaw(
    form: BilinearForm<V, F>,
    addV: BinOp<V>,
    addF: BinOp<F>,
    arbV: Arb<V>,
    eqF: Eq<F> = Eq.default(),
    prV: Printable<V> = Printable.default(),
    prF: Printable<F> = Printable.default(),
    label: String = "bilinear: additivity in left argument"
): TestingLaw =
    TestingLaw.named(label) {
        checkAll(Arb.pair(arbV, arbV), arbV) { (v1, v2), w ->
            val left = form(addV(v1, v2), w)
            val right = addF(form(v1, w), form(v2, w))

            withClue(buildString {
                appendLine("Additivity (left) failed:")
                appendLine("B(v1 + v2, w) vs B(v1,w) + B(v2,w)")
                appendLine("v1 = ${prV(v1)}")
                appendLine("v2 = ${prV(v2)}")
                appendLine("w  = ${prV(w)}")
                appendLine("LHS = ${prF(left)}")
                appendLine("RHS = ${prF(right)}")
            }) {
                check(eqF(left, right))
            }
        }
    }

fun <F : Any, V : Any> bilinearScalarLeftLaw(
    form: BilinearForm<V, F>,
    act: LeftAction<F, V>,
    mulF: BinOp<F>,
    arbF: Arb<F>,
    arbV: Arb<V>,
    eqF: Eq<F> = Eq.default(),
    prV: Printable<V> = Printable.default(),
    prF: Printable<F> = Printable.default(),
    label: String = "bilinear: homogeneity in left argument"
): TestingLaw =
    TestingLaw.named(label) {
        checkAll(arbF, arbV, arbV) { r, v, w ->
            val left = form(act(r, v), w)
            val right = mulF(r, form(v, w))

            withClue(buildString {
                appendLine("Homogeneity (left) failed:")
                appendLine("B(r·v, w) vs r * B(v,w)")
                appendLine("r  = ${prF(r)}")
                appendLine("v  = ${prV(v)}")
                appendLine("w  = ${prV(w)}")
                appendLine("LHS = ${prF(left)}")
                appendLine("RHS = ${prF(right)}")
            }) {
                check(eqF(left, right))
            }
        }
    }

fun <F : Any, V : Any> bilinearAddRightLaw(
    form: BilinearForm<V, F>,
    addV: BinOp<V>,
    addF: BinOp<F>,
    arbV: Arb<V>,
    eqF: Eq<F> = Eq.default(),
    prV: Printable<V> = Printable.default(),
    prF: Printable<F> = Printable.default(),
    label: String = "bilinear: additivity in right argument"
): TestingLaw =
    TestingLaw.named(label) {
        checkAll(arbV, Arb.pair(arbV, arbV)) { v, (w1, w2) ->
            val left = form(v, addV(w1, w2))
            val right = addF(form(v, w1), form(v, w2))

            withClue(buildString {
                appendLine("Additivity (right) failed:")
                appendLine("B(v, w1 + w2) vs B(v,w1) + B(v,w2)")
                appendLine("v  = ${prV(v)}")
                appendLine("w1 = ${prV(w1)}")
                appendLine("w2 = ${prV(w2)}")
                appendLine("LHS = ${prF(left)}")
                appendLine("RHS = ${prF(right)}")
            }) {
                check(eqF(left, right))
            }
        }
    }

fun <F : Any, V : Any> bilinearScalarRightLaw(
    form: BilinearForm<V, F>,
    act: LeftAction<F, V>,
    mulF: BinOp<F>,
    arbF: Arb<F>,
    arbV: Arb<V>,
    eqF: Eq<F> = Eq.default(),
    prV: Printable<V> = Printable.default(),
    prF: Printable<F> = Printable.default(),
    label: String = "bilinear: homogeneity in right argument"
): TestingLaw =
    TestingLaw.named(label) {
        checkAll(arbF, arbV, arbV) { r, v, w ->
            val left = form(v, act(r, w))
            val right = mulF(r, form(v, w))

            withClue(buildString {
                appendLine("Homogeneity (right) failed:")
                appendLine("B(v, r·w) vs r * B(v,w)")
                appendLine("r  = ${prF(r)}")
                appendLine("v  = ${prV(v)}")
                appendLine("w  = ${prV(w)}")
                appendLine("LHS = ${prF(left)}")
                appendLine("RHS = ${prF(right)}")
            }) {
                check(eqF(left, right))
            }
        }
    }