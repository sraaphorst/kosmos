package org.vorpal.kosmos.functional.datastructures

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.vorpal.kosmos.functional.datastructures.Options.catches
import org.vorpal.kosmos.functional.datastructures.Options.catchesAll
import org.vorpal.kosmos.functional.datastructures.Options.map2
import org.vorpal.kosmos.functional.datastructures.Options.sequence
import org.vorpal.kosmos.functional.datastructures.Options.traverse

class OptionSpec : FunSpec({
    // ============ Functor Laws ============

    context("Functor Laws") {

        test("Identity: map(id) == id") {
            checkAll(ArbOption.arbIntOption) { opt ->
                opt.map { it } shouldBe opt
            }
        }

        test("Composition: map(f).map(g) == map(g ∘ f)") {
            checkAll(ArbOption.arbIntOption, Arb.int(), Arb.int()) { opt, x, y ->
                val f: (Int) -> Int = { it + x }
                val g: (Int) -> String = { (it * y).toString() }

                opt.map(f).map(g) shouldBe opt.map { g(f(it)) }
            }
        }
    }

    // ============ Monad Laws ============

    context("Monad Laws") {

        test("Left Identity: pure(a).flatMap(f) == f(a)") {
            checkAll(Arb.int()) { a ->
                val f: (Int) -> Option<String> = { Option.Some((it * 2).toString()) }

                Option.Some(a).flatMap(f) shouldBe f(a)
            }
        }

        test("Right Identity: m.flatMap(pure) == m") {
            checkAll(ArbOption.arbIntOption) { opt ->
                opt.flatMap { Option.Some(it) } shouldBe opt
            }
        }

        test("Associativity: m.flatMap(f).flatMap(g) == m.flatMap { f(it).flatMap(g) }") {
            checkAll(ArbOption.arbIntOption, Arb.int(), Arb.int()) { opt, x, y ->
                val f: (Int) -> Option<Int> = { Option.Some(it + x) }
                val g: (Int) -> Option<String> = { Option.Some((it * y).toString()) }

                opt.flatMap(f).flatMap(g) shouldBe opt.flatMap { f(it).flatMap(g) }
            }
        }
    }

    // ============ map ============

    context("map") {

        test("Some(x).map(f) == Option.Some(f(x))") {
            checkAll(Arb.int()) { x ->
                val f: (Int) -> String = { (it * 2).toString() }
                Option.Some(x).map(f) shouldBe Option.Some(f(x))
            }
        }

        test("None.map(f) == Option.None") {
            val f: (Int) -> String = { it.toString() }
            Option.None.map(f) shouldBe Option.None
        }
    }

    // ============ flatMap ============

    context("flatMap") {

        test("Some(x).flatMap(f) == f(x)") {
            checkAll(Arb.int()) { x ->
                val f: (Int) -> Option<String> = { Option.Some((it * 2).toString()) }
                Option.Some(x).flatMap(f) shouldBe f(x)
            }
        }

        test("None.flatMap(f) == Option.None") {
            val f: (Int) -> Option<String> = { Option.Some(it.toString()) }
            Option.None.flatMap(f) shouldBe Option.None
        }

        test("flatMap short-circuits on Option.None from function") {
            checkAll(Arb.int()) { x ->
                val f: (Int) -> Option<String> = { Option.None }
                Option.Some(x).flatMap(f) shouldBe Option.None
            }
        }
    }

    // ============ flatten ============

    context("flatten") {

        test("Some(Some(x)).flatten() == Option.Some(x)") {
            checkAll(Arb.int()) { x ->
                val nested: Option<Option<Int>> = Option.Some(Option.Some(x))
                nested.flatten() shouldBe Option.Some(x)
            }
        }

        test("Some(None).flatten() == Option.None") {
            val nested: Option<Option<Int>> = Option.Some(Option.None)
            nested.flatten() shouldBe Option.None
        }

        test("None.flatten() == Option.None") {
            val nested: Option<Option<Int>> = Option.None
            nested.flatten() shouldBe Option.None
        }

        test("flatten is equivalent to flatMap(identity)") {
            checkAll(ArbOption.option(ArbOption.arbIntOption)) { nested ->
                nested.flatten() shouldBe nested.flatMap { it }
            }
        }
    }

    // ============ getOrElse ============

    context("getOrElse") {

        test("Some(x).getOrElse(default) == x") {
            checkAll(Arb.int(), Arb.int()) { x, default ->
                Option.Some(x).getOrElse { default } shouldBe x
            }
        }

        test("None.getOrElse(default) == default") {
            checkAll(Arb.int()) { default ->
                Option.None.getOrElse { default } shouldBe default
            }
        }

        test("default is lazy (not evaluated for Option.Some)") {
            var evaluated = false
            Option.Some(42).getOrElse {
                evaluated = true
                0
            }
            evaluated shouldBe false
        }

        test("default is evaluated for Option.None") {
            var evaluated = false
            Option.None.getOrElse {
                evaluated = true
                0
            }
            evaluated shouldBe true
        }
    }

    // ============ orElse ============

    context("orElse") {

        test("Some(x).orElse(other) == Option.Some(x)") {
            checkAll(Arb.int(), ArbOption.arbIntOption) { x, other ->
                Option.Some(x).orElse(other) shouldBe Option.Some(x)
            }
        }

        test("None.orElse(other) == other") {
            checkAll(ArbOption.arbIntOption) { other ->
                Option.None.orElse(other) shouldBe other
            }
        }

        test("orElse chains correctly") {
            Option.None
                .orElse(Option.None)
                .orElse(Option.Some(42)) shouldBe Option.Some(42)
        }
    }

    // ============ isPresent ============

    context("isPresent") {

        test("Some(x).isPresent() == true") {
            checkAll(Arb.int()) { x ->
                Option.Some(x).isNonEmpty() shouldBe true
            }
        }

        test("None.isPresent() == false") {
            Option.None.isNonEmpty() shouldBe false
        }
    }

    // ============ filter ============

    context("filter") {

        test("Some(x).filter(p) == Option.Some(x) when p(x) is true") {
            checkAll(Arb.int()) { x ->
                Option.Some(x).filter { it == x } shouldBe Option.Some(x)
            }
        }

        test("Some(x).filter(p) == Option.None when p(x) is false") {
            checkAll(Arb.int()) { x ->
                Option.Some(x).filter { false } shouldBe Option.None
            }
        }

        test("None.filter(p) == Option.None") {
            Option.None.filter { _: Int -> true } shouldBe Option.None
        }

        test("filter with always-true predicate is identity") {
            checkAll(ArbOption.arbIntOption) { opt ->
                opt.filter { true } shouldBe opt
            }
        }

        test("filter with always-false predicate yields Option.None") {
            checkAll(ArbOption.arbIntOption) { opt ->
                val result = opt.filter { false }
                result shouldBe Option.None
            }
        }

        test("filter composition") {
            checkAll(ArbOption.some(Arb.int(0..100))) { opt: Option<Int> ->
                val p1: (Int) -> Boolean = { it > 25 }
                val p2: (Int) -> Boolean = { it < 75 }

                opt.filter(p1)
                    .filter(p2) shouldBe opt.filter { p1(it) && p2(it) }
            }
        }
    }

    // ============ Integration Tests ============

    context("Integration") {

        test("chaining operations") {
            val result = Option.Some(5)
                .map { it * 2 }
                .filter { it > 5 }
                .flatMap { Option.Some(it.toString()) }
                .getOrElse { "default" }

            result shouldBe "10"
        }

        test("short-circuiting on Option.None") {
            val result = Option.None
                .map { x: Int -> x * 2 }
                .filter { it > 5 }
                .flatMap { Option.Some(it.toString()) }
                .getOrElse { "default" }

            result shouldBe "default"
        }

        test("filter can produce Option.None mid-chain") {
            val result = Option.Some(5)
                .filter { it > 10 }
                .map { it * 2 }
                .getOrElse { 0 }

            result shouldBe 0
        }

        context("map2") {

            test("Some + Option.Some applies function") {
                checkAll(Arb.int(), Arb.string()) { x, s ->
                    map2(Option.Some(x), Option.Some(s)) { a, b -> "$a: $b" } shouldBe Option.Some("$x: $s")
                }
            }

            test("None + Option.Some == Option.None") {
                checkAll(Arb.string()) { s ->
                    map2(Option.None, Option.Some(s)) { a: Int, b -> "$a: $b" } shouldBe Option.None
                }
            }

            test("Some + Option.None == Option.None") {
                checkAll(Arb.int()) { x ->
                    map2(Option.Some(x), Option.None) { a, b: String -> "$a: $b" } shouldBe Option.None
                }
            }

            test("None + Option.None == Option.None") {
                map2(Option.None, Option.None) { a: Int, b: String -> "$a: $b" } shouldBe Option.None
            }
        }
    }

    context("sequence") {

        test("all Option.Some values -> Option.Some(list)") {
            checkAll(Arb.list(Arb.int(), 0..20)) { xs ->
                val options = xs.map { Option.Some(it) }
                sequence(options) shouldBe Option.Some(xs)
            }
        }

        test("any Option.None -> Option.None") {
            checkAll(Arb.list(Arb.int(), 1..20), Arb.int(0..19)) { xs, noneIdx ->
                val options = xs.mapIndexed { i, x ->
                    if (i == noneIdx % xs.size) Option.None else Option.Some(x)
                }
                sequence(options) shouldBe Option.None
            }
        }

        test("empty list -> Option.Some(emptyList)") {
            sequence(emptyList<Option<Int>>()) shouldBe Option.Some(emptyList())
        }

        test("list of all Option.None -> Option.None") {
            val options = List(5) { Option.None }
            sequence(options) shouldBe Option.None
        }

        test("preserves order") {
            val options = listOf(Option.Some(1), Option.Some(2), Option.Some(3))
            sequence(options) shouldBe Option.Some(listOf(1, 2, 3))
        }
    }

    context("traverse") {

        test("all succeed -> Option.Some(list of results)") {
            checkAll(Arb.list(Arb.int(), 0..50)) { xs ->
                traverse(xs) { Option.Some(it * 2) } shouldBe Option.Some(xs.map { it * 2 })
            }
        }

        test("any failure -> Option.None") {
            val result = traverse(listOf(1, 2, 3, 4)) { x ->
                if (x == 3) Option.None else Option.Some(x * 2)
            }
            result shouldBe Option.None
        }

        test("empty list -> Option.Some(emptyList)") {
            traverse(emptyList<Int>()) { Option.Some(it) } shouldBe Option.Some(emptyList())
        }

        test("short-circuits on first Option.None") {
            var count = 0
            traverse(listOf(1, 2, 3, 4)) { x ->
                count++
                if (x == 2) Option.None else Option.Some(x)
            }
            count shouldBe 2  // Should stop after hitting Option.None
        }

        test("preserves order") {
            traverse(listOf(1, 2, 3)) { Option.Some(it.toString()) } shouldBe
                    Option.Some(listOf("1", "2", "3"))
        }

        test("traverse with catches") {
            val strings = listOf("1", "2", "not a number", "4")
            val result = traverse(strings) { catches { it.toInt() } }
            result shouldBe Option.None
        }

        test("traverse is sequence ∘ map") {
            checkAll(Arb.list(Arb.int(), 0..20)) { xs ->
                val f: (Int) -> Option<String> = { Option.Some(it.toString()) }
                traverse(xs, f) shouldBe sequence(xs.map(f))
            }
        }
    }

    context("catches and catchesAll") {

        test("successful computation returns Option.Some") {
            checkAll(Arb.int()) { x ->
                catches { x * 2 } shouldBe Option.Some(x * 2)
            }
        }

        test("throwing computation returns Option.None") {
            val result = catches<Int> {
                throw RuntimeException("error")
            }
            result shouldBe Option.None
        }

        test("catches ArithmeticException") {
            val result = catches { 1 / 0 }
            result shouldBe Option.None
        }

        test("catches NullPointerException") {
            val result = catches {
                val s: String? = null
                s!!.length
            }
            result shouldBe Option.None
        }

        test("catches IllegalArgumentException") {
            val result = catches {
                require(false) { "invalid" }
                42
            }
            result shouldBe Option.None
        }

        test("catches custom exceptions") {
            class CustomException : Exception()

            val result = catches<Int> {
                throw CustomException()
            }
            result shouldBe Option.None
        }

        test("computation is lazy - not evaluated until called") {
            var evaluated = false
            val computation = {
                evaluated = true
                42
            }

            // Just creating the lambda shouldn't evaluate it
            evaluated shouldBe false

            // Now call catches
            catches(computation)
            evaluated shouldBe true
        }

        test("doesn't catch if no exception thrown") {
            var sideEffect = 0
            val result = catches {
                sideEffect = 42
                100
            }

            result shouldBe Option.Some(100)
            sideEffect shouldBe 42
        }

        test("handles null values correctly") {
            val result = catches<String?> { null }
            result shouldBe Option.Some(null)
        }

        test("works with complex computations") {
            data class Person(val name: String, val age: Int)

            val result = catches {
                Person("Alice", 30).copy(age = 31)
            }

            result shouldBe Option.Some(Person("Alice", 31))
        }

        test("catches errors (not just exceptions)") {
            val result = catchesAll<Int> {
                throw OutOfMemoryError("simulated")
            }
            result shouldBe Option.None
        }

        test("sequence of catches - all succeed") {
            val computations = listOf(
                { 1 },
                { 2 },
                { 3 }
            )

            val results = computations.map { catches(it) }
            sequence(results) shouldBe Option.Some(listOf(1, 2, 3))
        }

        test("sequence of catches - one fails") {
            val computations = listOf(
                { 1 },
                { throw Exception("fail") },
                { 3 }
            )

            val results = computations.map { catches(it) }
            sequence(results) shouldBe Option.None
        }

        test("catches with side effects are idempotent") {
            var counter = 0
            val computation = {
                counter++
                counter
            }

            val result1 = catches(computation)
            val result2 = catches(computation)

            result1 shouldBe Option.Some(1)
            result2 shouldBe Option.Some(2)
        }

        test("can be composed with other Option operations") {
            val result = catches { "42" }
                .map { it.toInt() }
                .filter { it > 40 }
                .map { it * 2 }

            result shouldBe Option.Some(84)
        }

        test("chaining catches with flatMap") {
            val result = catches { 10 }
                .flatMap { x -> catches { x / 2 } }
                .flatMap { y -> catches { y * 3 } }

            result shouldBe Option.Some(15)
        }

        test("catches in flatMap chain short-circuits") {
            var secondEvaluated = false

            val result = catches<Int> { throw Exception() }
                .flatMap {
                    secondEvaluated = true
                    catches { it * 2 }
                }

            result shouldBe Option.None
            secondEvaluated shouldBe false
        }
    }

    context("catches integration with real-world scenarios") {

        test("parsing integers safely") {
            fun safeParseInt(s: String): Option<Int> = catches { s.toInt() }

            safeParseInt("42") shouldBe Option.Some(42)
            safeParseInt("not a number") shouldBe Option.None
            safeParseInt("") shouldBe Option.None
        }

        test("array access safely") {
            fun <T> safeGet(list: List<T>, index: Int): Option<T> =
                catches { list[index] }

            val list = listOf(1, 2, 3)
            safeGet(list, 1) shouldBe Option.Some(2)
            safeGet(list, 10) shouldBe Option.None
            safeGet(list, -1) shouldBe Option.None
        }

        test("map lookup safely") {
            fun <K, V> safeMapGet(map: Map<K, V>, key: K): Option<V> =
                catches { map.getValue(key) }  // throws if key not present

            val map = mapOf("a" to 1, "b" to 2)
            safeMapGet(map, "a") shouldBe Option.Some(1)
            safeMapGet(map, "c") shouldBe Option.None
        }

        test("division by zero") {
            fun safeDivide(a: Int, b: Int): Option<Int> = catches { a / b }

            safeDivide(10, 2) shouldBe Option.Some(5)
            safeDivide(10, 0) shouldBe Option.None
        }
    }
})
