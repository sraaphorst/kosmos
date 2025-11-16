//package org.vorpal.kosmos.functional.instances.finiteset
//
//import org.vorpal.kosmos.core.FiniteSet
//import org.vorpal.kosmos.core.FiniteSetOf
//import org.vorpal.kosmos.core.ForFiniteSet
//import org.vorpal.kosmos.core.fix
//import org.vorpal.kosmos.functional.core.Kind
//import org.vorpal.kosmos.functional.typeclasses.Applicative
//import org.vorpal.kosmos.functional.typeclasses.Monad
//
//
//object FiniteSetApplicative: Applicative<ForFiniteSet> {
//    override fun <A> just(a: A): FiniteSetOf<A> =
//        FiniteSet.singleton(a)
//
//    override fun <A, B> ap(
//        ff: FiniteSetOf<(A) -> B>,
//        fa: FiniteSetOf<A>
//    ): FiniteSetOf<B> {
//        val fFS = ff.fix()
//        return when (val aFS = fa.fix()) {
//            is FiniteSet.Ordered -> {
//                val out = aFS.order.flatMap { a ->
//                    fFS.map { f -> f(a) }.toList()
//                }
//                FiniteSet.ordered(out)
//            }
//            is FiniteSet.Unordered -> {
//                val out = aFS.backing.flatMap { a ->
//                    fFS.toSet().map { f -> f(a) }
//                }.toSet()
//                FiniteSet.unordered(out)
//            }
//        }
//    }
//}
//
//object FiniteSetMonad: Monad<ForFiniteSet> {
//    override fun <A> pure(a: A): FiniteSetOf<A> =
//        FiniteSet.singleton(a)
//
//    override fun <A, B> flatMap(
//        fa: FiniteSetOf<A>,
//        f: (A) -> FiniteSetOf<B>
//    ): FiniteSetOf<B> = when (val s = fa.fix()) {
//        is FiniteSet.Ordered -> {
//            val out = s.order.flatMap { a -> f(a).fix().toOrdered().order }
//            FiniteSet.ordered(out)
//        }
//        is FiniteSet.Unordered -> {
//            val out = s.backing.flatMap { a -> f(a).fix().toUnordered().backing }
//            FiniteSet.unordered(out)
//        }
//    }
//}
///**
// * Example: lifting a functor.
// * It doesn't matter if F is List, Option, FiniteSet, etc: as long as there's a Functor<F> instance.
// * Think of map (or fmap in Haskell) as a "function lifter."
// */
////fun <F, A, B> liftFunctor(
////    F: Functor<F>,
////    f: (A) -> B
////): (Kind<F, A>) -> Kind<F, B> =
////    { fa -> F.run { fa.maps(f) } }
//
//// Now we can write:
//// val xs: FiniteSet<Int> = FiniteSet.of(1, 2, 3)
//// val ys: FiniteSet<Int> = with(FiniteSetFunctor) { xs.toKind().map { it * 2 }.fix() }
//// where ys = {2, 4, 6}
////interface ComposedFunctor<F, G> : Functor<Composed<F, G>>
////data class Composed<F, G>(val f: F, val g: G)
////data class ComposedKind<F, G, A>(val value: Kind<F, Kind<G, A>>) : Kind<Composed<F, G>, A>
////fun <F, G, A> Kind<F, Kind<G, A>>.toComposed(): ComposedKind<F, G, A> = ComposedKind(this)
////fun <F, G, A> ComposedKind<F, G, A>.fix(): Kind<F, Kind<G, A>> = value
////fun <F, G> composedFunctor(
////    FF: Functor<F>,
////    GF: Functor<G>
////): Functor<Composed<F, G>> =
////    object : Functor<Composed<F, G>> {
////        override fun <A, B> Kind<Composed<F, G>, A>.maps(f: (A) -> B): Kind<Composed<F, G>, B> {
////            val outer: Kind<F, Kind<G, A>> = (this as ComposedKind<F, G, A>).fix()
////            val mappedOuter: Kind<F, Kind<G, B>> = FF.run {
////                outer.maps { inner -> GF.run { inner.maps(f) } }
////            }
////            return ComposedKind(mappedOuter)
////        }
////    }
