package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.InnerProductSpace
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.BilinearFormLaw
import org.vorpal.kosmos.laws.property.SymmetryLaw
import org.vorpal.kosmos.laws.property.PositiveDefiniteLaw

class InnerProductSpaceLaws<F : Any, V : Any>(
    private val space: InnerProductSpace<F, V>,
    private val scalarArb: Arb<F>,
    private val vectorArb: Arb<V>,
    private val scalarEq: Eq<F>,
    private val vectorEq: Eq<V>,
    private val isNonNegative: (F) -> Boolean,
    private val scalarPrintable: Printable<F> = default(),
    private val vectorPrintable: Printable<V> = default(),
    private val scalarSymbol: String = "⟨·,·⟩",
) {

    fun laws(): List<TestingLaw> {
        val inner: (V, V) -> F = space::inner
        val zeroVector = space.group.identity
        val scalarZero = space.field.zero

        return listOf(
            // Symmetry / conjugate symmetry
            SymmetryLaw(
                op = space::inner,//{ x: V, y: V -> inner(x, y) },
                pairArb = Arb.pair(vectorArb, vectorArb),
                eq = scalarEq,
                prA = vectorPrintable,
                prB = scalarPrintable,
                symbol = scalarSymbol
            ),

            // Bilinearity in each variable, etc. (schematic)
            BilinearFormLaw(
                f = space::inner,
                addS = space.field.add.op,
                mulS = space.field.mul.op,
                addV = space.group.op,
                scalarAction = space.action,
                scalarArb = scalarArb,
                vectorArb = vectorArb,
                eq = scalarEq,
                prS = scalarPrintable,
                prV = vectorPrintable,
            ),

            // Positive-definiteness: ⟨x,x⟩ ≥ 0 and = 0 iff x = 0
            PositiveDefiniteLaw(
                inner = { x: V -> inner(x, x) },
                zeroVector = zeroVector,
                scalarZero = scalarZero,
                vectorArb = vectorArb,
                scalarEq = scalarEq,
                vectorEq = vectorEq,
                isNonNegative = isNonNegative,
                scalarPrintable = scalarPrintable,
                vectorPrintable = vectorPrintable
            )
        )
    }
}