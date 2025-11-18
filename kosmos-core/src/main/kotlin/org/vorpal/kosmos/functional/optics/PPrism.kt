package org.vorpal.kosmos.functional.optics

import org.vorpal.kosmos.core.Identity

/**
 * # Prism
 *
 * A **Prism** focuses on a **variant of a sum type** (sealed class, Either, Option).
 * Think of it as a "pattern match + constructor" pair - it tries to extract a specific case
 * and can build that case back.
 *
 * ## When to Use
 *
 * Use a Prism when working with:
 * - **Sum types** (sealed classes, enums)
 * - **Either<L, R>** (left or right case)
 * - **Option<A>** (Some or None case)
 * - **Result<T>** (Success or Failure)
 * - Any type where the focus **might not match**
 *
 * ## Key Difference from Lens
 *
 * - **Lens**: Focus always exists (product type field) - `get` returns A
 * - **Prism**: Focus might not match (sum type variant) - `getOrNull` returns A?
 *
 * ## Examples
 *
 * ```kotlin
 * sealed class Shape {
 *     data class Circle(val radius: Double) : Shape()
 *     data class Rectangle(val width: Double, val height: Double) : Shape()
 * }
 *
 * // Prism for the Circle variant
 * val circlePrism = Prism<Shape, Double>(
 *     getterOrNull = { shape ->
 *         when (shape) {
 *             is Shape.Circle -> shape.radius
 *             else -> null  // Doesn't match!
 *         }
 *     },
 *     reverseGetter = { radius -> Shape.Circle(radius) },
 *     identityT = { it }  // Return unchanged if doesn't match
 * )
 *
 * val circle: Shape = Shape.Circle(5.0)
 * val rectangle: Shape = Shape.Rectangle(4.0, 3.0)
 *
 * circlePrism.getOrNull(circle)      // 5.0
 * circlePrism.getOrNull(rectangle)   // null (doesn't match!)
 *
 * circlePrism.modify(circle) { it * 2 }      // Circle(10.0)
 * circlePrism.modify(rectangle) { it * 2 }   // Rectangle(4.0, 3.0) - unchanged!
 * ```
 *
 * ### Real-World Example: Either
 *
 * ```kotlin
 * sealed class Either<out L, out R> {
 *     data class Left<L>(val value: L) : Either<L, Nothing>()
 *     data class Right<R>(val value: R) : Either<Nothing, R>()
 * }
 *
 * // Prism for Right case
 * fun <L, R> right() = Prism<Either<L, R>, R>(
 *     getterOrNull = { either ->
 *         when (either) {
 *             is Either.Right -> either.value
 *             is Either.Left -> null
 *         }
 *     },
 *     reverseGetter = { Either.Right(it) },
 *     identityT = { it }
 * )
 *
 * val success: Either<String, Int> = Either.Right(42)
 * val failure: Either<String, Int> = Either.Left("error")
 *
 * right<String, Int>().getOrNull(success)  // 42
 * right<String, Int>().getOrNull(failure)  // null
 * right<String, Int>().modify(success) { it * 2 }  // Right(84)
 * right<String, Int>().modify(failure) { it * 2 }  // Left("error") - unchanged
 * ```
 *
 * ## Prism Laws
 *
 * Every valid Prism should satisfy:
 *
 * 1. **Partial Round-trip**: If `getOrNull` succeeds, then `reverseGet(get(s))` reconstructs s
 *    - `getOrNull(s)?.let { reverseGet(it) } == s` (when it matches)
 *
 * 2. **Reverse-Get-Get**: `getOrNull(reverseGet(a))` always succeeds and returns a
 *    - Building from focus then extracting gives you back the focus
 *
 * ## Composition
 *
 * - `Prism andThen Prism = Prism` - Match nested sum types
 * - `Prism andThen Lens = Optional` - After matching, focus on a field
 * - `Prism andThen Optional = Optional` - Both can fail
 * - `Prism andThen Traversal = Traversal` - Multiple elements if match succeeds
 *
 * ## Type Parameters
 *
 * - [S]: Source sum type (the sealed class, Either, etc.)
 * - [T]: Target type after reconstruction
 * - [A]: Focus type (the variant's content)
 * - [B]: Modified focus type
 *
 * For monomorphic prisms, use [Prism]<S, A> = PPrism<S, S, A, A>
 *
 * @see PLens for focuses that always exist
 * @see POptional for optional focuses in product types
 */
