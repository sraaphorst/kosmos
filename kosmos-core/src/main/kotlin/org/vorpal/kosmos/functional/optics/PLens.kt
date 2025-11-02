
package org.vorpal.kosmos.functional.optics

import org.vorpal.kosmos.core.Identity

/**
 * # Lens
 *
 * A **Lens** focuses on a **part of a structure that always exists**.
 * Think of it as a "getter + setter" pair for accessing and modifying a field in a data structure.
 *
 * ## When to Use
 *
 * Use a Lens when you need to:
 * - Access a field in a product type (like a data class)
 * - Update immutable nested structures
 * - Chain field accesses (`person.address.street`)
 * - Focus on something that's **guaranteed to be there**
 *
 * ## Key Properties
 *
 * ### Lens Laws
 *
 * Every valid Lens must satisfy these laws:
 *
 * 1. **Get-Put**: `lens.set(s, lens.get(s)) == s`
 *    - Setting what you just got doesn't change anything
 *
 * 2. **Put-Get**: `lens.get(lens.set(s, a)) == a`
 *    - Getting what you just set returns the value you set
 *
 * 3. **Put-Put**: `lens.set(lens.set(s, a1), a2) == lens.set(s, a2)`
 *    - Setting twice is the same as setting once (second value wins)
 *
 * ## Examples
 *
 * ```kotlin
 * data class Person(val name: String, val age: Int)
 * data class Company(val ceo: Person, val employees: Int)
 *
 * // Simple lens for Person.name
 * val nameLens = Lens<Person, String>(
 *     getter = { it.name },
 *     setter = { person, newName -> person.copy(name = newName) }
 * )
 *
 * val person = Person("Alice", 30)
 * nameLens.get(person)              // "Alice"
 * nameLens.set(person, "Bob")       // Person("Bob", 30)
 * nameLens.modify(person) { it.uppercase() }  // Person("ALICE", 30)
 *
 * // Composing lenses for nested access
 * val ceoLens = Lens<Company, Person>(
 *     getter = { it.ceo },
 *     setter = { company, newCeo -> company.copy(ceo = newCeo) }
 * )
 *
 * val ceoNameLens = ceoLens andThen nameLens
 *
 * val company = Company(Person("Alice", 30), 100)
 * ceoNameLens.get(company)                  // "Alice"
 * ceoNameLens.set(company, "Bob")           // Company(Person("Bob", 30), 100)
 * ```
 *
 * ## Lens vs Other Optics
 *
 * - **Lens**: Focus always exists (product type field)
 * - **Prism**: Focus may not exist (sum type variant)
 * - **Optional**: Focus may be missing (nullable field, map lookup)
 * - **Traversal**: Multiple focuses (list elements)
 *
 * ## Composition
 *
 * - `Lens andThen Lens = Lens` - Chain field accesses
 * - `Lens andThen Optional = Optional` - Access might fail deeper
 * - `Lens andThen Prism = Optional` - Access sum type variant
 * - `Lens andThen Traversal = Traversal` - Focus becomes multiple elements
 *
 * ## Type Parameters
 *
 * - [S]: Source type (input structure before modification)
 * - [T]: Target type (output structure after modification)
 * - [A]: Focus type before modification (what we view)
 * - [B]: Focus type after modification (what we set)
 *
 * For monomorphic lenses where types don't change, use [Lens]<S, A> = PLens<S, S, A, A>
 *
 * @see POptional for focuses that might not exist
 * @see PPrism for sum type focuses
 * @see PTraversal for multiple focuses
 */
