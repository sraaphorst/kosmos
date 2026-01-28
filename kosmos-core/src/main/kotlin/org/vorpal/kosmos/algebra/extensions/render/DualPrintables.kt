package org.vorpal.kosmos.algebra.extensions.render

import org.vorpal.kosmos.algebra.extensions.Dual
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.from

object DualPrintables {

    /** Always prints "a + bε". */
    fun <F : Any> dual(prF: Printable<F>): Printable<Dual<F>> =
        from { x ->
            "${prF(x.a)} + ${prF(x.b)}${Symbols.EPSILON}"
        }

    /**
     * Prints "a - |b|ε" iff rendered b starts with '-'; otherwise "a + bε".
     * Purely presentational: does not assume algebraic sign.
     */
    fun <F : Any> dualSigned(prF: Printable<F>): Printable<Dual<F>> =
        from { x ->
            val a = prF(x.a)
            val b = prF(x.b)

            if (b.startsWith("-")) "$a - ${b.drop(1).trimStart()}${Symbols.EPSILON}"
            else "$a + $b${Symbols.EPSILON}"
        }

    /**
     * Compact form: drops zero parts, prints ε for coefficient 1.
     * Does NOT introduce '-' based on negOne; stays algebraically neutral.
     */
    fun <F : Any> compact(
        prF: Printable<F>,
        field: Field<F>,
        eq: Eq<F> = Eq.default(),
    ): Printable<Dual<F>> =
        from { x ->
            val a0 = eq(x.a, field.zero)
            val b0 = eq(x.b, field.zero)
            val b1 = eq(x.b, field.one)

            when {
                a0 && b0 -> prF(field.zero)                      // 0
                a0 && b1 -> Symbols.EPSILON                      // ε
                a0       -> "${prF(x.b)}${Symbols.EPSILON}"       // bε
                b0       -> prF(x.a)                              // a
                b1       -> "${prF(x.a)} + ${Symbols.EPSILON}"    // a + ε
                else     -> "${prF(x.a)} + ${prF(x.b)}${Symbols.EPSILON}"
            }
        }

    /**
     * Compact + string-minus prettiness.
     * Uses '-' only if the rendered coefficient starts with '-'.
     */
    fun <F : Any> compactSigned(
        prF: Printable<F>,
        field: Field<F>,
        eq: Eq<F> = Eq.default(),
    ): Printable<Dual<F>> =
        from { x ->
            val a0 = eq(x.a, field.zero)
            val b0 = eq(x.b, field.zero)
            val b1 = eq(x.b, field.one)

            when {
                a0 && b0 -> prF(field.zero)
                a0 && b1 -> Symbols.EPSILON
                a0       -> "${prF(x.b)}${Symbols.EPSILON}"
                b0       -> prF(x.a)
                b1       -> "${prF(x.a)} + ${Symbols.EPSILON}"
                else     -> {
                    val a = prF(x.a)
                    val b = prF(x.b)

                    if (b.startsWith("-")) "$a - ${b.drop(1).trimStart()}${Symbols.EPSILON}"
                    else "$a + $b${Symbols.EPSILON}"
                }
            }
        }
}