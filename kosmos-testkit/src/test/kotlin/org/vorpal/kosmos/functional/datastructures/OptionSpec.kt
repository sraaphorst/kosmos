package org.vorpal.kosmos.functional.datastructures

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

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
    }
})
