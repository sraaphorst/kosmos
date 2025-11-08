package org.vorpal.kosmos.functional.optics

/**
 * # Traversal
 *
 * A **Traversal** focuses on **zero or more elements** within a structure.
 * Think of it as a way to access and modify multiple parts of a data structure at once,
 * like all elements in a list, all values in a map, or all leaves in a tree.
 *
 * ## When to Use
 *
 * Use a Traversal when you need to:
 * - Work with **collections** (list, set, map values)
 * - Focus on **multiple fields** simultaneously
 * - Apply transformations to **all matching elements**
 * - Filter and transform in one operation
 *
 * ## Key Characteristics
 *
 * - Can focus on **0, 1, or many elements**
 * - **Read**: Get all focused elements as a List
 * - **Write**: Modify all focused elements with a function
 * - More general than Lens/Prism/Optional (those focus on 0-1 element)
 *
 * ## Examples
 *
 * ### Simple List Traversal
 *
 * ```kotlin
 * val numbers = listOf(1, 2, 3, 4, 5)
 *
 * val eachNumber = Traversal<List<Int>, Int>(
 *     getter = { it },  // Get all elements
 *     modify = { list, f -> list.map(f) }  // Transform each
 * )
 *
 * eachNumber.get(numbers)                // [1, 2, 3, 4, 5]
 * eachNumber.modify(numbers) { it * 2 }  // [2, 4, 6, 8, 10]
 * eachNumber.set(numbers, 0)             // [0, 0, 0, 0, 0]
 * ```
 *
 * ### Nested Structure Traversal
 *
 * ```kotlin
 * data class Person(val name: String, val age: Int)
 * data class Team(val members: List<Person>)
 *
 * val membersLens = Lens<Team, List<Person>>(
 *     getter = { it.members },
 *     setter = { team, members -> team.copy(members = members) }
 * )
 *
 * val eachPerson = Traversal<List<Person>, Person>(
 *     getter = { it },
 *     modify = { list, f -> list.map(f) }
 * )
 *
 * val ageLens = Lens<Person, Int>(
 *     getter = { it.age },
 *     setter = { person, age -> person.copy(age = age) }
 * )
 *
 * // Compose to get all ages in a team
 * val allAges = membersLens andThen eachPerson andThen ageLens
 *
 * val team = Team(listOf(
 *     Person("Alice", 30),
 *     Person("Bob", 25)
 * ))
 *
 * allAges.get(team)                    // [30, 25]
 * allAges.modify(team) { it + 1 }      // Everyone ages by 1 year
 * ```
 *
 * ### Filtered Traversal
 *
 * ```kotlin
 * val adults = Traversal<List<Person>, Person>(
 *     getter = { list -> list.filter { it.age >= 18 } },
 *     modify = { list, f -> list.map { if (it.age >= 18) f(it) else it } }
 * )
 *
 * adults.get(people)                // Only adults
 * adults.modify(people) { person -> // Only modify adults
 *     person.copy(name = person.name.uppercase())
 * }
 * ```
 *
 * ## Traversal vs Other Optics
 *
 * - **Lens**: Focuses on exactly 1 element (always)
 * - **Prism**: Focuses on 0 or 1 element (sum type variant)
 * - **Optional**: Focuses on 0 or 1 element (may be missing)
 * - **Traversal**: Focuses on 0+ elements (most general)
 *
 * ## Composition
 *
 * - `Traversal andThen Traversal = Traversal` - Nested collections (list of lists)
 * - `Traversal andThen Lens = Traversal` - Field of each element
 * - `Lens andThen Traversal = Traversal` - Collection field, then each element
 * - `Optional andThen Traversal = Traversal` - If present, then multiple elements
 * - `Prism andThen Traversal = Traversal` - If matches variant, then multiple elements
 *
 * ## Common Patterns
 *
 * ### Pair Both
 *
 * ```kotlin
 * val both = Traversal<Pair<Int, Int>, Int>(
 *     getter = { listOf(it.first, it.second) },
 *     modify = { pair, f -> Pair(f(pair.first), f(pair.second)) }
 * )
 *
 * both.get(Pair(1, 2))              // [1, 2]
 * both.modify(Pair(1, 2)) { it * 2 }  // Pair(2, 4)
 * ```
 *
 * ### Map Values
 *
 * ```kotlin
 * val mapValues = Traversal<Map<String, Int>, Int>(
 *     getter = { it.values.toList() },
 *     modify = { map, f -> map.mapValues { (_, v) -> f(v) } }
 * )
 * ```
 *
 * ### Tree Leaves
 *
 * ```kotlin
 * sealed class Tree<A> {
 *     data class Leaf<A>(val value: A) : Tree<A>()
 *     data class Node<A>(val left: Tree<A>, val right: Tree<A>) : Tree<A>()
 * }
 *
 * fun <A> allLeaves() = Traversal<Tree<A>, A>(
 *     getter = { tree ->
 *         when (tree) {
 *             is Tree.Leaf -> listOf(tree.value)
 *             is Tree.Node -> allLeaves<A>().get(tree.left) + allLeaves<A>().get(tree.right)
 *         }
 *     },
 *     modify = { tree, f ->
 *         when (tree) {
 *             is Tree.Leaf -> Tree.Leaf(f(tree.value))
 *             is Tree.Node -> Tree.Node(
 *                 allLeaves<A>().modify(tree.left, f),
 *                 allLeaves<A>().modify(tree.right, f)
 *             )
 *         }
 *     }
 * )
 * ```
 *
 * ## Type Parameters
 *
 * - [S]: Source structure type
 * - [T]: Target structure type (after modification)
 * - [A]: Focus element type (what we read)
 * - [B]: Modified element type (what we write)
 *
 * For monomorphic traversals, use [Traversal]<S, A> = PTraversal<S, S, A, A>
 *
 * @see PLens for single element focus
 * @see POptional for optional single element
 * @see PPrism for sum type variant
 */
