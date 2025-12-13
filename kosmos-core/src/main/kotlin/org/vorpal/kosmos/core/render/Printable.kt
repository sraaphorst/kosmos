package org.vorpal.kosmos.core.render

import java.math.BigDecimal
import java.math.BigInteger

/**
 * A renderer for values of A.
 */
fun interface Printable<in A> {
    fun render(a: A): String
    operator fun invoke(a: A): String = render(a)

    companion object {
        /** Default printable: uses toString(). */
        fun <A> default(): Printable<A> = Printable { it.toString() }

        /** Build from a lambda (handy at call sites). */
        fun <A> from(f: (A) -> String): Printable<A> = Printable { a -> f(a) }
    }
}

/** Contravariant mapping: reuse a Printable<B> for A via A -> B. */
fun <A, B> Printable<B>.contramap(f: (A) -> B): Printable<A> =
    Printable { a -> this.render(f(a)) }

/** Small convenience for inline use. */
fun <A> Printable<A>.pretty(a: A): String = render(a)

object Printables {
    val char: Printable<Char> = Printable.default()
    val int: Printable<Int> = Printable.default()
    val long: Printable<Long> = Printable.default()
    val bigInt: Printable<BigInteger> = Printable.default()

    val float: Printable<Float> = Printable.default()
    val double: Printable<Double> = Printable.default()
    val bigDecimal: Printable<BigDecimal> = Printable { it.stripTrailingZeros().toPlainString() }

    val string: Printable<String> = Printable.default()
}
