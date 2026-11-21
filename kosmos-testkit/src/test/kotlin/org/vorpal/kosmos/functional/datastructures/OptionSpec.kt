package org.vorpal.kosmos.functional.datastructures

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import org.vorpal.kosmos.functional.datastructures.Options.catches
import org.vorpal.kosmos.functional.datastructures.Options.catchesAll
import org.vorpal.kosmos.functional.datastructures.Options.map2
import org.vorpal.kosmos.functional.datastructures.Options.sequence
import org.vorpal.kosmos.functional.datastructures.Options.traverse
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class OptionSpec : FunSpec({
    lawTests(lawGraph("Option") {
        // ============ Functor Laws ============

        branch("Functor Laws") {
            local("Identity: map(id) == id") {
                lawCheckAll(ArbOption.arbIntOption) { opt ->
                    opt.map { it } shouldBe opt
                }
            }

            local("Composition: map(f).map(g) == map(g ∘ f)") {
                lawCheckAll(ArbOption.arbIntOption, Arb.int(), Arb.int()) { opt, x, y ->
                    val f: (Int) -> Int = { it + x }
                    val g: (Int) -> String = { (it * y).toString() }

                    opt.map(f).map(g) shouldBe opt.map { g(f(it)) }
                }
            }
        }

        // ============ Monad Laws ============

        branch("Monad Laws") {
            local("Left Identity: pure(a).flatMap(f) == f(a)") {
                lawCheckAll(Arb.int()) { a ->
                    val f: (Int) -> Option<String> = { Option.Some((it * 2).toString()) }

                    Option.Some(a).flatMap(f) shouldBe f(a)
                }
            }

            local("Right Identity: m.flatMap(pure) == m") {
                lawCheckAll(ArbOption.arbIntOption) { opt ->
                    opt.flatMap { Option.Some(it) } shouldBe opt
                }
            }

            local("Associativity: m.flatMap(f).flatMap(g) == m.flatMap { f(it).flatMap(g) }") {
                lawCheckAll(ArbOption.arbIntOption, Arb.int(), Arb.int()) { opt, x, y ->
                    val f: (Int) -> Option<Int> = { Option.Some(it + x) }
                    val g: (Int) -> Option<String> = { Option.Some((it * y).toString()) }

                    opt.flatMap(f).flatMap(g) shouldBe opt.flatMap { f(it).flatMap(g) }
                }
            }
        }

        // ============ map ============

        branch("map") {
            local("Some(x).map(f) == Option.Some(f(x))") {
                lawCheckAll(Arb.int()) { x ->
                    val f: (Int) -> String = { (it * 2).toString() }
                    Option.Some(x).map(f) shouldBe Option.Some(f(x))
                }
            }

            local("None.map(f) == Option.None") {
                val f: (Int) -> String = { it.toString() }
                Option.None.map(f) shouldBe Option.None
            }
        }

        // ============ flatMap ============

        branch("flatMap") {
            local("Some(x).flatMap(f) == f(x)") {
                lawCheckAll(Arb.int()) { x ->
                    val f: (Int) -> Option<String> = { Option.Some((it * 2).toString()) }
                    Option.Some(x).flatMap(f) shouldBe f(x)
                }
            }

            local("None.flatMap(f) == Option.None") {
                val f: (Int) -> Option<String> = { Option.Some(it.toString()) }
                Option.None.flatMap(f) shouldBe Option.None
            }

            local("flatMap short-circuits on Option.None from function") {
                lawCheckAll(Arb.int()) { x ->
                    val f: (Int) -> Option<String> = { Option.None }
                    Option.Some(x).flatMap(f) shouldBe Option.None
                }
            }
        }

        // ============ flatten ============

        branch("flatten") {
            local("Some(Some(x)).flatten() == Option.Some(x)") {
                lawCheckAll(Arb.int()) { x ->
                    val nested: Option<Option<Int>> = Option.Some(Option.Some(x))
                    nested.flatten() shouldBe Option.Some(x)
                }
            }

            local("Some(None).flatten() == Option.None") {
                val nested: Option<Option<Int>> = Option.Some(Option.None)
                nested.flatten() shouldBe Option.None
            }

            local("None.flatten() == Option.None") {
                val nested: Option<Option<Int>> = Option.None
                nested.flatten() shouldBe Option.None
            }

            local("flatten is equivalent to flatMap(identity)") {
                lawCheckAll(ArbOption.option(ArbOption.arbIntOption)) { nested ->
                    nested.flatten() shouldBe nested.flatMap { it }
                }
            }
        }

        // ============ getOrElse ============

        branch("getOrElse") {
            local("Some(x).getOrElse(default) == x") {
                lawCheckAll(Arb.int(), Arb.int()) { x, default ->
                    Option.Some(x).getOrElse { default } shouldBe x
                }
            }

            local("None.getOrElse(default) == default") {
                lawCheckAll(Arb.int()) { default ->
                    Option.None.getOrElse { default } shouldBe default
                }
            }

            local("default is lazy (not evaluated for Option.Some)") {
                var evaluated = false
                Option.Some(42).getOrElse {
                    evaluated = true
                    0
                }
                evaluated shouldBe false
            }

            local("default is evaluated for Option.None") {
                var evaluated = false
                Option.None.getOrElse {
                    evaluated = true
                    0
                }
                evaluated shouldBe true
            }
        }

        // ============ orElse ============

        branch("orElse") {
            local("Some(x).orElse(other) == Option.Some(x)") {
                lawCheckAll(Arb.int(), ArbOption.arbIntOption) { x, other ->
                    Option.Some(x).orElse(other) shouldBe Option.Some(x)
                }
            }

            local("None.orElse(other) == other") {
                lawCheckAll(ArbOption.arbIntOption) { other ->
                    Option.None.orElse(other) shouldBe other
                }
            }

            local("orElse chains correctly") {
                Option.None
                    .orElse(Option.None)
                    .orElse(Option.Some(42)) shouldBe Option.Some(42)
            }
        }

        // ============ isPresent ============

        branch("isPresent") {
            local("Some(x).isPresent() == true") {
                lawCheckAll(Arb.int()) { x ->
                    Option.Some(x).isNonEmpty() shouldBe true
                }
            }

            local("None.isPresent() == false") {
                Option.None.isNonEmpty() shouldBe false
            }
        }

        // ============ filter ============

        branch("filter") {
            local("Some(x).filter(p) == Option.Some(x) when p(x) is true") {
                lawCheckAll(Arb.int()) { x ->
                    Option.Some(x).filter { it == x } shouldBe Option.Some(x)
                }
            }

            local("Some(x).filter(p) == Option.None when p(x) is false") {
                lawCheckAll(Arb.int()) { x ->
                    Option.Some(x).filter { false } shouldBe Option.None
                }
            }

            local("None.filter(p) == Option.None") {
                Option.None.filter { _: Int -> true } shouldBe Option.None
            }

            local("filter with always-true predicate is identity") {
                lawCheckAll(ArbOption.arbIntOption) { opt ->
                    opt.filter { true } shouldBe opt
                }
            }

            local("filter with always-false predicate yields Option.None") {
                lawCheckAll(ArbOption.arbIntOption) { opt ->
                    val result = opt.filter { false }
                    result shouldBe Option.None
                }
            }

            local("filter composition") {
                lawCheckAll(ArbOption.some(Arb.int(0..100))) { opt: Option<Int> ->
                    val p1: (Int) -> Boolean = { it > 25 }
                    val p2: (Int) -> Boolean = { it < 75 }

                    opt.filter(p1)
                        .filter(p2) shouldBe opt.filter { p1(it) && p2(it) }
                }
            }
        }

        // ============ Integration Tests ============

        branch("Integration") {
            local("chaining operations") {
                val result = Option.Some(5)
                    .map { it * 2 }
                    .filter { it > 5 }
                    .flatMap { Option.Some(it.toString()) }
                    .getOrElse { "default" }

                result shouldBe "10"
            }

            local("short-circuiting on Option.None") {
                val result = Option.None
                    .map { x: Int -> x * 2 }
                    .filter { it > 5 }
                    .flatMap { Option.Some(it.toString()) }
                    .getOrElse { "default" }

                result shouldBe "default"
            }

            local("filter can produce Option.None mid-chain") {
                val result = Option.Some(5)
                    .filter { it > 10 }
                    .map { it * 2 }
                    .getOrElse { 0 }

                result shouldBe 0
            }

            branch("map2") {
                local("Some + Option.Some applies function") {
                    lawCheckAll(Arb.int(), Arb.string()) { x, s ->
                        map2(Option.Some(x), Option.Some(s)) { a, b -> "$a: $b" } shouldBe Option.Some("$x: $s")
                    }
                }

                local("None + Option.Some == Option.None") {
                    lawCheckAll(Arb.string()) { s ->
                        map2(Option.None, Option.Some(s)) { a: Int, b -> "$a: $b" } shouldBe Option.None
                    }
                }

                local("Some + Option.None == Option.None") {
                    lawCheckAll(Arb.int()) { x ->
                        map2(Option.Some(x), Option.None) { a, b: String -> "$a: $b" } shouldBe Option.None
                    }
                }

                local("None + Option.None == Option.None") {
                    map2(Option.None, Option.None) { a: Int, b: String -> "$a: $b" } shouldBe Option.None
                }
            }
        }

        branch("sequence") {
            local("all Option.Some values -> Option.Some(list)") {
                lawCheckAll(Arb.list(Arb.int(), 0..20)) { xs ->
                    val options = xs.map { Option.Some(it) }
                    sequence(options) shouldBe Option.Some(xs)
                }
            }

            local("any Option.None -> Option.None") {
                lawCheckAll(Arb.list(Arb.int(), 1..20), Arb.int(0..19)) { xs, noneIdx ->
                    val options = xs.mapIndexed { i, x ->
                        if (i == noneIdx % xs.size) Option.None else Option.Some(x)
                    }
                    sequence(options) shouldBe Option.None
                }
            }

            local("empty list -> Option.Some(emptyList)") {
                sequence(emptyList<Option<Int>>()) shouldBe Option.Some(emptyList())
            }

            local("list of all Option.None -> Option.None") {
                val options = List(5) { Option.None }
                sequence(options) shouldBe Option.None
            }

            local("preserves order") {
                val options = listOf(Option.Some(1), Option.Some(2), Option.Some(3))
                sequence(options) shouldBe Option.Some(listOf(1, 2, 3))
            }
        }

        branch("traverse") {
            local("all succeed -> Option.Some(list of results)") {
                lawCheckAll(Arb.list(Arb.int(), 0..50)) { xs ->
                    traverse(xs) { Option.Some(it * 2) } shouldBe Option.Some(xs.map { it * 2 })
                }
            }

            local("any failure -> Option.None") {
                val result = traverse(listOf(1, 2, 3, 4)) { x ->
                    if (x == 3) Option.None else Option.Some(x * 2)
                }
                result shouldBe Option.None
            }

            local("empty list -> Option.Some(emptyList)") {
                traverse(emptyList<Int>()) { Option.Some(it) } shouldBe Option.Some(emptyList())
            }

            local("short-circuits on first Option.None") {
                var count = 0
                traverse(listOf(1, 2, 3, 4)) { x ->
                    count++
                    if (x == 2) Option.None else Option.Some(x)
                }
                count shouldBe 2  // Should stop after hitting Option.None
            }

            local("preserves order") {
                traverse(listOf(1, 2, 3)) { Option.Some(it.toString()) } shouldBe
                        Option.Some(listOf("1", "2", "3"))
            }

            local("traverse with catches") {
                val strings = listOf("1", "2", "not a number", "4")
                val result = traverse(strings) { catches { it.toInt() } }
                result shouldBe Option.None
            }

            local("traverse is sequence ∘ map") {
                lawCheckAll(Arb.list(Arb.int(), 0..20)) { xs ->
                    val f: (Int) -> Option<String> = { Option.Some(it.toString()) }
                    traverse(xs, f) shouldBe sequence(xs.map(f))
                }
            }
        }

        branch("catches and catchesAll") {
            local("successful computation returns Option.Some") {
                lawCheckAll(Arb.int()) { x ->
                    catches { x * 2 } shouldBe Option.Some(x * 2)
                }
            }

            local("throwing computation returns Option.None") {
                val result = catches<Int> {
                    throw RuntimeException("error")
                }
                result shouldBe Option.None
            }

            local("catches ArithmeticException") {
                val result = catches { 1 / 0 }
                result shouldBe Option.None
            }

            local("catches NullPointerException") {
                val result = catches {
                    val s: String? = null
                    s!!.length
                }
                result shouldBe Option.None
            }

            local("catches IllegalArgumentException") {
                val result = catches {
                    require(false) { "invalid" }
                    42
                }
                result shouldBe Option.None
            }

            local("catches custom exceptions") {
                class CustomException : Exception()

                val result = catches<Int> {
                    throw CustomException()
                }
                result shouldBe Option.None
            }

            local("computation is lazy - not evaluated until called") {
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

            local("doesn't catch if no exception thrown") {
                var sideEffect = 0
                val result = catches {
                    sideEffect = 42
                    100
                }

                result shouldBe Option.Some(100)
                sideEffect shouldBe 42
            }

            local("handles null values correctly") {
                val result = catches<String?> { null }
                result shouldBe Option.Some(null)
            }

            local("works with complex computations") {
                data class Person(val name: String, val age: Int)

                val result = catches {
                    Person("Alice", 30).copy(age = 31)
                }

                result shouldBe Option.Some(Person("Alice", 31))
            }

            local("catches errors (not just exceptions)") {
                val result = catchesAll<Int> {
                    throw OutOfMemoryError("simulated")
                }
                result shouldBe Option.None
            }

            local("sequence of catches - all succeed") {
                val computations = listOf(
                    { 1 },
                    { 2 },
                    { 3 }
                )

                val results = computations.map { catches(it) }
                sequence(results) shouldBe Option.Some(listOf(1, 2, 3))
            }

            local("sequence of catches - one fails") {
                val computations = listOf(
                    { 1 },
                    { throw Exception("fail") },
                    { 3 }
                )

                val results = computations.map { catches(it) }
                sequence(results) shouldBe Option.None
            }

            local("catches with side effects are idempotent") {
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

            local("can be composed with other Option operations") {
                val result = catches { "42" }
                    .map { it.toInt() }
                    .filter { it > 40 }
                    .map { it * 2 }

                result shouldBe Option.Some(84)
            }

            local("chaining catches with flatMap") {
                val result = catches { 10 }
                    .flatMap { x -> catches { x / 2 } }
                    .flatMap { y -> catches { y * 3 } }

                result shouldBe Option.Some(15)
            }

            local("catches in flatMap chain short-circuits") {
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

        branch("catches integration with real-world scenarios") {
            local("parsing integers safely") {
                fun safeParseInt(s: String): Option<Int> = catches { s.toInt() }

                safeParseInt("42") shouldBe Option.Some(42)
                safeParseInt("not a number") shouldBe Option.None
                safeParseInt("") shouldBe Option.None
            }

            local("array access safely") {
                fun <T> safeGet(list: List<T>, index: Int): Option<T> =
                    catches { list[index] }

                val list = listOf(1, 2, 3)
                safeGet(list, 1) shouldBe Option.Some(2)
                safeGet(list, 10) shouldBe Option.None
                safeGet(list, -1) shouldBe Option.None
            }

            local("map lookup safely") {
                fun <K, V> safeMapGet(map: Map<K, V>, key: K): Option<V> =
                    catches { map.getValue(key) }  // throws if key not present

                val map = mapOf("a" to 1, "b" to 2)
                safeMapGet(map, "a") shouldBe Option.Some(1)
                safeMapGet(map, "c") shouldBe Option.None
            }

            local("division by zero") {
                fun safeDivide(a: Int, b: Int): Option<Int> = catches { a / b }

                safeDivide(10, 2) shouldBe Option.Some(5)
                safeDivide(10, 0) shouldBe Option.None
            }
        }
    })
})
