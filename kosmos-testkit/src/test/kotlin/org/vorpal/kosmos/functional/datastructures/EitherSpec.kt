package org.vorpal.kosmos.functional.datastructures

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.kotest.assertions.throwables.shouldThrow

class EitherSpec : FreeSpec({

    "Either construction" - {
        "Left construction" {
            val left = Either.Left("error")
            left.shouldBeInstanceOf<Either.Left<String>>()
            left.value shouldBe "error"
        }

        "Right construction" {
            val right = Either.Right(42)
            right.shouldBeInstanceOf<Either.Right<Int>>()
            right.value shouldBe 42
        }

        "Companion left" {
            val left = Either.left("error")
            left.shouldBeInstanceOf<Either.Left<String>>()
        }

        "Companion right" {
            val right = Either.right(42)
            right.shouldBeInstanceOf<Either.Right<Int>>()
        }
    }

    "Either predicates" - {
        "isLeft on Left returns true" {
            checkAll(Arb.string()) { s ->
                Either.Left(s).isLeft() shouldBe true
            }
        }

        "isLeft on Right returns false" {
            checkAll(Arb.int()) { i ->
                Either.Right(i).isLeft() shouldBe false
            }
        }

        "isRight on Right returns true" {
            checkAll(Arb.int()) { i ->
                Either.Right(i).isRight() shouldBe true
            }
        }

        "isRight on Left returns false" {
            checkAll(Arb.string()) { s ->
                Either.Left(s).isRight() shouldBe false
            }
        }
    }

    "Functor Laws" - {
        "Identity: map(id) == id" {
            checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                either.map { it } shouldBe either
            }
        }

        "Composition: map(f).map(g) == map(g ∘ f)" {
            checkAll(
                ArbEither.either(Arb.string(), Arb.int()),
                Arb.int(1..100),
                Arb.int(1..100)
            ) { either, n, m ->
                val f: (Int) -> Int = { it + n }
                val g: (Int) -> Int = { it * m }

                either.map(f).map(g) shouldBe either.map { g(f(it)) }
            }
        }

        "map on Left is no-op" {
            checkAll(Arb.string(), Arb.int()) { error, n ->
                val left: Either<String, Int> = Either.Left(error)
                left.map { it + n } shouldBe left
            }
        }

        "map on Right applies function" {
            checkAll(Arb.int(), Arb.int()) { value, n ->
                val right: Either<String, Int> = Either.Right(value)
                right.map { it + n } shouldBe Either.Right(value + n)
            }
        }
    }

    "Monad Laws" - {
        "Left identity: right(a).flatMap(f) == f(a)" {
            checkAll(Arb.int()) { n ->
                val f: (Int) -> Either<String, Int> = { Either.Right(it * 2) }
                Either.right(n).flatMap(f) shouldBe f(n)
            }
        }

        "Right identity: m.flatMap(right) == m" {
            checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                either.flatMap { Either.right(it) } shouldBe either
            }
        }

        "Associativity: m.flatMap(f).flatMap(g) == m.flatMap { x -> f(x).flatMap(g) }" {
            checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                val f: (Int) -> Either<String, Int> = { Either.Right(it + 1) }
                val g: (Int) -> Either<String, Int> = { Either.Right(it * 2) }

                either.flatMap(f).flatMap(g) shouldBe
                        either.flatMap { x -> f(x).flatMap(g) }
            }
        }

        "flatMap on Left short-circuits" {
            checkAll(Arb.string()) { error ->
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

    "mapLeft" - {
        "transforms Left value" {
            checkAll(Arb.string(), Arb.int()) { error, n ->
                val left: Either<String, Int> = Either.Left(error)
                left.mapLeft { it.length + n } shouldBe Either.Left(error.length + n)
            }
        }

        "preserves Right" {
            checkAll(Arb.int(), Arb.string()) { value, _ ->
                val right: Either<String, Int> = Either.Right(value)
                right.mapLeft { it.length } shouldBe right
            }
        }
    }

    "flatMapLeft" - {
        "transforms Left with function returning Either" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.flatMapLeft { Either.Left(it.length) } shouldBe Either.Left(error.length)
            }
        }

        "can recover from Left to Right" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.flatMapLeft { Either.Right(42) } shouldBe Either.Right(42)
            }
        }

        "preserves Right" {
            checkAll(Arb.int()) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.flatMapLeft { Either.Left(it.length) } shouldBe right
            }
        }
    }

    "bimap" - {
        "transforms both sides" {
            checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
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

        "bimap identity is identity" {
            checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                either.bimap({ it }, { it }) shouldBe either
            }
        }
    }

    "orElse" - {
        "Left uses alternative" {
            checkAll(Arb.string(), Arb.int()) { error, value ->
                val left: Either<String, Int> = Either.Left(error)
                val alternative = Either.Right(value)
                left.orElse { alternative } shouldBe alternative
            }
        }

        "Right ignores alternative" {
            checkAll(Arb.int()) { value ->
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

    "getOrElse" - {
        "Left returns default" {
            checkAll(Arb.string(), Arb.int()) { error, default ->
                val left: Either<String, Int> = Either.Left(error)
                left.getOrElse { default } shouldBe default
            }
        }

        "Right returns value" {
            checkAll(Arb.int(), Arb.int()) { value, default ->
                val right: Either<String, Int> = Either.Right(value)
                right.getOrElse { default } shouldBe value
            }
        }
    }

    "getOrNull" - {
        "Left returns null" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.getOrNull() shouldBe null
            }
        }

        "Right returns value" {
            checkAll(Arb.int()) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.getOrNull() shouldBe value
            }
        }
    }

    "getOrThrow" - {
        "Left throws the exception" {
            checkAll(Arb.string()) { message ->
                val exception = RuntimeException(message)
                val left: Either<Throwable, Int> = Either.Left(exception)
                val thrown = shouldThrow<RuntimeException> {
                    left.getOrThrow()
                }
                thrown shouldBe exception
            }
        }

        "Right returns value" {
            checkAll(Arb.int()) { value ->
                val right: Either<Throwable, Int> = Either.Right(value)
                right.getOrThrow() shouldBe value
            }
        }
    }

    "fold" - {
        "executes left function on Left" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.fold(
                    ifLeft = { "Error: $it" },
                    ifRight = { "Success: $it" }
                ) shouldBe "Error: $error"
            }
        }

        "executes right function on Right" {
            checkAll(Arb.int()) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.fold(
                    ifLeft = { "Error: $it" },
                    ifRight = { "Success: $it" }
                ) shouldBe "Success: $value"
            }
        }
    }

    "filterOrElse" - {
        "Left passes through" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.filterOrElse({ it > 0 }, { "filtered" }) shouldBe left
            }
        }

        "Right passing predicate is unchanged" {
            checkAll(Arb.int(1..100)) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.filterOrElse({ it > 0 }, { "negative" }) shouldBe right
            }
        }

        "Right failing predicate becomes Left" {
            checkAll(Arb.int(1..100)) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.filterOrElse(
                    { it < 0 },
                    { "not negative" }
                ) shouldBe Either.Left("not negative")
            }
        }
    }

    "recover" - {
        "Left recovers to Right" {
            checkAll(Arb.string(), Arb.int()) { error, default ->
                val left: Either<String, Int> = Either.Left(error)
                left.recover { default } shouldBe Either.Right(default)
            }
        }

        "Right is unchanged" {
            checkAll(Arb.int()) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.recover { 0 } shouldBe right
            }
        }
    }

    "recoverWith" - {
        "Left can recover to Right" {
            checkAll(Arb.string(), Arb.int()) { error, value ->
                val left: Either<String, Int> = Either.Left(error)
                left.recoverWith { Either.Right(value) } shouldBe Either.Right(value)
            }
        }

        "Left can transform to different Left" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.recoverWith {
                    Either.Left(it.length)
                } shouldBe Either.Left(error.length)
            }
        }

        "Right is unchanged" {
            checkAll(Arb.int()) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.recoverWith { Either.Right(0) } shouldBe right
            }
        }
    }

    "collapse" - {
        "Left collapses to value" {
            checkAll(Arb.int()) { value ->
                val left: Either<Int, Int> = Either.Left(value)
                left.collapse() shouldBe value
            }
        }

        "Right collapses to value" {
            checkAll(Arb.int()) { value ->
                val right: Either<Int, Int> = Either.Right(value)
                right.collapse() shouldBe value
            }
        }
    }

    "Side effects" - {
        "onRight executes on Right" {
            checkAll(Arb.int()) { value ->
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

        "onRight does not execute on Left" {
            checkAll(Arb.string()) { error ->
                var executed = false
                val left: Either<String, Int> = Either.Left(error)

                val result = left.onRight { executed = true }

                executed shouldBe false
                result shouldBe left
            }
        }

        "onLeft executes on Left" {
            checkAll(Arb.string()) { error ->
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

        "onLeft does not execute on Right" {
            checkAll(Arb.int()) { value ->
                var executed = false
                val right: Either<String, Int> = Either.Right(value)

                val result = right.onLeft { executed = true }

                executed shouldBe false
                result shouldBe right
            }
        }

        "tap executes on both" {
            checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                var executed = false
                val result = either.tap { executed = true }

                executed shouldBe true
                result shouldBe either
            }
        }
    }

    "swap" - {
        "swaps Left to Right" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.swap() shouldBe Either.Right(error)
            }
        }

        "swaps Right to Left" {
            checkAll(Arb.int()) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.swap() shouldBe Either.Left(value)
            }
        }

        "swap is involutive (swap twice is identity)" {
            checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
                either.swap().swap() shouldBe either
            }
        }
    }

    "Conversions" - {
        "toOption on Right returns Some" {
            checkAll(Arb.int()) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.toOption() shouldBe Option.Some(value)
            }
        }

        "toOption on Left returns None" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.toOption() shouldBe Option.None
            }
        }

        "toLeftOption on Left returns Some" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.toLeftOption() shouldBe Option.Some(error)
            }
        }

        "toLeftOption on Right returns None" {
            checkAll(Arb.int()) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.toLeftOption() shouldBe Option.None
            }
        }
    }

    "Applicative operations" - {
        "ap applies function in Right" {
            checkAll(Arb.int(), Arb.int()) { value, n ->
                val either: Either<String, Int> = Either.Right(value)
                val f: Either<String, (Int) -> Int> = Either.Right { it + n }

                either.ap(f) shouldBe Either.Right(value + n)
            }
        }

        "ap with Left function returns Left" {
            checkAll(Arb.int(), Arb.string()) { value, error ->
                val either: Either<String, Int> = Either.Right(value)
                val f: Either<String, (Int) -> Int> = Either.Left(error)

                either.ap(f) shouldBe Either.Left(error)
            }
        }

        "ap with Left value returns Left value" {
            checkAll(Arb.string(), Arb.int()) { error, n ->
                val either: Either<String, Int> = Either.Left(error)
                val f: Either<String, (Int) -> Int> = Either.Right { it + n }

                either.ap(f) shouldBe Either.Left(error)
            }
        }
    }

    "zip" - {
        "two Rights zip to Pair" {
            checkAll(Arb.int(), Arb.string()) { i, s ->
                val e1: Either<String, Int> = Either.Right(i)
                val e2: Either<String, String> = Either.Right(s)

                e1.zip(e2) shouldBe Either.Right(i to s)
            }
        }

        "Left and Right zip to Left" {
            checkAll(Arb.string(), Arb.int()) { error, value ->
                val left: Either<String, Int> = Either.Left(error)
                val right: Either<String, Int> = Either.Right(value)

                left.zip(right) shouldBe left
            }
        }

        "Right and Left zip to Left" {
            checkAll(Arb.int(), Arb.string()) { value, error ->
                val right: Either<String, Int> = Either.Right(value)
                val left: Either<String, Int> = Either.Left(error)

                right.zip(left) shouldBe left
            }
        }
    }

    "zipWith" - {
        "combines two Rights with function" {
            checkAll(Arb.int(), Arb.int()) { a, b ->
                val e1: Either<String, Int> = Either.Right(a)
                val e2: Either<String, Int> = Either.Right(b)

                e1.zipWith(e2) { x, y -> x + y } shouldBe Either.Right(a + b)
            }
        }

        "short-circuits on Left" {
            checkAll(Arb.string(), Arb.int()) { error, value ->
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

    "flatten" - {
        "flattens nested Right" {
            checkAll(Arb.int()) { value ->
                val nested: Either<String, Either<String, Int>> =
                    Either.Right(Either.Right(value))
                nested.flatten() shouldBe Either.Right(value)
            }
        }

        "flattens Right(Left)" {
            checkAll(Arb.string()) { error ->
                val nested: Either<String, Either<String, Int>> =
                    Either.Right(Either.Left(error))
                nested.flatten() shouldBe Either.Left(error)
            }
        }

        "flattens outer Left" {
            checkAll(Arb.string()) { error ->
                val nested: Either<String, Either<String, Int>> = Either.Left(error)
                nested.flatten() shouldBe Either.Left(error)
            }
        }
    }

    "ensure, guard, and filter aliases" - {
        "ensure passing predicate preserves Right" {
            checkAll(Arb.int(1..100)) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.ensure({ "negative" }) { it > 0 } shouldBe right
            }
        }

        "ensure failing predicate converts to Left" {
            checkAll(Arb.int(1..100)) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.ensure({ "not negative" }) { it < 0 } shouldBe
                        Either.Left("not negative")
            }
        }

        "ensure Left passes through" {
            checkAll(Arb.string()) { error ->
                val left: Either<String, Int> = Either.Left(error)
                left.ensure({ "other error" }) { it > 0 } shouldBe left
            }
        }

        "guard is alias for ensure" {
            checkAll(Arb.int(1..100)) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.guard({ "error" }) { it > 0 } shouldBe
                        right.ensure({ "error" }) { it > 0 }
            }
        }

        "filter is alias for ensure" {
            checkAll(Arb.int(1..100)) { value ->
                val right: Either<String, Int> = Either.Right(value)
                right.filter({ "error" }) { it > 0 } shouldBe
                        right.ensure({ "error" }) { it > 0 }
            }
        }
    }

    "liftEither extensions" - {
        "unary function lift" {
            checkAll(Arb.int()) { value ->
                val double: (Int) -> Int = { it * 2 }
                val lifted = double.liftEither<String, Int, Int>()

                lifted(Either.Right(value)) shouldBe Either.Right(value * 2)
            }
        }

        "unary lift preserves Left" {
            checkAll(Arb.string()) { error ->
                val double: (Int) -> Int = { it * 2 }
                val lifted = double.liftEither<String, Int, Int>()
                val left: Either<String, Int> = Either.Left(error)

                lifted(left) shouldBe left
            }
        }

        "binary function lift combines two Rights" {
            checkAll(Arb.int(), Arb.int()) { a, b ->
                val add: (Int, Int) -> Int = { x, y -> x + y }
                val lifted = add.liftEither<String, Int, Int, Int>()

                lifted(Either.Right(a), Either.Right(b)) shouldBe Either.Right(a + b)
            }
        }

        "binary lift short-circuits on first Left" {
            checkAll(Arb.string(), Arb.int()) { error, value ->
                val add: (Int, Int) -> Int = { x, y -> x + y }
                val lifted = add.liftEither<String, Int, Int, Int>()
                val left: Either<String, Int> = Either.Left(error)
                val right: Either<String, Int> = Either.Right(value)

                lifted(left, right) shouldBe left
            }
        }

        "binary lift short-circuits on second Left" {
            checkAll(Arb.int(), Arb.string()) { value, error ->
                val add: (Int, Int) -> Int = { x, y -> x + y }
                val lifted = add.liftEither<String, Int, Int, Int>()
                val right: Either<String, Int> = Either.Right(value)
                val left: Either<String, Int> = Either.Left(error)

                lifted(right, left) shouldBe left
            }
        }

        "ternary function lift combines three Rights" {
            checkAll(Arb.int(), Arb.int(), Arb.int()) { a, b, c ->
                val sum: (Int, Int, Int) -> Int = { x, y, z -> x + y + z }
                val lifted = sum.liftEither<String, Int, Int, Int, Int>()

                lifted(
                    Either.Right(a),
                    Either.Right(b),
                    Either.Right(c)
                ) shouldBe Either.Right(a + b + c)
            }
        }

        "ternary lift short-circuits on any Left" {
            checkAll(Arb.string()) { error ->
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

        "practical example: validation with lift" {
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

    "Eithers companion object" - {
        "map2 combines two Eithers" {
            checkAll(Arb.int(), Arb.int()) { a, b ->
                val e1: Either<String, Int> = Either.Right(a)
                val e2: Either<String, Int> = Either.Right(b)

                Eithers.map2(e1, e2) { x, y -> x + y } shouldBe Either.Right(a + b)
            }
        }

        "map2 short-circuits on first Left" {
            checkAll(Arb.string(), Arb.int()) { error, value ->
                val left: Either<String, Int> = Either.Left(error)
                val right: Either<String, Int> = Either.Right(value)

                Eithers.map2(left, right) { x, y -> x + y } shouldBe left
            }
        }

        "sequence all Rights" {
            checkAll(Arb.list(Arb.int(), 0..20)) { values ->
                val eithers = values.map { Either.Right(it) as Either<String, Int> }
                Eithers.sequence(eithers) shouldBe Either.Right(values)
            }
        }

        "sequence with one Left returns that Left" {
            checkAll(Arb.list(Arb.int(), 1..20), Arb.string()) { values, error ->
                val eithers = values.map { Either.Right(it) as Either<String, Int> }
                    .toMutableList()
                val leftIndex = values.size / 2
                eithers[leftIndex] = Either.Left(error)
                Eithers.sequence(eithers) shouldBe Either.Left(error)
            }
        }

        "sequence empty list returns Right of empty list" {
            val empty = emptyList<Either<String, Int>>()
            Eithers.sequence(empty) shouldBe Either.Right(emptyList())
        }

        "sequence is tail-recursive (stack-safe)" {
            val large = (1..10000).map { Either.Right(it) as Either<String, Int> }
            val result = Eithers.sequence(large)
            result.shouldBeInstanceOf<Either.Right<List<Int>>>()
            result.value.size shouldBe 10000
        }

        "traverse maps and sequences" {
            checkAll(Arb.list(Arb.int(0..100), 0..20)) { values ->
                Eithers.traverse(values) {
                    Either.Right(it * 2) as Either<String, Int>
                } shouldBe Either.Right(values.map { it * 2 })
            }
        }

        "traverse short-circuits on first Left" {
            checkAll(Arb.list(Arb.int(0..100), 1..20)) { values ->
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

        "catches successful computation" {
            checkAll(Arb.int()) { value ->
                Eithers.catches { value } shouldBe Either.Right(value)
            }
        }

        "catches exception" {
            checkAll(Arb.string()) { message ->
                val result = Eithers.catches {
                    throw RuntimeException(message)
                }
                result.shouldBeInstanceOf<Either.Left<Throwable>>()
                result.value.message shouldBe message
            }
        }

        "cond with true returns Right" {
            checkAll(Arb.int()) { value ->
                Eithers.cond(true, { value }, { "error" }) shouldBe Either.Right(value)
            }
        }

        "cond with false returns Left" {
            checkAll(Arb.string()) { error ->
                Eithers.cond(false, { 42 }, { error }) shouldBe Either.Left(error)
            }
        }
    }

    "Property: map preserves structure" {
        checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
            either.map { it }.isLeft() shouldBe either.isLeft()
            either.map { it }.isRight() shouldBe either.isRight()
        }
    }

    "Property: flatMap with Right identity doesn't change structure" {
        checkAll(ArbEither.either(Arb.string(), Arb.int())) { either ->
            val result = either.flatMap { Either.Right(it) }
            result.isLeft() shouldBe either.isLeft()
            result.isRight() shouldBe either.isRight()
        }
    }

    "Edge cases" - {
        "nested Eithers work correctly" {
            val nested: Either<String, Either<String, Int>> =
                Either.Right(Either.Right(42))
            nested.flatten() shouldBe Either.Right(42)
        }

        "deeply nested Eithers can be flattened multiple times" {
            val deep: Either<String, Either<String, Either<String, Int>>> =
                Either.Right(Either.Right(Either.Right(42)))
            deep.flatten().flatten() shouldBe Either.Right(42)
        }

        "large collection sequence doesn't stack overflow" {
            val large = (1..10000).map { Either.Right(it) as Either<String, Int> }
            val result = Eithers.sequence(large)
            result.shouldBeInstanceOf<Either.Right<List<Int>>>()
            result.value.size shouldBe 10000
        }

        "multiple error types through bimap" {
            val intError: Either<Int, String> = Either.Left(404)
            val stringError: Either<String, String> = intError.bimap(
                { "Error code: $it" },
                { it }
            )
            stringError shouldBe Either.Left("Error code: 404")
        }
    }

    "Real-world scenarios" - {
        "chaining validations with flatMap" {
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

        "error recovery chain" {
            val primaryService: Either<String, Int> = Either.Left("Primary down")
            val fallbackService: Either<String, Int> = Either.Right(42)

            val result = primaryService
                .recoverWith { fallbackService }
                .recover { 0 }

            result shouldBe Either.Right(42)
        }

        "side effects for logging" {
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
