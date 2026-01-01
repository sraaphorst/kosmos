package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.InnerProductSpace
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.innerproduct.realInnerBilinearityLaws
import org.vorpal.kosmos.laws.innerproduct.realInnerSymmetryLaw
import org.vorpal.kosmos.laws.innerproduct.realNormConsistencyLaw
import org.vorpal.kosmos.laws.property.realPositiveDefiniteLaw
import org.vorpal.kosmos.laws.suiteName

class InnerProductSpaceLaws<V : Any>(
    space: InnerProductSpace<Real, V>,
    scalarArb: Arb<Real>,
    vectorArb: Arb<V>,
    eqV: Eq<V> = Eq.default(),
    tolerance: Real = 1e-10,
    eqReal: Eq<Real> = Eqs.realApprox(absTol = tolerance, relTol = tolerance),
    prV: Printable<V> = Printable.default(),
    prR: Printable<Real> = Printable.default(),
    includeNormLaw: Boolean = false,
) : LawSuite {

    override val name = suiteName(
        "InnerProductSpace",
        "R[${space.scalars.add.op.symbol}${space.scalars.mul.op.symbol}]",
        space.leftAction.symbol,
        "V[${space.group.op.symbol}]",
        "⟨·,·⟩"
    )

    private val vectorSpaceLaws: VectorSpaceLaws<Real, V> by lazy {
        VectorSpaceLaws(space, scalarArb, vectorArb, eqReal, eqV, prR, prV)
    }

    private val structureLaws: List<TestingLaw> =
        buildList {
            addAll(
                realInnerBilinearityLaws(
                    inner = space.inner,
                    addV = space.group.op,
                    addR = space.scalars.add.op,
                    mulR = space.scalars.mul.op,
                    act = space.leftAction,
                    scalarArb = scalarArb,
                    vectorArb = vectorArb,
                    eqR = eqReal,
                    prV = prV,
                    prR = prR
                )
            )

            add(
                realInnerSymmetryLaw(
                    inner = space.inner,
                    vectorArb = vectorArb,
                    eqR = eqReal,
                    prV = prV,
                    prR = prR
                )
            )

            add(
                realPositiveDefiniteLaw(
                    inner = org.vorpal.kosmos.core.ops.UnaryOp("⟨v,v⟩") { v -> space.inner(v, v) },
                    zeroVector = space.group.identity,
                    vectorArb = vectorArb,
                    vectorEq = eqV,
                    vectorPr = prV,
                    tolerance = tolerance
                )
            )

            if (includeNormLaw) {
                add(
                    realNormConsistencyLaw(
                        inner = space.inner,
                        norm = space::norm,
                        vectorArb = vectorArb,
                        eqR = eqReal,
                        tolerance = tolerance,
                        prV = prV,
                        prR = prR
                    )
                )
            }
        }

    override fun laws(): List<TestingLaw> =
        structureLaws

    override fun fullLaws(): List<TestingLaw> =
        vectorSpaceLaws.fullLaws() + structureLaws
}