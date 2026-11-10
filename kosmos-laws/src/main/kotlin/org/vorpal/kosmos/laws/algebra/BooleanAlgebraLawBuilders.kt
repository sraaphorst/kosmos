package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

fun <A : Any> complementJoinTopLaw(
    join: BinOp<A>,
    not: Endo<A>,
    top: A,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "boolean complement: x ∨ ¬x = ⊤",
): TestingLaw = TestingLaw.named(label) {
    lawCheckAll(arb, render = renderWith(pr)) { x ->
        val left = join(x, not(x))

        lawCheck(eq(left, top)) {
            buildString {
            appendLine("Complement (join) failed:")
            appendLine("x = ${pr(x)}")
            appendLine("x ${join.symbol} ${not.symbol}x = ${pr(left)}")
            appendLine("⊤ = ${pr(top)}")
            }
        }
    }
}

fun <A : Any> complementMeetBottomLaw(
    meet: BinOp<A>,
    not: Endo<A>,
    bottom: A,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "boolean complement: x ∧ ¬x = ⊥",
): TestingLaw = TestingLaw.named(label) {
    lawCheckAll(arb, render = renderWith(pr)) { x ->
        val left = meet(x, not(x))

        lawCheck(eq(left, bottom)) {
            buildString {
            appendLine("Complement (meet) failed:")
            appendLine("x = ${pr(x)}")
            appendLine("x ${meet.symbol} ${not.symbol}x = ${pr(left)}")
            appendLine("⊥ = ${pr(bottom)}")
            }
        }
    }
}

fun <A : Any> notBottomIsTopLaw(
    not: Endo<A>,
    bottom: A,
    top: A,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "boolean: ¬⊥ = ⊤",
): TestingLaw = TestingLaw.named(label) {
    val value = not(bottom)

    lawCheck(eq(value, top)) {
        buildString {
        appendLine("Negation of bottom failed:")
        appendLine("⊥ = ${pr(bottom)}")
        appendLine("¬⊥ = ${pr(value)}")
        appendLine("⊤ = ${pr(top)}")
        }
    }
}

fun <A : Any> notTopIsBottomLaw(
    not: Endo<A>,
    bottom: A,
    top: A,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "boolean: ¬⊤ = ⊥",
): TestingLaw = TestingLaw.named(label) {
    val value = not(top)

    lawCheck(eq(value, bottom)) {
        buildString {
        appendLine("Negation of top failed:")
        appendLine("⊤ = ${pr(top)}")
        appendLine("¬⊤ = ${pr(value)}")
        appendLine("⊥ = ${pr(bottom)}")
        }
    }
}

fun <A : Any> deMorganJoinLaw(
    join: BinOp<A>,
    meet: BinOp<A>,
    not: Endo<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "de Morgan: ¬(x ∨ y) = ¬x ∧ ¬y",
): TestingLaw = TestingLaw.named(label) {
    lawCheckAll(arb, arb) { x, y ->
        val left = not(join(x, y))
        val right = meet(not(x), not(y))

        lawCheck(eq(left, right)) {
            buildString {
            appendLine("De Morgan (join) failed:")
            appendLine("x = ${pr(x)}")
            appendLine("y = ${pr(y)}")
            appendLine("LHS: ¬(x ${join.symbol} y) = ${pr(left)}")
            appendLine("RHS: (¬x ${meet.symbol} ¬y) = ${pr(right)}")
            }
        }
    }
}

fun <A : Any> deMorganMeetLaw(
    join: BinOp<A>,
    meet: BinOp<A>,
    not: Endo<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default(),
    label: String = "de Morgan: ¬(x ∧ y) = ¬x ∨ ¬y",
): TestingLaw = TestingLaw.named(label) {
    lawCheckAll(arb, arb) { x, y ->
        val left = not(meet(x, y))
        val right = join(not(x), not(y))

        lawCheck(eq(left, right)) {
            buildString {
            appendLine("De Morgan (meet) failed:")
            appendLine("x = ${pr(x)}")
            appendLine("y = ${pr(y)}")
            appendLine("LHS: ¬(x ${meet.symbol} y) = ${pr(left)}")
            appendLine("RHS: (¬x ${join.symbol} ¬y) = ${pr(right)}")
            }
        }
    }
}