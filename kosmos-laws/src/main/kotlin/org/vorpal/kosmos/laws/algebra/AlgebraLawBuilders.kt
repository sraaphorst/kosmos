package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

// ---------------------------------------------------------------------------------------------
// Axioms (definition-level)
// ---------------------------------------------------------------------------------------------

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


// ---------------------------------------------------------------------------------------------
// Derived / diagnostic (unital; redundant but useful for debugging)
// ---------------------------------------------------------------------------------------------

/**
 * Derived law (unital): (r ⊳ 1) * a = r ⊳ a
 */
fun <R : Any, A : Any> scalarActsAsLeftMultiplicationLaw(
    act: LeftAction<R, A>,
    mulA: BinOp<A>,
    oneA: A,
    arbR: Arb<R>,
    arbA: Arb<A>,
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
    label: String = "derived: (r ⊳ 1) * a = r ⊳ a"
): TestingLaw = TestingLaw.named(label) {
    checkAll(Arb.pair(arbR, arbA)) { (r, a) ->
        val iota = act(r, oneA)
        val left = mulA(iota, a)
        val right = act(r, a)

        withClue(
            buildString {
                appendLine("Scalar acts as left multiplication failed:")
                appendLine("r = ${prR(r)}")
                appendLine("a = ${prA(a)}")
                appendLine("ι(r) = r ${act.symbol} 1 = ${prA(iota)}")
                appendLine("LHS: ι(r) ${mulA.symbol} a = ${prA(left)}")
                appendLine("RHS: r ${act.symbol} a = ${prA(right)}")
            }
        ) {
            check(eqA(left, right))
        }
    }
}

/**
 * Derived law (unital): a * (r ⊳ 1) = r ⊳ a
 */
fun <R : Any, A : Any> scalarActsAsRightMultiplicationLaw(
    act: LeftAction<R, A>,
    mulA: BinOp<A>,
    oneA: A,
    arbR: Arb<R>,
    arbA: Arb<A>,
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
    label: String = "derived: a * (r ⊳ 1) = r ⊳ a"
): TestingLaw = TestingLaw.named(label) {
    checkAll(Arb.pair(arbR, arbA)) { (r, a) ->
        val iota = act(r, oneA)
        val left = mulA(a, iota)
        val right = act(r, a)

        withClue(
            buildString {
                appendLine("Scalar acts as right multiplication failed:")
                appendLine("r = ${prR(r)}")
                appendLine("a = ${prA(a)}")
                appendLine("ι(r) = r ${act.symbol} 1 = ${prA(iota)}")
                appendLine("LHS: a ${mulA.symbol} ι(r) = ${prA(left)}")
                appendLine("RHS: r ${act.symbol} a = ${prA(right)}")
            }
        ) {
            check(eqA(left, right))
        }
    }
}

/**
 * Derived law (unital): (r ⊳ 1) * a = a * (r ⊳ 1)
 *
 * i.e. ι(r) is central (commuting). In nonassociative settings, “center” is often
 * commuting + nucleus; this tests just commuting.
 */
fun <R : Any, A : Any> scalarElementCommutesWithAllLaw(
    act: LeftAction<R, A>,
    mulA: BinOp<A>,
    oneA: A,
    arbR: Arb<R>,
    arbA: Arb<A>,
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
    label: String = "derived: (r ⊳ 1) * a = a * (r ⊳ 1)"
): TestingLaw = TestingLaw.named(label) {
    checkAll(Arb.pair(arbR, arbA)) { (r, a) ->
        val iota = act(r, oneA)
        val left = mulA(iota, a)
        val right = mulA(a, iota)

        withClue(
            buildString {
                appendLine("Scalar element commuting failed:")
                appendLine("r = ${prR(r)}")
                appendLine("a = ${prA(a)}")
                appendLine("ι(r) = r ${act.symbol} 1 = ${prA(iota)}")
                appendLine("LHS: ι(r) ${mulA.symbol} a = ${prA(left)}")
                appendLine("RHS: a ${mulA.symbol} ι(r) = ${prA(right)}")
            }
        ) {
            check(eqA(left, right))
        }
    }
}

/**
 * Derived law (unital): (r ⊳ 1) * (s ⊳ 1) = (r*s) ⊳ 1
 */
fun <R : Any, A : Any> scalarEmbeddingMultiplicativeDerivedLaw(
    act: LeftAction<R, A>,
    mulR: BinOp<R>,
    mulA: BinOp<A>,
    oneA: A,
    arbR: Arb<R>,
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
    label: String = "derived: (r ⊳ 1) * (s ⊳ 1) = (r*s) ⊳ 1"
): TestingLaw = TestingLaw.named(label) {
    checkAll(Arb.pair(arbR, arbR)) { (r, s) ->
        val iR = act(r, oneA)
        val iS = act(s, oneA)
        val left = mulA(iR, iS)

        val rs = mulR(r, s)
        val right = act(rs, oneA)

        withClue(
            buildString {
                appendLine("Scalar embedding multiplicativity failed:")
                appendLine("r = ${prR(r)}")
                appendLine("s = ${prR(s)}")
                appendLine("ι(r) = ${prA(iR)}")
                appendLine("ι(s) = ${prA(iS)}")
                appendLine("LHS: ι(r) ${mulA.symbol} ι(s) = ${prA(left)}")
                appendLine("RHS: ι(r ${mulR.symbol} s) = ${prA(right)}")
            }
        ) {
            check(eqA(left, right))
        }
    }
}
