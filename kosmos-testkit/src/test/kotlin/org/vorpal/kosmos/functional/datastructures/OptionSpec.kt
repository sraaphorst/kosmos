package org.vorpal.kosmos.functional.datastructures

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.vorpal.kosmos.functional.datastructures.Options.catches
import org.vorpal.kosmos.functional.datastructures.Options.map2
import org.vorpal.kosmos.functional.datastructures.Options.sequence
import org.vorpal.kosmos.functional.datastructures.Options.traverse

class OptionSpec : FunSpec({
    // ============ Functor Laws ============

    context("Functor Laws") {

        test("Identity: map(id) == id") {
            checkAll(arbIntOption) { opt ->
                opt.map { it } shouldBe opt
            }
        }

        test("Composition: map(f).map(g) == map(g ∘ f)") {
            checkAll(arbIntOption, Arb.int(), Arb.int()) { opt, x, y ->
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
                val f: (Int) -> Option<String> = { Some((it * 2).toString()) }

                Some(a).flatMap(f) shouldBe f(a)
            }
        }

        test("Right Identity: m.flatMap(pure) == m") {
            checkAll(arbIntOption) { opt ->
                opt.flatMap { Some(it) } shouldBe opt
            }
        }

        test("Associativity: m.flatMap(f).flatMap(g) == m.flatMap { f(it).flatMap(g) }") {
            checkAll(arbIntOption, Arb.int(), Arb.int()) { opt, x, y ->
                val f: (Int) -> Option<Int> = { Some(it + x) }
                val g: (Int) -> Option<String> = { Some((it * y).toString()) }

                opt.flatMap(f).flatMap(g) shouldBe opt.flatMap { f(it).flatMap(g) }
            }
        }
    }

    // ============ map ============

    context("map") {

        test("Some(x).map(f) == Some(f(x))") {
            checkAll(Arb.int()) { x ->
                val f: (Int) -> String = { (it * 2).toString() }
                Some(x).map(f) shouldBe Some(f(x))
            }
        }

        test("None.map(f) == None") {
            val f: (Int) -> String = { it.toString() }
            None.map(f) shouldBe None
        }
    }

    // ============ flatMap ============

    context("flatMap") {

        test("Some(x).flatMap(f) == f(x)") {
            checkAll(Arb.int()) { x ->
                val f: (Int) -> Option<String> = { Some((it * 2).toString()) }
                Some(x).flatMap(f) shouldBe f(x)
            }
        }

        test("None.flatMap(f) == None") {
            val f: (Int) -> Option<String> = { Some(it.toString()) }
            None.flatMap(f) shouldBe None
        }

        test("flatMap short-circuits on None from function") {
            checkAll(Arb.int()) { x ->
                val f: (Int) -> Option<String> = { None }
                Some(x).flatMap(f) shouldBe None
            }
        }
    }

    // ============ flatten ============

    context("flatten") {

        test("Some(Some(x)).flatten() == Some(x)") {
            checkAll(Arb.int()) { x ->
                val nested: Option<Option<Int>> = Some(Some(x))
                nested.flatten() shouldBe Some(x)
            }
        }

        test("Some(None).flatten() == None") {
            val nested: Option<Option<Int>> = Some(None)
            nested.flatten() shouldBe None
        }

        test("None.flatten() == None") {
            val nested: Option<Option<Int>> = None
            nested.flatten() shouldBe None
        }

        test("flatten is equivalent to flatMap(identity)") {
            checkAll(Arb.option(arbIntOption)) { nested ->
                nested.flatten() shouldBe nested.flatMap { it }
            }
        }
    }

    // ============ getOrElse ============

    context("getOrElse") {

        test("Some(x).getOrElse(default) == x") {
            checkAll(Arb.int(), Arb.int()) { x, default ->
                Some(x).getOrElse { default } shouldBe x
            }
        }

        test("None.getOrElse(default) == default") {
            checkAll(Arb.int()) { default ->
                None.getOrElse { default } shouldBe default
            }
        }

        test("default is lazy (not evaluated for Some)") {
            var evaluated = false
            Some(42).getOrElse {
                evaluated = true
                0
            }
            evaluated shouldBe false
        }

        test("default is evaluated for None") {
            var evaluated = false
            None.getOrElse {
                evaluated = true
                0
            }
            evaluated shouldBe true
        }
    }

    // ============ orElse ============

    context("orElse") {

        test("Some(x).orElse(other) == Some(x)") {
            checkAll(Arb.int(), arbIntOption) { x, other ->
                Some(x).orElse(other) shouldBe Some(x)
            }
        }

        test("None.orElse(other) == other") {
            checkAll(arbIntOption) { other ->
                None.orElse(other) shouldBe other
            }
        }

        test("orElse chains correctly") {
            None.orElse(None).orElse(Some(42)) shouldBe Some(42)
        }
    }

    // ============ isPresent ============

    context("isPresent") {

        test("Some(x).isPresent() == true") {
            checkAll(Arb.int()) { x ->
                Some(x).isPresent() shouldBe true
            }
        }

        test("None.isPresent() == false") {
            None.isPresent() shouldBe false
        }
    }

    // ============ filter ============

    context("filter") {

        test("Some(x).filter(p) == Some(x) when p(x) is true") {
            checkAll(Arb.int()) { x ->
                Some(x).filter { it == x } shouldBe Some(x)
            }
        }

        test("Some(x).filter(p) == None when p(x) is false") {
            checkAll(Arb.int()) { x ->
                Some(x).filter { false } shouldBe None
            }
        }

        test("None.filter(p) == None") {
            None.filter { _: Int -> true } shouldBe None
        }

        test("filter with always-true predicate is identity") {
            checkAll(arbIntOption) { opt ->
                opt.filter { true } shouldBe opt
            }
        }

        test("filter with always-false predicate yields None") {
            checkAll(arbIntOption) { opt ->
                val result = opt.filter { false }
                result shouldBe None
            }
        }

        test("filter composition") {
            checkAll(Arb.some(Arb.int(0..100))) { opt ->
                val p1: (Int) -> Boolean = { it > 25 }
                val p2: (Int) -> Boolean = { it < 75 }

                opt.filter(p1).filter(p2) shouldBe opt.filter { p1(it) && p2(it) }
            }
        }
    }

    // ============ Integration Tests ============

    context("Integration") {

        test("chaining operations") {
            val result = Some(5)
                .map { it * 2 }
                .filter { it > 5 }
                .flatMap { Some(it.toString()) }
                .getOrElse { "default" }

            result shouldBe "10"
        }

        test("short-circuiting on None") {
            val result = None
                .map { x: Int -> x * 2 }
                .filter { it > 5 }
                .flatMap { Some(it.toString()) }
                .getOrElse { "default" }

            result shouldBe "default"
        }

        test("filter can produce None mid-chain") {
            val result = Some(5)
                .filter { it > 10 }
                .map { it * 2 }
                .getOrElse { 0 }

            result shouldBe 0
        }

        context("map2") {

            test("Some + Some applies function") {
                checkAll(Arb.int(), Arb.string()) { x, s ->
                    map2(Some(x), Some(s)) { a, b -> "$a: $b" } shouldBe Some("$x: $s")
                }
            }

            test("None + Some == None") {
                checkAll(Arb.string()) { s ->
                    map2(None, Some(s)) { a: Int, b -> "$a: $b" } shouldBe None
                }
            }

            test("Some + None == None") {
                checkAll(Arb.int()) { x ->
                    map2(Some(x), None) { a, b: String -> "$a: $b" } shouldBe None
                }
            }

            test("None + None == None") {
                map2(None, None) { a: Int, b: String -> "$a: $b" } shouldBe None
            }
        }
    }

    context("sequence") {

        test("all Some values -> Some(list)") {
            checkAll(Arb.list(Arb.int(), 0..20)) { xs ->
                val options = xs.map { Some(it) }
                sequence(options) shouldBe Some(xs)
            }
        }

        test("any None -> None") {
            checkAll(Arb.list(Arb.int(), 1..20), Arb.int(0..19)) { xs, noneIdx ->
                val options = xs.mapIndexed { i, x ->
                    if (i == noneIdx % xs.size) None else Some(x)
                }
                sequence(options) shouldBe None
            }
        }

        test("empty list -> Some(emptyList)") {
            sequence(emptyList<Option<Int>>()) shouldBe Some(emptyList<Int>())
        }

        test("list of all None -> None") {
            val options = List(5) { None }
            sequence(options) shouldBe None
        }

        test("preserves order") {
            val options = listOf(Some(1), Some(2), Some(3))
            sequence(options) shouldBe Some(listOf(1, 2, 3))
        }
    }

    context("traverse") {

        test("all succeed -> Some(list of results)") {
            checkAll(Arb.list(Arb.int(), 0..50)) { xs ->
                traverse(xs) { Some(it * 2) } shouldBe Some(xs.map { it * 2 })
            }
        }

        test("any failure -> None") {
            val result = traverse(listOf(1, 2, 3, 4)) { x ->
                if (x == 3) None else Some(x * 2)
            }
            result shouldBe None
        }

        test("empty list -> Some(emptyList)") {
            traverse(emptyList<Int>()) { Some(it) } shouldBe Some(emptyList())
        }

        test("short-circuits on first None") {
            var count = 0
            traverse(listOf(1, 2, 3, 4)) { x ->
                count++
                if (x == 2) None else Some(x)
            }
            count shouldBe 2  // Should stop after hitting None
        }

        test("preserves order") {
            traverse(listOf(1, 2, 3)) { Some(it.toString()) } shouldBe
                    Some(listOf("1", "2", "3"))
        }

        test("traverse with catches") {
            val strings = listOf("1", "2", "not a number", "4")
            val result = traverse(strings) { catches { it.toInt() } }
            result shouldBe None
        }

        test("traverse is sequence ∘ map") {
            checkAll(Arb.list(Arb.int(), 0..20)) { xs ->
                val f: (Int) -> Option<String> = { Some(it.toString()) }
                traverse(xs, f) shouldBe sequence(xs.map(f))
            }
        }
    }

    context("catches") {

        test("successful computation returns Some") {
            checkAll(Arb.int()) { x ->
                catches { x * 2 } shouldBe Some(x * 2)
            }
        }

        test("throwing computation returns None") {
            val result = catches<Int> {
                throw RuntimeException("error")
            }
            result shouldBe None
        }

        test("catches ArithmeticException") {
            val result = catches { 1 / 0 }
            result shouldBe None
        }

        test("catches NullPointerException") {
            val result = catches {
                val s: String? = null
                s!!.length
            }
            result shouldBe None
        }

        test("catches IllegalArgumentException") {
            val result = catches {
                require(false) { "invalid" }
                42
            }
            result shouldBe None
        }

        test("catches custom exceptions") {
            class CustomException : Exception()

            val result = catches<Int> {
                throw CustomException()
            }
            result shouldBe None
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

            result shouldBe Some(100)
            sideEffect shouldBe 42
        }

        test("handles null values correctly") {
            val result = catches<String?> { null }
            result shouldBe Some(null)
        }

        test("works with complex computations") {
            data class Person(val name: String, val age: Int)

            val result = catches {
                Person("Alice", 30).copy(age = 31)
            }

            result shouldBe Some(Person("Alice", 31))
        }

        test("catches errors (not just exceptions)") {
            val result = catches<Int> {
                throw OutOfMemoryError("simulated")
            }
            result shouldBe None
        }

        test("sequence of catches - all succeed") {
            val computations = listOf(
                { 1 },
                { 2 },
                { 3 }
            )

            val results = computations.map { catches(it) }
            sequence(results) shouldBe Some(listOf(1, 2, 3))
        }

        test("sequence of catches - one fails") {
            val computations = listOf<() -> Int>(
                { 1 },
                { throw Exception("fail") },
                { 3 }
            )

            val results = computations.map { catches(it) }
            sequence(results) shouldBe None
        }

        test("catches with side effects are idempotent") {
            var counter = 0
            val computation = {
                counter++
                counter
            }

            val result1 = catches(computation)
            val result2 = catches(computation)

            result1 shouldBe Some(1)
            result2 shouldBe Some(2)
        }

        test("can be composed with other Option operations") {
            val result = catches { "42" }
                .map { it.toInt() }
                .filter { it > 40 }
                .map { it * 2 }

            result shouldBe Some(84)
        }

        test("chaining catches with flatMap") {
            val result = catches { 10 }
                .flatMap { x -> catches { x / 2 } }
                .flatMap { y -> catches { y * 3 } }

            result shouldBe Some(15)
        }

        test("catches in flatMap chain short-circuits") {
            var secondEvaluated = false

            val result = catches<Int> { throw Exception() }
                .flatMap {
                    secondEvaluated = true
                    catches { it * 2 }
                }

            result shouldBe None
            secondEvaluated shouldBe false
        }
    }

    context("catches integration with real-world scenarios") {

        test("parsing integers safely") {
            fun safeParseInt(s: String): Option<Int> = catches { s.toInt() }

            safeParseInt("42") shouldBe Some(42)
            safeParseInt("not a number") shouldBe None
            safeParseInt("") shouldBe None
        }

        test("array access safely") {
            fun <T> safeGet(list: List<T>, index: Int): Option<T> =
                catches { list[index] }

            val list = listOf(1, 2, 3)
            safeGet(list, 1) shouldBe Some(2)
            safeGet(list, 10) shouldBe None
            safeGet(list, -1) shouldBe None
        }

        test("map lookup safely") {
            fun <K, V> safeMapGet(map: Map<K, V>, key: K): Option<V> =
                catches { map.getValue(key) }  // throws if key not present

            val map = mapOf("a" to 1, "b" to 2)
            safeMapGet(map, "a") shouldBe Some(1)
            safeMapGet(map, "c") shouldBe None
        }

        test("division by zero") {
            fun safeDivide(a: Int, b: Int): Option<Int> = catches { a / b }

            safeDivide(10, 2) shouldBe Some(5)
            safeDivide(10, 0) shouldBe None
        }
    }
})
