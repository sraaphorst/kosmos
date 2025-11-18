package org.vorpal.kosmos.functional.instances.option

import org.vorpal.kosmos.functional.datastructures.ForOption
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.OptionOf
import org.vorpal.kosmos.functional.datastructures.fix
import org.vorpal.kosmos.functional.typeclasses.Applicative
import org.vorpal.kosmos.functional.typeclasses.Monad

object OptionApplicative : Applicative<ForOption> {
    override fun <A> pure(a: A): OptionOf<A> =
        Option.Some(a)

    override fun <A, B> ap(
        ff: OptionOf<(A) -> B>,
        fa: OptionOf<A>
    ): OptionOf<B> {
        return when (val fOpt = ff.fix) {
            is Option.None -> Option.None
            is Option.Some ->
                when (val aOpt = fa.fix) {
                    is Option.None -> Option.None
                    is Option.Some -> Option.Some(fOpt.value(aOpt.value))
                }
        }
    }
}

object OptionMonad : Monad<ForOption> {
    override fun <A> pure(a: A): OptionOf<A> =
        OptionApplicative.pure(a)

    override fun <A, B> ap(ff: OptionOf<(A) -> B>, fa: OptionOf<A>): OptionOf<B> =
        OptionApplicative.ap(ff, fa)

    override fun <A, B> map(fa: OptionOf<A>, f: (A) -> B): OptionOf<B> =
        OptionApplicative.map(fa, f)

    override fun <A, B> flatMap(
        fa: OptionOf<A>,
        f: (A) -> OptionOf<B>
    ): OptionOf<B> = when (val aOpt = fa.fix) {
        is Option.None -> Option.None
        is Option.Some -> f(aOpt.value)
    }
}

// Alternatively:
//object OptionMonad2 : Monad<ForOption>, Applicative<ForOption> by OptionApplicative {
//
//    override fun <A, B> map(fa: OptionOf<A>, f: (A) -> B): OptionOf<B> =
//        super<Monad>.map(fa, f)
//
//    override fun <A, B> ap(ff: OptionOf<(A) -> B>, fa: OptionOf<A>): OptionOf<B> =
//        super<Monad>.ap(ff, fa)
//
//    override fun <A, B> flatMap(
//        fa: OptionOf<A>,
//        f: (A) -> OptionOf<B>
//    ): OptionOf<B> =
//        when (val aOpt = fa.fix) {
//            is Option.None -> Option.None
//            is Option.Some -> f(aOpt.value)
//        }
//}
