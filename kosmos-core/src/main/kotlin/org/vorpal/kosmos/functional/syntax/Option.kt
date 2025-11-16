package org.vorpal.kosmos.functional.syntax

import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.Options
import org.vorpal.kosmos.functional.datastructures.Options.liftOption

/**
 * Reads nicely at call sites:
 * alternative to [Options.map2].
 */
fun <A, B, C> Option<A>.map2(
    ob: Option<B>,
    f: (A, B) -> C
): Option<C> =
    Options.map2(this, ob, f)

/**
 * Reads nicely at call sites:
 * alternative to [Options.map3].
 */
fun <A, B, C, D> Option<A>.map3(
    ob: Option<B>,
    oc: Option<C>,
    f: (A, B, C) -> D
): Option<D> =
    Options.map3(this, ob, oc, f)

// Example in real code:
fun main() {
    val oa = Options.just(2)
    val ob = Options.just(3)
    val oc = Options.just(5)

    // Option.Some(5)
    val r2 = Options.map2(oa, ob) { a, b -> a + b }
    println(r2)

    // Option.Some(11)
    val r3 = Options.map3(oa, ob, oc) { a, b, c -> a * b + c }
    println(r3)

    val f2 = { a: Int, b: Int -> a + b }.liftOption()
    val r2b = f2(oa, ob)
    println(r2b)

    val f3 = { a: Int, b: Int, c: Int -> a * b + c }.liftOption()
    val r3b = f3(oa, ob, oc)
    println(r3b)
}