data class PPrism<S, T, A, B>(
    /** Try to extract the focus - returns null if this variant doesn't match */
    val getterOrNull: (S) -> A?,
    /** Construct the sum type from the focus (inverse operation) */
    val reverseGetter: (B) -> T,
    /** Return structure unchanged when it doesn't match this variant */
    val identityT: (S) -> T
): GetterOrNull<S, A>, ReverseGetter<B, T> {

    /** Try to extract focus - returns null if sum type doesn't match this variant */
    override fun getOrNull(s: S): A? = getterOrNull(s)

    /** Construct the sum type from a focus value */
    override fun reverseGet(b: B): T = reverseGetter(b)

    /**
     * Modify the focus if it matches, otherwise return structure unchanged.
     *
     * This is the key operation for Prisms - it gracefully handles non-matching cases.
     *
     * Example:
     * ```kotlin
     * circlePrism.modify(circle) { radius * 2 }      // Circle(10.0)
     * circlePrism.modify(rectangle) { radius * 2 }   // Rectangle unchanged
     * ```
     */
    fun modify(s: S, f: (A) -> B): T =
        getOrNull(s)?.let { a -> reverseGet(f(a)) } ?: identityT(s)

    /**
     * Compose two Prisms to match nested sum types.
     *
     * Example:
     * ```kotlin
     * val rightPrism: Prism<Either<E, A>, A> = ...
     * val somePrism: Prism<Option<B>, B> = ...
     * val rightSome = rightPrism andThen somePrism  // Either<E, Option<B>> -> B
     * ```
     */
    infix fun <C, D> andThen(other: PPrism<A, B, C, D>): PPrism<S, T, C, D> =
        PPrism(
            getterOrNull = { s -> getOrNull(s)?.let(other::getOrNull) },
            reverseGetter = { d -> reverseGet(other.reverseGet(d)) },
            identityT = identityT
        )

    /**
     * Compose Prisms in reverse order.
     */
    infix fun <C, D> compose(other: PPrism<C, D, S, T>): PPrism<C, D, A, B> =
        other andThen this

    /**
     * Compose Prism with Lens.
     *
     * Results in Optional since Prism might not match.
     *
     * Example:
     * ```kotlin
     * val circlePrism: Prism<Shape, Shape.Circle> = ...
     * val radiusLens: Lens<Shape.Circle, Double> = ...
     * val circleRadius = circlePrism andThen radiusLens  // Shape -> Double?
     * ```
     */
    infix fun <C, D> andThen(other: PLens<A, B, C, D>): POptional<S, T, C, D> =
        POptional(
            getterOrNull = { s -> getOrNull(s)?.let(other::get) },
            setter = { s, d -> getOrNull(s)
                ?.let { a -> reverseGet(other.set(a, d)) }
                ?: identityT(s)
            },
            identityT
        )

    /**
     * Compose Prism with Optional.
     *
     * Results in Optional since either could fail.
     */
    infix fun <C, D> andThen(other: POptional<A, B, C, D>): POptional<S, T, C, D> =
        POptional(
            getterOrNull = { s -> getOrNull(s)?.let(other::getOrNull) },
            setter = { s, d ->
                getOrNull(s)?.let { a ->
                    reverseGet(other.set(a, d))
                } ?: identityT(s)
            },
            identityT = identityT
        )

    /**
     * Compose Prism with Traversal.
     *
     * Results in Traversal - focuses on multiple elements if Prism matches, zero otherwise.
     */
    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        PTraversal(
            getter = { s -> getOrNull(s)?.let { a -> other.getter(a) } ?: emptyList() },
            modify = { s, f ->
                getOrNull(s)?.let { a ->
                    reverseGet(other.modify(a, f))
                } ?: identityT(s)
            }
        )

    /**
     * Compose Prism with Setter.
     */
    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    /**
     * Reverse composition with Iso.
     */
    infix fun <C, D> compose(other: PIso<C, D, S, T>): PPrism<C, D, A, B> =
        other andThen this

    /**
     * Convert Prism to Optional.
     *
     * This is always safe since Prism is conceptually a special case of Optional.
     */
    fun toOptional(): POptional<S, T, A, B> = POptional(
        getterOrNull = getterOrNull,
        setter = { _, b -> reverseGetter(b) },
        identityT = identityT
    )

    /**
     * Convert Prism to Traversal that focuses on 0 or 1 element.
     */
    fun toTraversal(): PTraversal<S, T, A, B> = PTraversal(
        getter = { s -> getOrNull(s)?.let { listOf(it) } ?: emptyList() },
        modify = this::modify
    )

    /**
     * Convert Prism to Setter (write-only optic).
     */
    fun toSetter(): PSetter<S, T, A, B> =
        PSetter(this::modify)

    companion object {
        /**
         * A Prism that never matches: useful for composition tricks.
         */
        fun <S, A> never(): Prism<S, A> = Prism(
            getterOrNull = { null },
            reverseGetter = { throw IllegalStateException("Unreachable") },
            identityT = Identity()
        )
    }
}

/**
 * Monomorphic Prism where types don't change.
 *
 * Example: `Prism<Shape, Double>` for extracting Circle radius from Shape.
 */
typealias Prism<S, A> = PPrism<S, S, A, A>