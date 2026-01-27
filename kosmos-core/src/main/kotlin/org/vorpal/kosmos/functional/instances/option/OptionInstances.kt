package org.vorpal.kosmos.functional.instances.option

import org.vorpal.kosmos.functional.datastructures.Either
import org.vorpal.kosmos.functional.datastructures.ForOption
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.OptionOf
import org.vorpal.kosmos.functional.datastructures.fix
import org.vorpal.kosmos.functional.datastructures.flatMap
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

object OptionMonad: Monad<ForOption> {
    override fun <A> pure(a: A): OptionOf<A> =
        OptionApplicative.pure(a)

    override fun <A, B> flatMap(
        fa: OptionOf<A>,
        f: (A) -> OptionOf<B>
    ): OptionOf<B> = when (val aOpt = fa.fix) {
        is Option.None -> Option.None
        is Option.Some -> f(aOpt.value)
    }

    /**
     * Stack-safe implementation of tailRecM for Option.
     * * This converts the recursive step into an imperative while loop.
     * It runs in constant stack space regardless of how many times it loops.
     */
    override fun <A, B> tailRecM(a: A, f: (A) -> OptionOf<Either<A, B>>): OptionOf<B> {
        var current = a
        while (true) {
            val result = f(current).fix
            when (result) {
                is Option.None -> return Option.None
                is Option.Some -> {
                    when (val either = result.value) {
                        is Either.Left -> {
                            // The loop continues with the new value 'a'
                            current = either.value
                        }
                        is Either.Right -> {
                            // The loop terminates with result 'b'
                            return Option.Some(either.value)
                        }
                    }
                }
            }
        }
    }
}

// TODO: DELETE ALL THIS.
fun naiveRecursive(num: Int): Option<String> {
    // This is NOT stack-safe.
    // It builds a chain of 100,000 flatMap calls in memory.
    return recFun(num).fix.flatMap { either ->
        when (either) {
            is Either.Left -> naiveRecursive(either.value) // <--- Recursion here
            is Either.Right -> Option.Some(either.value)
        }
    }
}

fun recFun(num: Int): OptionOf<Either<Int, String>> =
    if (num <= 0) Option.Some(Either.Right("Never going to be done..."))
    else Option.Some(Either.Left(num - 1))

fun main() {
    val hugeNumber = 100_000

    // 1. Safe (Using your tailRecM loop)
    println(OptionMonad.tailRecM(hugeNumber, ::recFun))
    // Output: Some(value=Never going to be done...)

    // 2. Unsafe (Using standard recursion)
    // This will throw StackOverflowError
//    println(naiveRecursive(hugeNumber))
}
