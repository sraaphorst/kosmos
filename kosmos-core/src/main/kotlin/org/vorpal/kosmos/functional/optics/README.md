# Kosmos Optics Library Guide

## Table of Contents

1. [What Are Optics?](#what-are-optics)
2. [The Optics Hierarchy](#the-optics-hierarchy)
3. [When to Use Each Optic](#when-to-use-each-optic)
4. [Quick Reference](#quick-reference)
5. [Composition Rules](#composition-rules)
6. [Common Patterns](#common-patterns)
7. [Examples](#examples)

## What Are Optics?

**Optics** are composable tools for accessing and modifying parts of immutable data structures. They solve the problem of deeply nested updates in functional programming.

### The Problem

Without optics, updating nested immutable structures is verbose:

```kotlin
data class Street(val name: String)
data class Address(val street: Street)
data class Person(val name: String, val address: Address)

val person = Person("Alice", Address(Street("Main St")))

// To change the street name:
val updated = person.copy(
    address = person.address.copy(
        street = person.address.street.copy(
            name = "Elm St"
        )
    )
)
```

### The Solution

With optics, it's clean and composable:

```kotlin
val streetNameLens = personLens andThen addressLens andThen streetLens andThen nameLens
val updated = streetNameLens.set(person, "Elm St")
```

## The Optics Hierarchy

```
Iso          - Bidirectional, lossless conversion (most powerful)
 ├─> Lens    - Always exists (product type field)
 └─> Prism   - May not match (sum type variant)
      └─> Optional - May be missing (nullable, map key)
           └─> Traversal - Zero or more elements
                └─> Setter - Write-only (most general)
```

### Power vs Generality

- **More powerful** = Can do more, but applies to fewer situations
- **More general** = Applies to more situations, but can do less

**Iso** is most powerful (can read AND write bidirectionally) but only works for perfect conversions.
**Setter** is most general (works for any modification) but can only write, not read.

## When to Use Each Optic

| Optic | Use When | Example | Can Read? | Can Fail? |
|-------|----------|---------|-----------|-----------|
| **Iso** | Lossless conversion between types | String ↔ List<Char> | ✓ | ✗ |
| **Lens** | Accessing product type field that always exists | person.name | ✓ | ✗ |
| **Prism** | Accessing sum type variant that may not match | Either.Right | ✓ | ✓ |
| **Optional** | Accessing element that may be missing | map[key], list[i] | ✓ | ✓ |
| **Traversal** | Accessing multiple elements | list elements | ✓ | ✗* |
| **Setter** | Only modifying, not reading | Write-only access | ✗ | ✗ |

*Traversal doesn't fail, but can focus on zero elements

### Decision Tree

```
Can you convert back and forth perfectly?
├─ YES → Iso
└─ NO → Does the focus always exist?
    ├─ YES → Lens
    └─ NO → Is it a sum type variant?
        ├─ YES → Prism
        └─ NO → Could there be multiple elements?
            ├─ YES → Traversal
            └─ NO → Optional
```

## Quick Reference

### Iso (PIso)

```kotlin
// String ↔ List<Char>
val stringChars = Iso<String, List<Char>>(
    getter = { it.toList() },
    reverseGetter = { it.joinToString("") }
)

stringChars.get("hello")              // ['h', 'e', 'l', 'l', 'o']
stringChars.reverseGet(listOf('h', 'i'))  // "hi"
```

**Key methods:**
- `get(s: S): A` - Extract focus
- `reverseGet(b: B): T` - Build structure from focus
- `modify(s: S, f: (A) -> B): T` - Transform via focus

### Lens (PLens)

```kotlin
data class Person(val name: String, val age: Int)

val nameLens = Lens<Person, String>(
    getter = { it.name },
    setter = { person, newName -> person.copy(name = newName) }
)

nameLens.get(person)              // "Alice"
nameLens.set(person, "Bob")       // Person("Bob", 30)
nameLens.modify(person) { it.uppercase() }  // Person("ALICE", 30)
```

**Key methods:**
- `get(s: S): A` - Read the focus
- `set(s: S, b: B): T` - Write the focus
- `modify(s: S, f: (A) -> B): T` - Transform the focus

### Prism (PPrism)

```kotlin
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: String) : Result<Nothing>()
}

val successPrism = Prism<Result<Int>, Int>(
    getterOrNull = { result ->
        when (result) {
            is Result.Success -> result.value
            else -> null
        }
    },
    reverseGetter = { Result.Success(it) },
    identityT = { it }
)

successPrism.getOrNull(Result.Success(42))  // 42
successPrism.getOrNull(Result.Failure("error"))  // null
```

**Key methods:**
- `getOrNull(s: S): A?` - Try to extract focus
- `reverseGet(b: B): T` - Build the sum type from focus
- `modify(s: S, f: (A) -> B): T` - Modify if matches

### Optional (POptional)

```kotlin
val map = mapOf("a" to 1, "b" to 2)

val atKeyA = Optional.of<Map<String, Int>, Int>(
    getterOrNull = { it["a"] },
    setter = { map, value -> map + ("a" to value) }
)

atKeyA.getOrNull(map)              // 1
atKeyA.getOrNull(emptyMap())       // null
atKeyA.set(map, 10)                // {"a": 10, "b": 2}
```

**Key methods:**
- `getOrNull(s: S): A?` - Try to get focus
- `set(s: S, b: B): T` - Set focus (may add it)
- `modify(s: S, f: (A) -> B): T` - Modify if present

### Traversal (PTraversal)

```kotlin
val numbers = listOf(1, 2, 3, 4)

val each = Traversal<List<Int>, Int>(
    getter = { it },
    modify = { list, f -> list.map(f) }
)

each.get(numbers)                // [1, 2, 3, 4]
each.modify(numbers) { it * 2 }  // [2, 4, 6, 8]
each.set(numbers, 0)             // [0, 0, 0, 0]
```

**Key methods:**
- `get(s: S): List<A>` - Get all focuses
- `set(s: S, b: B): T` - Set all to same value
- `modify(s: S, f: (A) -> B): T` - Transform each

### Setter (PSetter)

```kotlin
val ageSetter = PSetter<Person, Person, Int, Int> { person, f ->
    person.copy(age = f(person.age))
}

ageSetter.modify(person) { it + 1 }  // Increment age
// ageSetter.get(person)  // Compile error - no read!
```

**Key methods:**
- `set(s: S, b: B): T` - Set to value
- `modify(s: S, f: (A) -> B): T` - Transform focus

## Composition Rules

When you compose optics with `andThen`, the result is determined by the **weaker** of the two:

| First | Second | Result | Intuition |
|-------|--------|--------|-----------|
| Iso | Iso | Iso | Perfect stays perfect |
| Iso | Lens | Lens | Can't reverse through lens |
| Iso | Prism | Prism | Can't guarantee match |
| Lens | Lens | Lens | Field of field |
| Lens | Optional | Optional | Field might be missing |
| Lens | Prism | Optional | Field variant might not match |
| Prism | Lens | Optional | Variant might not match |
| Prism | Prism | Prism | Nested variant matching |
| Optional | Optional | Optional | Both might fail |
| *any* | Traversal | Traversal | Multiple elements |
| *any* | Setter | Setter | Lose read capability |

### Composition Examples

```kotlin
// Lens andThen Lens = Lens (nested field access)
val ceoLens: Lens<Company, Person> = ...
val nameLens: Lens<Person, String> = ...
val ceoName = ceoLens andThen nameLens  // Lens<Company, String>

// Lens andThen Optional = Optional (field might be missing)
val employeesLens: Lens<Company, List<Person>> = ...
val first: Optional<List<Person>, Person> = ...
val firstEmployee = employeesLens andThen first  // Optional<Company, Person>

// Lens andThen Traversal = Traversal (multiple elements)
val employeesLens: Lens<Company, List<Person>> = ...
val each: Traversal<List<Person>, Person> = ...
val allEmployees = employeesLens andThen each  // Traversal<Company, Person>
```

## Common Patterns

### Nested Data Access

```kotlin
data class Street(val name: String)
data class Address(val street: Street)
data class Person(val address: Address)

val streetLens = Lens<Address, Street>(
    getter = { it.street },
    setter = { addr, s -> addr.copy(street = s) }
)

val addressLens = Lens<Person, Address>(
    getter = { it.address },
    setter = { p, a -> p.copy(address = a) }
)

val nameLens = Lens<Street, String>(
    getter = { it.name },
    setter = { s, n -> s.copy(name = n) }
)

// Compose for deep access
val personStreetName = addressLens andThen streetLens andThen nameLens

person.get(personStreetName)  // Read deep
person.set(personStreetName, "Elm St")  // Write deep
```

### Sum Type Handling

```kotlin
sealed class Payment {
    data class CreditCard(val number: String) : Payment()
    data class Cash(val amount: Double) : Payment()
}

val creditCardPrism = Prism<Payment, String>(
    getterOrNull = { p -> (p as? Payment.CreditCard)?.number },
    reverseGetter = { Payment.CreditCard(it) },
    identityT = { it }
)

// Only modifies credit card payments
creditCardPrism.modify(payment) { maskCardNumber(it) }
```

### Collection Operations

```kotlin
data class Team(val members: List<Person>)

val membersLens: Lens<Team, List<Person>> = ...
val each: Traversal<List<Person>, Person> = ...
val ageLens: Lens<Person, Int> = ...

// All member ages in team
val allAges = membersLens andThen each andThen ageLens

team.get(allAges)              // List of all ages
team.modify(allAges) { it + 1 }  // Everyone ages a year
```

### Safe Nullable Access

```kotlin
data class Config(val timeout: Int?)

val timeoutOptional = Optional.of<Config, Int>(
    getterOrNull = { it.timeout },
    setter = { cfg, t -> cfg.copy(timeout = t) }
)

timeoutOptional.modify(config) { it * 2 }  // Only if timeout is set
```

## Examples

### Example 1: Deeply Nested Update

**Problem:** Update a street name in a nested structure

```kotlin
data class Country(val name: String)
data class City(val name: String, val country: Country)
data class Street(val name: String, val city: City)
data class Address(val street: Street)
data class Person(val name: String, val address: Address)

// Build the lens chain
val addressLens = Lens<Person, Address>(
    getter = { it.address },
    setter = { p, a -> p.copy(address = a) }
)

val streetLens = Lens<Address, Street>(
    getter = { it.street },
    setter = { a, s -> a.copy(street = s) }
)

val cityLens = Lens<Street, City>(
    getter = { it.city },
    setter = { s, c -> s.copy(city = c) }
)

val cityNameLens = Lens<City, String>(
    getter = { it.name },
    setter = { c, n -> c.copy(name = n) }
)

val personCityName = addressLens andThen streetLens andThen cityLens andThen cityNameLens

// Use it
val person = Person(/* ... */)
personCityName.get(person)  // Read city name
personCityName.set(person, "New York")  // Change city name
```

### Example 2: Working with Either

```kotlin
sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()
}

fun <L, R> right() = Prism<Either<L, R>, R>(
    getterOrNull = { (it as? Either.Right)?.value },
    reverseGetter = { Either.Right(it) },
    identityT = { it }
)

val result: Either<String, Int> = Either.Right(42)
right<String, Int>().modify(result) { it * 2 }  // Right(84)

val error: Either<String, Int> = Either.Left("error")
right<String, Int>().modify(error) { it * 2 }   // Left("error") - unchanged
```

### Example 3: Batch Processing with Traversal

```kotlin
data class Student(val name: String, val grade: Double)
data class Classroom(val students: List<Student>)

val studentsLens = Lens<Classroom, List<Student>>(
    getter = { it.students },
    setter = { c, s -> c.copy(students = s) }
)

val eachStudent = Lenses.each<Student>()

val gradeLens = Lens<Student, Double>(
    getter = { it.grade },
    setter = { s, g -> s.copy(grade = g) }
)

val allGrades = studentsLens andThen eachStudent andThen gradeLens

val classroom = Classroom(listOf(
    Student("Alice", 85.0),
    Student("Bob", 90.0)
))

// Curve all grades by 5%
allGrades.modify(classroom) { it * 1.05 }

// Get all grades
allGrades.get(classroom)  // [85.0, 90.0]
```

### Example 4: Optional Map Access

```kotlin
data class UserProfile(val metadata: Map<String, String>)

val metadataLens = Lens<UserProfile, Map<String, String>>(
    getter = { it.metadata },
    setter = { p, m -> p.copy(metadata = m) }
)

val bioOptional = Lenses.atKey<String, String>("bio")

val userBio = metadataLens andThen bioOptional

val profile = UserProfile(mapOf("bio" to "Developer", "location" to "NYC"))

userBio.getOrNull(profile)  // "Developer"
userBio.set(profile, "Senior Developer")  // Updates bio
userBio.modify(profile) { it.uppercase() }  // "DEVELOPER"
```

## Pre-built Optics

The `Lenses` object provides common optics:

```kotlin
// Pair access
Lenses.first<A, B>()   // Lens<Pair<A, B>, A>
Lenses.second<A, B>()  // Lens<Pair<A, B>, B>

// List access
Lenses.at<A>(index)    // Optional<List<A>, A>
Lenses.each<A>()       // Traversal<List<A>, A>

// Map access
Lenses.atKey<K, V>(key)  // Optional<Map<K, V>, V>

// Nullable
Lenses.nullable<A>()   // Prism<A?, A>

// Homogeneous pair
Lenses.both<A>()       // Traversal<Pair<A, A>, A>
```

## Tips and Best Practices

### 1. Start with Lens, specialize as needed

Most field access uses Lens. Only reach for Optional/Prism when the focus can fail.

### 2. Compose liberally

Don't be afraid to chain many optics - that's their strength!

```kotlin
val result = lens1 andThen lens2 andThen optional3 andThen lens4
```

### 3. Use type aliases for clarity

```kotlin
typealias StreetNameOptic = Lens<Person, String>
val streetName: StreetNameOptic = personLens andThen addressLens andThen streetLens
```

### 4. Create reusable optics

Define optics as vals and reuse them:

```kotlin
object PersonOptics {
    val name = Lens<Person, String>(...)
    val age = Lens<Person, Int>(...)
    val address = Lens<Person, Address>(...)
}

object AddressOptics {
    val street = Lens<Address, Street>(...)
    val city = Lens<Address, String>(...)
}

val personCity = PersonOptics.address andThen AddressOptics.city
```

### 5. Remember the laws

When creating custom optics, test that they satisfy the appropriate laws (Get-Put, Put-Get, Put-Put for Lens, etc.)

---

## Further Reading

- **Arrow Optics** (Kotlin): https://arrow-kt.io/docs/optics/
- **Monocle** (Scala): https://www.optics.dev/Monocle/
- **lens** (Haskell): https://hackage.haskell.org/package/lens

## Summary

Optics provide composable, type-safe access to immutable data structures. Choose the right optic for your use case:

- **Iso**: Perfect conversion
- **Lens**: Always-present field
- **Prism**: Sum type variant
- **Optional**: Nullable/missing field
- **Traversal**: Multiple elements
- **Setter**: Write-only

Compose them with `andThen` to build complex accessors that read like natural field access.