data class PTraversal<S, T, A, B>(
    /** Extract all focused elements as a list */
    val getter: (S) -> List<A>,
    /** Modify all focused elements using a transformation function */
    val modify: (S, (A) -> B) -> T
): Setter<S, B, T> {

    /**
     * Get all focused elements.
     *
     * Returns an empty list if there are no elements.
     *
     * Example:
     * ```kotlin
     * eachElement.get(listOf(1, 2, 3))  // [1, 2, 3]
     * eachElement.get(emptyList())      // []
     * ```
     */
    fun get(s: S): List<A> =
        getter(s)

    /**
     * Set all focused elements to the same value.
     *
     * Example:
     * ```kotlin
     * eachElement.set(listOf(1, 2, 3), 0)  // [0, 0, 0]
     * ```
     */
    override fun set(s: S, b: B): T =
        modify(s) { b }

    /**
     * Compose two Traversals for nested collections.
     *
     * Example:
     * ```kotlin
     * val outer: Traversal<List<List<Int>>, List<Int>> = ...
     * val inner: Traversal<List<Int>, Int> = ...
     * val flattened = outer andThen inner  // Traversal<List<List<Int>>, Int>
     *
     * flattened.get(listOf(listOf(1, 2), listOf(3, 4)))  // [1, 2, 3, 4]
     * ```
     */
    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        PTraversal(
            getter = { s -> getter(s).flatMap { a -> other.getter(a) } },
            modify = { s, f -> modify(s) { a -> other.modify(a, f) } }
        )

    /**
     * Compose Traversal with Setter.
     */
    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    /**
     * Compose Traversals in reverse order.
     */
    infix fun <C, D> compose(other: PTraversal<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Lens.
     */
    infix fun <C, D> compose(other: PLens<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Iso.
     */
    infix fun <C, D> compose(other: PIso<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Prism.
     */
    infix fun <C, D> compose(other: PPrism<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Optional.
     */
    infix fun <C, D> compose(other: POptional<C, D, S, T>): PTraversal<C, D, A, B> =
        other andThen this

    /**
     * Convert Traversal to Setter (loses ability to read).
     */
    fun toSetter(): PSetter<S, T, A, B> =
        PSetter(modify)
}

/**
 * Monomorphic Traversal where types don't change.
 *
 * This is the most common case: `Traversal<List<Int>, Int>` for list elements.
 */
typealias Traversal<S, A> = PTraversal<S, S, A, A>