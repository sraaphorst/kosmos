//package org.vorpal.kosmos.functional.instances.option
//
//
//import org.vorpal.kosmos.functional.core.Kind
//import org.vorpal.kosmos.functional.datastructures.ForOption
//import org.vorpal.kosmos.functional.datastructures.Option
//import org.vorpal.kosmos.functional.datastructures.OptionOf
//import org.vorpal.kosmos.functional.datastructures.fix
//import org.vorpal.kosmos.functional.typeclasses.Applicative
//import org.vorpal.kosmos.functional.typeclasses.Monad
//
///**
// * The Applicative instance for Option.
// * Modeling: None short-circuits; Some applies the function.
// * Note that this is also a [org.vorpal.kosmos.functional.typeclasses.Functor] and an [org.vorpal.kosmos.functional.typeclasses.Apply].
// */
//object OptionApplicative : Applicative<ForOption> {
//
//    override fun <A> just(a: A): OptionOf<A> =
//        Option.Some(a)
//
//    override fun <A, B> ap(ff: OptionOf<(A) -> B>, fa: OptionOf<A>): OptionOf<B> {
//        val aopt = fa.fix()
//        return when (val fopt = ff.fix()) {
//            is Option.None -> Option.None
//            is Option.Some ->
//                when (aopt) {
//                    is Option.None -> Option.None
//                    is Option.Some -> Option.Some(fopt.value(aopt.value))
//                }
//        }
//    }
//}
//
//object OptionMonad : Monad<ForOption>, Applicative<ForOption> by OptionApplicative {
//    override fun <A> pure(a: A): Kind<ForOption, A> = just(a)
//    override fun <A, B> flatMap(
//        fa: OptionOf<A>,
//        f: (A) -> OptionOf<B>
//    ): OptionOf<B> =
//        when (val oa = fa.fix()) {
//            is Option.None -> Option.None
//            is Option.Some -> f(oa.value)
//        }
//}
