package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Vector space = module over a field.
 *
 * This is really just:
 *  - FieldLaws for the scalar field F
 *  - ModuleLaws for the R-module (here R = F is a field)
 */
class VectorSpaceLaws<F : Any, V : Any>(
    private val field: Field<F>,
    private val module: RModule<F, V>,
    private val scalarArb: Arb<F>,
    private val vectorArb: Arb<V>,
    private val scalarEq: Eq<F>,
    private val vectorEq: Eq<V>,
    private val scalarPrintable: Printable<F> = default(),
    private val vectorPrintable: Printable<V> = default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋅",
    private val actionSymbol: String = "·"
) {

    fun laws(): List<TestingLaw> =
        FieldLaws(
            field = field,
            arb = scalarArb,
            eq = scalarEq,
            pr = scalarPrintable,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() +
                RModuleLaws(
                    module = module,
                    scalarArb = scalarArb,
                    vectorArb = vectorArb,
                    eq = vectorEq,
                    pr = vectorPrintable,
                    scalarSymbol = actionSymbol
                ).laws()
}