package org.vorpal.kosmos.laws.innerproduct

import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.BilinearForm
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith
import org.vorpal.kosmos.laws.property.bilinearAddLeftLaw
import org.vorpal.kosmos.laws.property.bilinearAddRightLaw
import org.vorpal.kosmos.laws.property.bilinearScalarLeftLaw
import org.vorpal.kosmos.laws.property.bilinearScalarRightLaw

fun <V : Any> realInnerBilinearityLaws(
    inner: BilinearForm<V, Real>,
    addV: BinOp<V>,
    addR: BinOp<Real>,
    mulR: BinOp<Real>,
    act: LeftAction<Real, V>,
    scalarArb: Arb<Real>,
    vectorArb: Arb<V>,
    eqR: Eq<Real> = Eq.default(),
    prV: Printable<V> = Printable.default(),
    prR: Printable<Real> = Printable.default(),
): List<TestingLaw> = listOf(
    bilinearAddLeftLaw(
        form = inner,
        addV = addV,
        addF = addR,
        arbV = vectorArb,
        eqF = eqR,
        prV = prV,
        prF = prR,
        label = "inner: additivity in left argument"
    ),
    bilinearScalarLeftLaw(
        form = inner,
        act = act,
        mulF = mulR,
        arbF = scalarArb,
        arbV = vectorArb,
        eqF = eqR,
        prV = prV,
        prF = prR,
        label = "inner: homogeneity in left argument"
    ),
    bilinearAddRightLaw(
        form = inner,
        addV = addV,
        addF = addR,
        arbV = vectorArb,
        eqF = eqR,
        prV = prV,
        prF = prR,
        label = "inner: additivity in right argument"
    ),
    bilinearScalarRightLaw(
        form = inner,
        act = act,
        mulF = mulR,
        arbF = scalarArb,
        arbV = vectorArb,
        eqF = eqR,
        prV = prV,
        prF = prR,
        label = "inner: homogeneity in right argument"
    ),
)

fun <V : Any> realInnerSymmetryLaw(
    inner: BilinearForm<V, Real>,
    vectorArb: Arb<V>,
    eqR: Eq<Real> = Eq.default(),
    prV: Printable<V> = Printable.default(),
    prR: Printable<Real> = Printable.default(),
    label: String = "inner: symmetry ⟨v,w⟩ = ⟨w,v⟩"
): TestingLaw = TestingLaw.named(label) {
    lawCheckAll(Arb.pair(vectorArb, vectorArb)) { (v, w) ->
        val left = inner(v, w)
        val right = inner(w, v)

        lawCheck(eqR(left, right)) {
            buildString {
                appendLine("Inner symmetry failed:")
                appendLine("v = ${prV(v)}")
                appendLine("w = ${prV(w)}")
                appendLine("⟨v,w⟩ = ${prR(left)}")
                appendLine("⟨w,v⟩ = ${prR(right)}")
            }
        }
    }
}

fun <V : Any> realNormConsistencyLaw(
    inner: BilinearForm<V, Real>,
    norm: (V) -> Real,
    vectorArb: Arb<V>,
    eqR: Eq<Real> = Eq.default(),
    tolerance: Real = 1e-10,
    prV: Printable<V> = Printable.default(),
    prR: Printable<Real> = Printable.default(),
    label: String = "norm: ‖v‖² = ⟨v,v⟩ and ‖v‖ ≥ 0"
): TestingLaw = TestingLaw.named(label) {
    lawCheckAll(vectorArb, render = renderWith(prV)) { v ->
        val n = norm(v)
        val n2 = n * n
        val vv = inner(v, v)

        val message = {
            buildString {
                appendLine("Norm consistency failed:")
                appendLine("v = ${prV(v)}")
                appendLine("‖v‖ = ${prR(n)}")
                appendLine("‖v‖² = ${prR(n2)}")
                appendLine("⟨v,v⟩ = ${prR(vv)}")
                appendLine("tolerance = $tolerance")
            }
        }
        lawCheck(n >= -tolerance, message)
        lawCheck(eqR(n2, vv), message)
    }
}
