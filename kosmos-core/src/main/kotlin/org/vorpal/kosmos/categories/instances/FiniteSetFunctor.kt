package org.vorpal.kosmos.categories.instances

import org.vorpal.kosmos.categories.*
import org.vorpal.kosmos.core.FiniteSet

/**
 * FiniteSetOf<A> plays the role of F<A> in Kotlin since Kotlin does not have support for
 * higher-kinded types.
 * ForFiniteSet is the stand-in for F<_>, a type constructor (container that takes one argument).
 * It is a phantom-type witness.
 * We emulate it in Kotlin by:
 * - Defining a Kind<F, A> interface.
 * - Introducing a witness object like ForFiniteSet.
 * - Defining helpers like fix and toKind to
 */
object ForFiniteSet

/**
 * Higher-kinded alias.
 * "A FiniteSet<A>, but seen abstractly."
 */
typealias FiniteSetOf<A> = Kind<ForFiniteSet, A>

/** Wrapper so FiniteSet can act as a Kind at runtime. */
data class FiniteSetKind<A>(val value: FiniteSet<A>) : FiniteSetOf<A>

/** Smart conversions. */
fun <A> FiniteSet<A>.toKind(): FiniteSetOf<A> = FiniteSetKind(this)

/** fix() -> unwrap the FiniteSetKind<A> into a real FiniteSet<A>. */
fun <A> FiniteSetOf<A>.fix(): FiniteSet<A> =
    (this as FiniteSetKind<A>).value

fun <A> FiniteSetOf<A>.unwrap(): FiniteSet<A> = fix()
operator fun <A> FiniteSetOf<A>.iterator(): Iterator<A> = fix().iterator()

/**
 * Functor instance for FiniteSet: we map over a FiniteSet.
 * .map(f) -> apply your existing FiniteSet.map(f).
 * FiniteSetKind(...) -> rewrap the result as a Kind<ForFiniteSet, B>.
 */
object FiniteSetFunctor : Functor<ForFiniteSet> {
    override fun <A, B> FiniteSetOf<A>.map(f: (A) -> B): FiniteSetOf<B> =
        FiniteSetKind(fix().map(f))
}

// Here is the implementation for a List.
object ForList
typealias ListOf<A> = Kind<ForList, A>
data class ListKind<A>(val value: List<A>) : ListOf<A>
fun <A> List<A>.toKind(): ListOf<A> = ListKind(this)
fun <A> ListOf<A>.fix(): List<A> =
    (this as ListKind<A>).value
object ListFunctor : Functor<ForList> {
    override fun <A, B> ListOf<A>.map(f: (A) -> B): ListOf<B> =
        ListKind(fix().map(f))
}

/**
 * The reasons we don't call FiniteSetFunctor or toKind() directly most of the time:
 * the pattern is designed to let generic code (libraries) work for any functorial type.
 */

/**
 * Example: lifting a functor.
 * It doesn't matter if F is List, Option, FiniteSet, etc: as long as there's a Functor<F> instance.
 * Think of map (or fmap in Haskell) as a "function lifter."
 */
fun <F, A, B> liftFunctor(
    F: Functor<F>,
    f: (A) -> B
): (Kind<F, A>) -> Kind<F, B> =
    { fa -> F.run { fa.map(f) } }

// Now we can write:
// val xs: FiniteSet<Int> = FiniteSet.of(1, 2, 3)
// val ys: FiniteSet<Int> = with(FiniteSetFunctor) { xs.toKind().map { it * 2 }.fix() }
// where ys = {2, 4, 6}
interface ComposedFunctor<F, G> : Functor<Composed<F, G>>
data class Composed<F, G>(val f: F, val g: G)
data class ComposedKind<F, G, A>(val value: Kind<F, Kind<G, A>>) : Kind<Composed<F, G>, A>
fun <F, G, A> Kind<F, Kind<G, A>>.toComposed(): ComposedKind<F, G, A> = ComposedKind(this)
fun <F, G, A> ComposedKind<F, G, A>.fix(): Kind<F, Kind<G, A>> = value
fun <F, G> composedFunctor(
    FF: Functor<F>,
    GF: Functor<G>
): Functor<Composed<F, G>> =
    object : Functor<Composed<F, G>> {
        override fun <A, B> Kind<Composed<F, G>, A>.map(f: (A) -> B): Kind<Composed<F, G>, B> {
            val outer: Kind<F, Kind<G, A>> = (this as ComposedKind<F, G, A>).fix()
            val mappedOuter: Kind<F, Kind<G, B>> = FF.run {
                outer.map { inner -> GF.run { inner.map(f) } }
            }
            return ComposedKind(mappedOuter)
        }
    }

val ListListFunctor = composedFunctor(ListFunctor, ListFunctor)


fun main() {
    val xs: FiniteSet<Int> = FiniteSet.of(1, 2, 3)
    val ys: FiniteSet<Int> = with(FiniteSetFunctor) { xs.toKind().map { it * 2 }.fix() }
    println(ys)

    // List the String::length functor to List<String>.
    val stringList: List<String> = listOf("hello", "world")
    val lifted = liftFunctor(ListFunctor, String::length)
    println(lifted(stringList.toKind()).fix())

    // The point is reusability: other generic abstractions (Applicative, Traversable, Monad)
    // can now treat FiniteSet and List as a functor without caring what it actually is.
    // Analogy table:
    // Concept            Haskell         Kotlin Emulation           Meaning
    // Type constructor   List            ForFiniteSet               A type family that takes one parameter.
    // Type application   List<Int>       Kind<ForFiniteSet, Int>    Applying the constructor.
    // Real value         [1, 2, 3]       FiniteSet.of(1, 2, 3)      The actual data.
    // Wrapper            -               FiniteSetKind              Allows runtime Kind representation
    // fmap               fmap f xs       with(FiniteSetFunctor)     Generic mapping
    //                                    {xs.toKind().map(f).fix()}

    // Symbol / Term              Purpose
    // Functor<F>                 Declares how to map over any type constructor F<_>
    // ForFiniteSet               Phantom type: tells Kotlin "this type belongs to FiniteSet."
    // Kind<F, A>                 Type-level stand-in for "F".
    // FiniteSetKind<A>           Runtime wrapper for FiniteSet<A> so it can act as a Kind.
    // toKind()                   Wraps FiniteSet<A> -> FiniteSetKind<A>.
    // fix()                      Unwraps FiniteSetKind<A> -> FiniteSet<A>.
    // FiniteSetFunctor           The instance saying how to map a function over FiniteSets generically.
    // Usage                      Only needed when you want code to work over any Functor, not just FiniteSet.

    // Lifting intuition: f works for plain Ints.
    val f: (Int) -> String = { n -> "Result: $n" }

    // What if you don't have an Int, but a container of Ints?
    // Example: List<Int> or FiniteSet<Int>.
    // You can't feed a List<Int> into a function expecting an Int.
    // Instead of changing f, we lift it: we extend its domain and codomain so it works inside the functor.
    // f: A -> B becomes F(f): F -> F.
    // Needs to know if ordered or unordered, though.
    val ws: FiniteSet<Int> = FiniteSet.of(1, 2, 3)
    val ts = FiniteSet.of(4, 5, 6).toUnordered()
    val halver = { i: Int -> i / 2.0 }
    val liftedHalf = liftFunctor(FiniteSetFunctor, halver)//{ i: Int -> i / 2.0}
    println(liftedHalf(ws.toKind()).fix())

    // Unordered needs to be iterated over.
    liftedHalf(ts.toKind()).fix().forEach { println(it) }
}