class PLens<S, T, A, B>(
    /** Extracts the focus from the structure */
    private val getter: (S) -> A,
    /** Updates the focus in the structure */
    private val setter: (S, B) -> T
): Getter<S, A>, Setter<S, B, T> {

    /** Read the focus from structure S */
    override fun get(s: S): A = getter(s)

    /** Write focus B into structure S, returning updated structure T */
    override fun set(s: S, b: B): T = setter(s, b)

    /**
     * Modify the focus using a function.
     *
     * This is often more convenient than `set` when you want to transform
     * the existing value.
     *
     * Example:
     * ```kotlin
     * ageLens.modify(person) { it + 1 }  // Increment age
     * ```
     */
    fun modify(s: S, f: (A) -> B): T =
        set(s, f(get(s)))

    /**
     * Compose two lenses to focus deeper into a structure.
     *
     * Example:
     * ```kotlin
     * val addressLens: Lens<Person, Address> = ...
     * val streetLens: Lens<Address, String> = ...
     * val personStreetLens = addressLens andThen streetLens
     * ```
     *
     * Now `personStreetLens` can read/write the street field directly from a Person.
     */
    infix fun <C, D> andThen(other: PLens<A, B, C, D>): PLens<S, T, C, D> =
        PLens(
            getter = { s -> other.get(get(s)) },
            setter = { s, d -> set(s, other.set(get(s), d)) }
        )

    /**
     * Compose a Lens with a Traversal.
     *
     * Results in a Traversal that focuses on multiple elements reached via the lens.
     *
     * Example:
     * ```kotlin
     * val employeesLens: Lens<Company, List<Person>> = ...
     * val eachPerson: Traversal<List<Person>, Person> = ...
     * val allEmployees = employeesLens andThen eachPerson
     * ```
     */
    infix fun <C, D> andThen(other: PTraversal<A, B, C, D>): PTraversal<S, T, C, D> =
        PTraversal(
            getter = { s -> other.getter(get(s)) },
            modify = { s, f -> set(s, other.modify(get(s), f)) }
        )

    /**
     * Compose a Lens with a Setter (write-only optic).
     *
     * Results in a Setter since you lose the ability to read.
     */
    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        toSetter() andThen other

    /**
     * Compose lenses in reverse order.
     *
     * `a compose b` is equivalent to `b andThen a`.
     */
    infix fun <C, D> compose(other: PLens<C, D, S, T>): PLens<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Prism.
     *
     * Results in Optional since Prism might fail to match.
     */
    infix fun <C, D> compose(other: PPrism<C, D, S, T>): POptional<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Iso.
     *
     * Results in a Lens since Iso always succeeds.
     */
    infix fun <C, D> compose(other: PIso<C, D, S, T>): PLens<C, D, A, B> =
        other andThen this

    /**
     * Convert this Lens to a Traversal that focuses on exactly one element.
     */
    fun toTraversal(): PTraversal<S, T, A, B> = PTraversal(
        getter = { s -> listOf(get(s)) },
        modify = { s, f -> modify(s, f) }
    )

    /**
     * Convert this Lens to a Setter (loses the ability to read).
     */
    fun toSetter(): PSetter<S, T, A, B> =
        PSetter(this::modify)
}

/**
 * Monomorphic Lens where structure and focus types don't change.
 *
 * This is the most common case: `Lens<Person, String>` for accessing a String field.
 */
typealias Lens<S, A> = PLens<S, S, A, A>

/**
 * Convert a monomorphic Lens to an Optional.
 *
 * Since a Lens always succeeds, the Optional's `getOrNull` never returns null.
 * This is useful for composing with other Optionals.
 */
fun <S, A> Lens<S, A>.toOptional(): Optional<S, A> = Optional.of(
    getterOrNull = { s -> get(s) },
    setter = this::set
)

/**
 * Invoke a lens as a function to read the focus.
 *
 * Example: `nameLens(person)` instead of `nameLens.get(person)`
 */
operator fun <S, A> Lens<S, A>.invoke(s: S): A = get(s)

/**
 * Create a function that sets a specific value.
 *
 * Example:
 * ```kotlin
 * val setAge30 = ageLens.setTo(30)
 * val updatedPerson = setAge30(person)
 * ```
 */
fun <S, A> Lens<S, A>.setTo(a: A): (S) -> S = { s -> set(s, a) }

/**
 * Compose a monomorphic Lens with an Optional.
 *
 * Results in an Optional since the second optic might fail.
 *
 * This requires the Lens to be monomorphic (S = S) to properly handle the identity case.
 */
infix fun <S, A, C, D> Lens<S, A>.andThen(other: POptional<A, A, C, D>): POptional<S, S, C, D> =
    POptional(
        getterOrNull = { s -> other.getOrNull(get(s)) },
        setter = { s, d ->
            val a = get(s)
            set(s, other.set(a, d))
        },
        identityT = Identity()
    )