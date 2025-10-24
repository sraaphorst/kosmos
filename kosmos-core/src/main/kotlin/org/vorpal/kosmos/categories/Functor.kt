package org.vorpal.kosmos.categories

/**
 * A Functor is any context F that can map over its contents.
 *
 * In category theory, it is a mapping between categories that preserves structure.
 * In programming, when we talk about a functor, we mean a type constructor F<_>
 *     that lets us “map” a function over its contents without changing the outer structure.
 *
 * In Haskell, fmap:: (A -> B) -> F<A> -> F<B>
 * Examples:
 *  - List: map(f): List<A> -> List<B>
 *  - Option: map(f): Option<A> -> Option<B>
 *  - FiniteSet: map(F): FiniteSet<A> -> FiniteSet<B>
 *
 * Functor Laws:
 *  1. Identity:     fa.map(::identity) == fa
 *  2. Composition:  fa.map(f compose g) == fa.map(g).map(f)
 *
 *  Kotlin's type system is not expressive enough to abstract over F directly.
 *  In Haskell or Scala, we would write:
 *  class Functor f where
 *    fmap :: (A -> B) -> f A -> f B
 *  where f is a type constructor (e.g. List, Option, Set).
 *
 *  In Kotlin, we cannot write a generic parameter that itself takes a type parameter:
 *  We can say class Foo<A>, but not class Foo<F<_>, A>.
 *
 *  Thus, to emulate this, we introduce a marker interface:
 *    interface Kind<F, out A>
 *  which means: "Kind<F, A> stands for the application of some constructor F to type A."
 *
 *  Then, to make the compiler remember which "container" we are talking about, we make a phantom type, e.g:
 *    object ForFiniteSet
 *  which allows us to talk about "something of type FiniteSet applied to A" as Kind<ForFiniteSet, A>.
 *
 *  toKind() and fix(): used for wrapping and unwrapping.
 *  Since Kotlin doesn't really have HKTs, our container FiniteSet<A> isn't actually a Kind<ForFiniteSet, A>.
 *  We fix that with a wrapper class:
 *    data class FiniteSetKind<A>(val value: FiniteSet<A>): Kind<ForFiniteSet, A>
 *  and then FiniteSetKind<A> is something the compiler recognizes as Kind<ForFiniteSet, A>.
 *
 *  toKind() wraps a real FiniteSet into this emulated HK form:
 *    FiniteSet<A>.toKind() -> FiniteSetOf<A> = FiniteSetKind<A>
 *  fix() unwraps it back into the real FiniteSet:
 *    FiniteSetKind<A>.fix() -> FiniteSet<A>.
 *
 *  xs.toKind(): FiniteSet<A> -> FiniteSetKind<A> (wrap)
 *  x.fix():     FiniteSetKind<A> -> FiniteSet<A> (unwrap)
 *  "Pretend my FiniteSet is an HKT so that I can use it generically."
 *
 *  Then we can define the FiniteSetFunctor instance.
 *  Functor: "For any container F, define how to map over it."
 *  Then we define the specific instance for FiniteSet, i.e. object FiniteSetFunctor : Functor<ForFiniteSet>.
 */
interface Functor<F> {
    fun <A, B> Kind<F, A>.map(f: (A) -> B): Kind<F, B>
}
