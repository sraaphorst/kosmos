package org.vorpal.kosmos.functional.datastructures

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class EitherSpec : FunSpec({
    lawTests(lawGraph("Either") {
        branch("Either construction") {
            local("Left construction") {
                val left = Either.Left("error")
                left.shouldBeInstanceOf<Either.Left<String>>()
                left.value shouldBe "error"
            }

            local("Right construction") {
                val right = Either.Right(42)
                right.shouldBeInstanceOf<Either.Right<Int>>()
                right.value shouldBe 42
            }

            local("Companion left") {
                val left = Either.left("error")
                left.shouldBeInstanceOf<Either.Left<String>>()
            }

            local("Companion right") {
                val right = Either.right(42)
                right.shouldBeInstanceOf<Either.Right<Int>>()
            }
        }

        branch("Either predicates") {
            local("isLeft on Left returns true") {
                lawCheckAll(Arb.string()) { s ->
                    Either.Left(s).isLeft() shouldBe true
                }
            }

            local("isLeft on Right returns false") {
                lawCheckAll(Arb.int()) { i ->
                    Either.Right(i).isLeft() shouldBe false
                }
            }

            local("isRight on Right returns true") {
                lawCheckAll(Arb.int()) { i ->
                    Either.Right(i).isRight() shouldBe true
                }
            }

            local("isRight on Left returns false") {
                lawCheckAll(Arb.string()) { s ->
                    Either.Left(s).isRight() shouldBe false
                }
            }
        }

        branch("Functor Laws") {
            local("Identity: map(id) == id") {
                lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                    either.map { it } shouldBe either
                }
            }

            local("Composition: map(f).map(g) == map(g ∘ f)") {
                lawCheckAll(
                    ArbEither.either(Arb.string(), Arb.int()),
                    Arb.int(1..100),
                    Arb.int(1..100)
                ) { either, n, m ->
                    val f: (Int) -> Int = { it + n }
                    val g: (Int) -> Int = { it * m }

                    either.map(f).map(g) shouldBe either.map { g(f(it)) }
                }
            }

            local("map on Left is no-op") {
                lawCheckAll(Arb.string(), Arb.int()) { error, n ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.map { it + n } shouldBe left
                }
            }

            local("map on Right applies function") {
                lawCheckAll(Arb.int(), Arb.int()) { value, n ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.map { it + n } shouldBe Either.Right(value + n)
                }
            }
        }

        branch("Monad Laws") {
            local("Left identity: right(a).flatMap(f) == f(a)") {
                lawCheckAll(Arb.int()) { n ->
                    val f: (Int) -> Either<String, Int> = { Either.Right(it * 2) }
                    Either.right(n).flatMap(f) shouldBe f(n)
                }
            }

            local("Right identity: m.flatMap(right) == m") {
                lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                    either.flatMap { Either.right(it) } shouldBe either
                }
            }

            local("Associativity: m.flatMap(f).flatMap(g) == m.flatMap { x -> f(x).flatMap(g) }") {
                lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                    val f: (Int) -> Either<String, Int> = { Either.Right(it + 1) }
                    val g: (Int) -> Either<String, Int> = { Either.Right(it * 2) }

                    either.flatMap(f).flatMap(g) shouldBe
                            either.flatMap { x -> f(x).flatMap(g) }
                }
            }

            local("flatMap on Left short-circuits") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    var called = false
                    left.flatMap {
                        called = true
                        Either.Right(it + 1)
                    }
                    called shouldBe false
                }
            }
        }

        branch("mapLeft") {
            local("transforms Left value") {
                lawCheckAll(Arb.string(), Arb.int()) { error, n ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.mapLeft { it.length + n } shouldBe Either.Left(error.length + n)
                }
            }

            local("preserves Right") {
                lawCheckAll(Arb.int(), Arb.string()) { value, _ ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.mapLeft { it.length } shouldBe right
                }
            }
        }

        branch("flatMapLeft") {
            local("transforms Left with function returning Either") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.flatMapLeft { Either.Left(it.length) } shouldBe Either.Left(error.length)
                }
            }

            local("can recover from Left to Right") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.flatMapLeft { Either.Right(42) } shouldBe Either.Right(42)
                }
            }

            local("preserves Right") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.flatMapLeft { Either.Left(it.length) } shouldBe right
                }
            }
        }

        branch("bimap") {
            local("transforms both sides") {
                lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                    val leftF: (String) -> Int = { it.length }
                    val rightF: (Int) -> String = { it.toString() }

                    when (either) {
                        is Either.Left ->
                            either.bimap(leftF, rightF) shouldBe Either.Left(either.value.length)
                        is Either.Right ->
                            either.bimap(leftF, rightF) shouldBe Either.Right(either.value.toString())
                    }
                }
            }

            local("bimap identity is identity") {
                lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                    either.bimap({ it }, { it }) shouldBe either
                }
            }
        }

        branch("orElse") {
            local("Left uses alternative") {
                lawCheckAll(Arb.string(), Arb.int()) { error, value ->
                    val left: Either<String, Int> = Either.Left(error)
                    val alternative = Either.Right(value)
                    left.orElse { alternative } shouldBe alternative
                }
            }

            local("Right ignores alternative") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    var called = false
                    val result = right.orElse {
                        called = true
                        Either.Right(0)
                    }
                    result shouldBe right
                    called shouldBe false
                }
            }
        }

        branch("getOrElse") {
            local("Left returns default") {
                lawCheckAll(Arb.string(), Arb.int()) { error, default ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.getOrElse { default } shouldBe default
                }
            }

            local("Right returns value") {
                lawCheckAll(Arb.int(), Arb.int()) { value, default ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.getOrElse { default } shouldBe value
                }
            }
        }

        branch("getOrNull") {
            local("Left returns null") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.getOrNull() shouldBe null
                }
            }

            local("Right returns value") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.getOrNull() shouldBe value
                }
            }
        }

        branch("getOrThrow") {
            local("Left throws the exception") {
                lawCheckAll(Arb.string()) { message ->
                    val exception = RuntimeException(message)
                    val left: Either<Throwable, Int> = Either.Left(exception)
                    val thrown = shouldThrow<RuntimeException> {
                        left.getOrThrow()
                    }
                    thrown shouldBe exception
                }
            }

            local("Right returns value") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<Throwable, Int> = Either.Right(value)
                    right.getOrThrow() shouldBe value
                }
            }
        }

        branch("fold") {
            local("executes left function on Left") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.fold(
                        ifLeft = { "Error: $it" },
                        ifRight = { "Success: $it" }
                    ) shouldBe "Error: $error"
                }
            }

            local("executes right function on Right") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.fold(
                        ifLeft = { "Error: $it" },
                        ifRight = { "Success: $it" }
                    ) shouldBe "Success: $value"
                }
            }
        }

        branch("filterOrElse") {
            local("Left passes through") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.filterOrElse({ it > 0 }, { "filtered" }) shouldBe left
                }
            }

            local("Right passing predicate is unchanged") {
                lawCheckAll(Arb.int(1..100)) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.filterOrElse({ it > 0 }, { "negative" }) shouldBe right
                }
            }

            local("Right failing predicate becomes Left") {
                lawCheckAll(Arb.int(1..100)) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.filterOrElse(
                        { it < 0 },
                        { "not negative" }
                    ) shouldBe Either.Left("not negative")
                }
            }
        }

        branch("recover") {
            local("Left recovers to Right") {
                lawCheckAll(Arb.string(), Arb.int()) { error, default ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.recover { default } shouldBe Either.Right(default)
                }
            }

            local("Right is unchanged") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.recover { 0 } shouldBe right
                }
            }
        }

        branch("recoverWith") {
            local("Left can recover to Right") {
                lawCheckAll(Arb.string(), Arb.int()) { error, value ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.recoverWith { Either.Right(value) } shouldBe Either.Right(value)
                }
            }

            local("Left can transform to different Left") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.recoverWith {
                        Either.Left(it.length)
                    } shouldBe Either.Left(error.length)
                }
            }

            local("Right is unchanged") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.recoverWith { Either.Right(0) } shouldBe right
                }
            }
        }

        branch("collapse") {
            local("Left collapses to value") {
                lawCheckAll(Arb.int()) { value ->
                    val left: Either<Int, Int> = Either.Left(value)
                    left.collapse() shouldBe value
                }
            }

            local("Right collapses to value") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<Int, Int> = Either.Right(value)
                    right.collapse() shouldBe value
                }
            }
        }

        branch("Side effects") {
            local("onRight executes on Right") {
                lawCheckAll(Arb.int()) { value ->
                    var executed = false
                    var capturedValue = 0
                    val right: Either<String, Int> = Either.Right(value)

                    val result = right.onRight {
                        executed = true
                        capturedValue = it
                    }

                    executed shouldBe true
                    capturedValue shouldBe value
                    result shouldBe right
                }
            }

            local("onRight does not execute on Left") {
                lawCheckAll(Arb.string()) { error ->
                    var executed = false
                    val left: Either<String, Int> = Either.Left(error)

                    val result = left.onRight { executed = true }

                    executed shouldBe false
                    result shouldBe left
                }
            }

            local("onLeft executes on Left") {
                lawCheckAll(Arb.string()) { error ->
                    var executed = false
                    var capturedError = ""
                    val left: Either<String, Int> = Either.Left(error)

                    val result = left.onLeft {
                        executed = true
                        capturedError = it
                    }

                    executed shouldBe true
                    capturedError shouldBe error
                    result shouldBe left
                }
            }

            local("onLeft does not execute on Right") {
                lawCheckAll(Arb.int()) { value ->
                    var executed = false
                    val right: Either<String, Int> = Either.Right(value)

                    val result = right.onLeft { executed = true }

                    executed shouldBe false
                    result shouldBe right
                }
            }

            local("tap executes on both") {
                lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                    var executed = false
                    val result = either.tap { executed = true }

                    executed shouldBe true
                    result shouldBe either
                }
            }
        }

        branch("swap") {
            local("swaps Left to Right") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.swap() shouldBe Either.Right(error)
                }
            }

            local("swaps Right to Left") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.swap() shouldBe Either.Left(value)
                }
            }

            local("swap is involutive (swap twice is identity)") {
                lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                    either.swap().swap() shouldBe either
                }
            }
        }

        branch("Conversions") {
            local("toOption on Right returns Some") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.toOption() shouldBe Option.Some(value)
                }
            }

            local("toOption on Left returns None") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.toOption() shouldBe Option.None
                }
            }

            local("toLeftOption on Left returns Some") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.toLeftOption() shouldBe Option.Some(error)
                }
            }

            local("toLeftOption on Right returns None") {
                lawCheckAll(Arb.int()) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.toLeftOption() shouldBe Option.None
                }
            }
        }

        branch("Applicative operations") {
            local("ap applies function in Right") {
                lawCheckAll(Arb.int(), Arb.int()) { value, n ->
                    val either: Either<String, Int> = Either.Right(value)
                    val f: Either<String, (Int) -> Int> = Either.Right { it + n }

                    either.ap(f) shouldBe Either.Right(value + n)
                }
            }

            local("ap with Left function returns Left") {
                lawCheckAll(Arb.int(), Arb.string()) { value, error ->
                    val either: Either<String, Int> = Either.Right(value)
                    val f: Either<String, (Int) -> Int> = Either.Left(error)

                    either.ap(f) shouldBe Either.Left(error)
                }
            }

            local("ap with Left value returns Left value") {
                lawCheckAll(Arb.string(), Arb.int()) { error, n ->
                    val either: Either<String, Int> = Either.Left(error)
                    val f: Either<String, (Int) -> Int> = Either.Right { it + n }

                    either.ap(f) shouldBe Either.Left(error)
                }
            }
        }

        branch("zip") {
            local("two Rights zip to Pair") {
                lawCheckAll(Arb.int(), Arb.string()) { i, s ->
                    val e1: Either<String, Int> = Either.Right(i)
                    val e2: Either<String, String> = Either.Right(s)

                    e1.zip(e2) shouldBe Either.Right(i to s)
                }
            }

            local("Left and Right zip to Left") {
                lawCheckAll(Arb.string(), Arb.int()) { error, value ->
                    val left: Either<String, Int> = Either.Left(error)
                    val right: Either<String, Int> = Either.Right(value)

                    left.zip(right) shouldBe left
                }
            }

            local("Right and Left zip to Left") {
                lawCheckAll(Arb.int(), Arb.string()) { value, error ->
                    val right: Either<String, Int> = Either.Right(value)
                    val left: Either<String, Int> = Either.Left(error)

                    right.zip(left) shouldBe left
                }
            }
        }

        branch("zipWith") {
            local("combines two Rights with function") {
                lawCheckAll(Arb.int(), Arb.int()) { a, b ->
                    val e1: Either<String, Int> = Either.Right(a)
                    val e2: Either<String, Int> = Either.Right(b)

                    e1.zipWith(e2) { x, y -> x + y } shouldBe Either.Right(a + b)
                }
            }

            local("short-circuits on Left") {
                lawCheckAll(Arb.string(), Arb.int()) { error, value ->
                    val left: Either<String, Int> = Either.Left(error)
                    val right: Either<String, Int> = Either.Right(value)

                    var called = false
                    left.zipWith(right) { x, y ->
                        called = true
                        x + y
                    } shouldBe left
                    called shouldBe false
                }
            }
        }

        branch("flatten") {
            local("flattens nested Right") {
                lawCheckAll(Arb.int()) { value ->
                    val nested: Either<String, Either<String, Int>> =
                        Either.Right(Either.Right(value))
                    nested.flatten() shouldBe Either.Right(value)
                }
            }

            local("flattens Right(Left)") {
                lawCheckAll(Arb.string()) { error ->
                    val nested: Either<String, Either<String, Int>> =
                        Either.Right(Either.Left(error))
                    nested.flatten() shouldBe Either.Left(error)
                }
            }

            local("flattens outer Left") {
                lawCheckAll(Arb.string()) { error ->
                    val nested: Either<String, Either<String, Int>> = Either.Left(error)
                    nested.flatten() shouldBe Either.Left(error)
                }
            }
        }

        branch("ensure, guard, and filter aliases") {
            local("ensure passing predicate preserves Right") {
                lawCheckAll(Arb.int(1..100)) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.ensure({ "negative" }) { it > 0 } shouldBe right
                }
            }

            local("ensure failing predicate converts to Left") {
                lawCheckAll(Arb.int(1..100)) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.ensure({ "not negative" }) { it < 0 } shouldBe
                            Either.Left("not negative")
                }
            }

            local("ensure Left passes through") {
                lawCheckAll(Arb.string()) { error ->
                    val left: Either<String, Int> = Either.Left(error)
                    left.ensure({ "other error" }) { it > 0 } shouldBe left
                }
            }

            local("guard is alias for ensure") {
                lawCheckAll(Arb.int(1..100)) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.guard({ "error" }) { it > 0 } shouldBe
                            right.ensure({ "error" }) { it > 0 }
                }
            }

            local("filter is alias for ensure") {
                lawCheckAll(Arb.int(1..100)) { value ->
                    val right: Either<String, Int> = Either.Right(value)
                    right.filter({ "error" }) { it > 0 } shouldBe
                            right.ensure({ "error" }) { it > 0 }
                }
            }
        }

        branch("liftEither extensions") {
            local("unary function lift") {
                lawCheckAll(Arb.int()) { value ->
                    val double: (Int) -> Int = { it * 2 }
                    val lifted = double.liftEither<String, Int, Int>()

                    lifted(Either.Right(value)) shouldBe Either.Right(value * 2)
                }
            }

            local("unary lift preserves Left") {
                lawCheckAll(Arb.string()) { error ->
                    val double: (Int) -> Int = { it * 2 }
                    val lifted = double.liftEither<String, Int, Int>()
                    val left: Either<String, Int> = Either.Left(error)

                    lifted(left) shouldBe left
                }
            }

            local("binary function lift combines two Rights") {
                lawCheckAll(Arb.int(), Arb.int()) { a, b ->
                    val add: (Int, Int) -> Int = { x, y -> x + y }
                    val lifted = add.liftEither<String, Int, Int, Int>()

                    lifted(Either.Right(a), Either.Right(b)) shouldBe Either.Right(a + b)
                }
            }

            local("binary lift short-circuits on first Left") {
                lawCheckAll(Arb.string(), Arb.int()) { error, value ->
                    val add: (Int, Int) -> Int = { x, y -> x + y }
                    val lifted = add.liftEither<String, Int, Int, Int>()
                    val left: Either<String, Int> = Either.Left(error)
                    val right: Either<String, Int> = Either.Right(value)

                    lifted(left, right) shouldBe left
                }
            }

            local("binary lift short-circuits on second Left") {
                lawCheckAll(Arb.int(), Arb.string()) { value, error ->
                    val add: (Int, Int) -> Int = { x, y -> x + y }
                    val lifted = add.liftEither<String, Int, Int, Int>()
                    val right: Either<String, Int> = Either.Right(value)
                    val left: Either<String, Int> = Either.Left(error)

                    lifted(right, left) shouldBe left
                }
            }

            local("ternary function lift combines three Rights") {
                lawCheckAll(Arb.int(), Arb.int(), Arb.int()) { a, b, c ->
                    val sum: (Int, Int, Int) -> Int = { x, y, z -> x + y + z }
                    val lifted = sum.liftEither<String, Int, Int, Int, Int>()

                    lifted(
                        Either.Right(a),
                        Either.Right(b),
                        Either.Right(c)
                    ) shouldBe Either.Right(a + b + c)
                }
            }

            local("ternary lift short-circuits on any Left") {
                lawCheckAll(Arb.string()) { error ->
                    val sum: (Int, Int, Int) -> Int = { x, y, z -> x + y + z }
                    val lifted = sum.liftEither<String, Int, Int, Int, Int>()
                    val left: Either<String, Int> = Either.Left(error)

                    // Test first position
                    lifted(left, Either.Right(1), Either.Right(2)) shouldBe left

                    // Test second position
                    lifted(Either.Right(1), left, Either.Right(2)) shouldBe left

                    // Test third position
                    lifted(Either.Right(1), Either.Right(2), left) shouldBe left
                }
            }

            local("practical example: validation with lift") {
                data class User(val name: String, val age: Int, val email: String)

                fun validateName(s: String): Either<String, String> =
                    if (s.length >= 3) Either.Right(s)
                    else Either.Left("Name too short")

                fun validateAge(n: Int): Either<String, Int> =
                    if (n >= 18) Either.Right(n)
                    else Either.Left("Must be 18+")

                fun validateEmail(s: String): Either<String, String> =
                    if ("@" in s) Either.Right(s)
                    else Either.Left("Invalid email")

                val createUser = ::User.liftEither<String, String, Int, String, User>()

                // Valid user
                createUser(
                    validateName("Alice"),
                    validateAge(25),
                    validateEmail("alice@example.com")
                ) shouldBe Either.Right(User("Alice", 25, "alice@example.com"))

                // Invalid name
                createUser(
                    validateName("Al"),
                    validateAge(25),
                    validateEmail("alice@example.com")
                ) shouldBe Either.Left("Name too short")

                // Invalid age
                createUser(
                    validateName("Alice"),
                    validateAge(15),
                    validateEmail("alice@example.com")
                ) shouldBe Either.Left("Must be 18+")
            }
        }

        branch("Eithers companion object") {
            local("map2 combines two Eithers") {
                lawCheckAll(Arb.int(), Arb.int()) { a, b ->
                    val e1: Either<String, Int> = Either.Right(a)
                    val e2: Either<String, Int> = Either.Right(b)

                    Eithers.map2(e1, e2) { x, y -> x + y } shouldBe Either.Right(a + b)
                }
            }

            local("map2 short-circuits on first Left") {
                lawCheckAll(Arb.string(), Arb.int()) { error, value ->
                    val left: Either<String, Int> = Either.Left(error)
                    val right: Either<String, Int> = Either.Right(value)

                    Eithers.map2(left, right) { x, y -> x + y } shouldBe left
                }
            }

            local("sequence all Rights") {
                lawCheckAll(Arb.list(Arb.int(), 0..20)) { values ->
                    val eithers = values.map { Either.Right(it) as Either<String, Int> }
                    Eithers.sequence(eithers) shouldBe Either.Right(values)
                }
            }

            local("sequence with one Left returns that Left") {
                lawCheckAll(Arb.list(Arb.int(), 1..20), Arb.string()) { values, error ->
                    val eithers = values.map { Either.Right(it) as Either<String, Int> }
                        .toMutableList()
                    val leftIndex = values.size / 2
                    eithers[leftIndex] = Either.Left(error)
                    Eithers.sequence(eithers) shouldBe Either.Left(error)
                }
            }

            local("sequence empty list returns Right of empty list") {
                val empty = emptyList<Either<String, Int>>()
                Eithers.sequence(empty) shouldBe Either.Right(emptyList())
            }

            local("sequence is tail-recursive (stack-safe)") {
                val large = (1..10000).map { Either.Right(it) as Either<String, Int> }
                val result = Eithers.sequence(large)
                result.shouldBeInstanceOf<Either.Right<List<Int>>>()
                result.value.size shouldBe 10000
            }

            local("traverse maps and sequences") {
                lawCheckAll(Arb.list(Arb.int(0..100), 0..20)) { values ->
                    Eithers.traverse(values) {
                        Either.Right(it * 2) as Either<String, Int>
                    } shouldBe Either.Right(values.map { it * 2 })
                }
            }

            local("traverse short-circuits on first Left") {
                lawCheckAll(Arb.list(Arb.int(0..100), 1..20)) { values ->
                    var count = 0
                    val failAtIndex = values.size / 2

                    Eithers.traverse(values.withIndex().toList()) { (index, value) ->
                        count++
                        if (index == failAtIndex) {
                            Either.Left("error") as Either<String, Int>
                        } else {
                            Either.Right(value)
                        }
                    }
                    // Should stop at the Left
                    count shouldBe failAtIndex + 1
                }
            }

            local("catches successful computation") {
                lawCheckAll(Arb.int()) { value ->
                    Eithers.catches { value } shouldBe Either.Right(value)
                }
            }

            local("catches exception") {
                lawCheckAll(Arb.string()) { message ->
                    val result = Eithers.catches {
                        throw RuntimeException(message)
                    }
                    result.shouldBeInstanceOf<Either.Left<Throwable>>()
                    result.value.message shouldBe message
                }
            }

            local("cond with true returns Right") {
                lawCheckAll(Arb.int()) { value ->
                    Eithers.cond(true, { value }, { "error" }) shouldBe Either.Right(value)
                }
            }

            local("cond with false returns Left") {
                lawCheckAll(Arb.string()) { error ->
                    Eithers.cond(false, { 42 }, { error }) shouldBe Either.Left(error)
                }
            }
        }

        local("Property: map preserves structure") {
            lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                either.map { it }.isLeft() shouldBe either.isLeft()
                either.map { it }.isRight() shouldBe either.isRight()
            }
        }

        local("Property: flatMap with Right identity doesn't change structure") {
            lawCheckAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                val result = either.flatMap { Either.Right(it) }
                result.isLeft() shouldBe either.isLeft()
                result.isRight() shouldBe either.isRight()
            }
        }

        branch("Edge cases") {
            local("nested Eithers work correctly") {
                val nested: Either<String, Either<String, Int>> =
                    Either.Right(Either.Right(42))
                nested.flatten() shouldBe Either.Right(42)
            }

            local("deeply nested Eithers can be flattened multiple times") {
                val deep: Either<String, Either<String, Either<String, Int>>> =
                    Either.Right(Either.Right(Either.Right(42)))
                deep.flatten().flatten() shouldBe Either.Right(42)
            }

            local("large collection sequence doesn't stack overflow") {
                val large = (1..10000).map { Either.Right(it) as Either<String, Int> }
                val result = Eithers.sequence(large)
                result.shouldBeInstanceOf<Either.Right<List<Int>>>()
                result.value.size shouldBe 10000
            }

            local("multiple error types through bimap") {
                val intError: Either<Int, String> = Either.Left(404)
                val stringError: Either<String, String> = intError.bimap(
                    { "Error code: $it" },
                    { it }
                )
                stringError shouldBe Either.Left("Error code: 404")
            }
        }

        branch("Real-world scenarios") {
            local("chaining validations with flatMap") {
                fun parseAge(s: String): Either<String, Int> =
                    s.toIntOrNull()?.let { Either.Right(it) }
                        ?: Either.Left("Not a number: $s")

                fun validateAge(n: Int): Either<String, Int> =
                    if (n in 0..150) Either.Right(n)
                    else Either.Left("Invalid age: $n")

                parseAge("25")
                    .flatMap { validateAge(it) } shouldBe Either.Right(25)

                parseAge("abc")
                    .flatMap { validateAge(it) } shouldBe Either.Left("Not a number: abc")

                parseAge("200")
                    .flatMap { validateAge(it) } shouldBe Either.Left("Invalid age: 200")
            }

            local("error recovery chain") {
                val primaryService: Either<String, Int> = Either.Left("Primary down")
                val fallbackService: Either<String, Int> = Either.Right(42)

                val result = primaryService
                    .recoverWith { fallbackService }
                    .recover { 0 }

                result shouldBe Either.Right(42)
            }

            local("side effects for logging") {
                var loggedError: String? = null
                var loggedSuccess: Int? = null

                val computation: Either<String, Int> = Either.Right(42)

                computation
                    .onLeft { loggedError = it }
                    .onRight { loggedSuccess = it }

                loggedError shouldBe null
                loggedSuccess shouldBe 42
            }
        }
    })
})
