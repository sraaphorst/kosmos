package org.vorpal.kosmos.laws.core

import arrow.core.identity
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldNotThrowAny
import org.vorpal.kosmos.algebra.IndexFunction

/**
 * Generic algebraic laws for any self-composable IndexFunction<T>.
 *
 * T must implement an interface like:
 *
 * interface IndexFunction<T : IndexFunction<T>> {
 *     fun run(n: Int): Int
 *     fun andThen(other: T): T
 *     fun flatMap(f: (Int) -> T): T
 *     fun repeat(k: Int): T
 * }
 */
object IndexFunctionLaws {

    data class Context<T : IndexFunction<T>>(
        val Id: T,
        val sample: () -> T,
        val pure: (f: (Int) -> Int) -> T
    )

    /**
     * Identity law: Id ∘ f == f ∘ Id == f
     */
    fun <T : IndexFunction<T>> identityLaw(ctx: Context<T>, n: Int = 5) {
        val f = ctx.sample()
        (ctx.Id andThen f).run(n) shouldBe f.run(n)
        (f andThen ctx.Id).run(n) shouldBe f.run(n)
    }

    /**
     * Associativity law: (f ∘ g) ∘ h == f ∘ (g ∘ h)
     */
    fun <T : IndexFunction<T>> associativityLaw(ctx: Context<T>, n: Int = 5) {
        val f = ctx.sample()
        val g = ctx.sample()
        val h = ctx.sample()
        ((f andThen g) andThen h).run(n) shouldBe (f andThen (g andThen h)).run(n)
    }

    /**
     * Functor map law: Id.map(f) == pure(f)
     * (requires T to support map via pure lifting)
     */
    fun <T : IndexFunction<T>> functorMapLaw(ctx: Context<T>) {
        val f = { x: Int -> x + 1 }
        ctx.pure(f).run(5) shouldBe f(5)
    }

    /**
     * Left identity: pure(x).flatMap(f) == f(x)
     */
    fun <T : IndexFunction<T>> leftIdentityLaw(ctx: Context<T>, x: Int = 4) {
        // f can be anything lawful; this one depends on the bound value n
        val f = { n: Int -> ctx.sample().repeat(n % 3) }

        // Evaluate both sides at the injected value x (matches value-binding semantics)
        ctx.pure { x }.flatMap(f).run(x) shouldBe f(x).run(x)
    }

    // TODO: Check this.
    /**
     * Right identity: m.flatMap(pure) == m
     */
    fun <T : IndexFunction<T>> rightIdentityLaw(ctx: Context<T>, n: Int = 5) {
        val m = ctx.sample()
        m.flatMap { ctx.pure(::identity) }.run(n) shouldBe m.run(n)
    }

    /**
     * Repeat law: repeat(k) == composition of self k times
     */
    fun <T : IndexFunction<T>> repeatLaw(ctx: Context<T>, k: Int = 3, n: Int = 5) {
        val f = ctx.sample()
        f.repeat(k).run(n) shouldBe (1..k).fold(n) { acc, _ -> f.run(acc) }
    }

    /**
     * Sanity run: all laws execute successfully.
     */
    fun <T : IndexFunction<T>> sanity(ctx: Context<T>) {
        shouldNotThrowAny {
            identityLaw(ctx)
            associativityLaw(ctx)
            functorMapLaw(ctx)
            leftIdentityLaw(ctx)
            rightIdentityLaw(ctx)
            repeatLaw(ctx)
        }
    }
}