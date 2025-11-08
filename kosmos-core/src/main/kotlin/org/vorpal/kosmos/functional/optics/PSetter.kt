package org.vorpal.kosmos.functional.optics

/**
 * # Setter
 *
 * A **Setter** is a **write-only optic** - the most general optic that can only modify, not read.
 * It's at the bottom of the optics hierarchy and represents pure transformation capability.
 *
 * ## When to Use
 *
 * Use a Setter when you:
 * - Only need to **modify** values, not read them
 * - Want the most general optic that works with any transformation
 * - Are composing optics and only care about the write capability
 * - Need to abstract over "things that can be modified"
 *
 * ## Key Characteristics
 *
 * - **Write-only**: Can modify focus, but cannot read it
 * - **Most general**: Every other optic can convert to a Setter
 * - **Composable**: Setters compose with all other optics
 * - **Safe**: Cannot leak information (useful for encapsulation)
 *
 * ## Examples
 *
 * ### Simple Setter
 *
 * ```kotlin
 * data class Person(val name: String, val age: Int)
 *
 * // A setter that only allows modifying age, not reading it
 * val ageSetter = PSetter<Person, Person, Int, Int> { person, f ->
 *     person.copy(age = f(person.age))
 * }
 *
 * val person = Person("Alice", 30)
 *
 * // Can modify
 * ageSetter.modify(person) { it + 1 }    // Person("Alice", 31)
 * ageSetter.set(person, 25)              // Person("Alice", 25)
 *
 * // Cannot read! (No get method)
 * // ageSetter.get(person)  // Compile error
 * ```
 *
 * ### Contravariant Mapping
 *
 * Setters shine when you want to transform containers without exposing their contents:
 *
 * ```kotlin
 * data class Box<A>(val value: A)
 *
 * // Can transform contents without revealing what's inside
 * fun <A, B> boxSetter() = PSetter<Box<A>, Box<B>, A, B> { box, f ->
 *     Box(f(box.value))
 * }
 *
 * val intBox = Box(42)
 * boxSetter<Int, String>().modify(intBox) { it.toString() }  // Box("42")
 * ```
 *
 * ### Why Use Setter Over Lens?
 *
 * Sometimes you want to enforce write-only access for encapsulation:
 *
 * ```kotlin
 * class SecureConfig(private var password: String) {
 *     // Expose only a setter - clients can change password but not read it
 *     val passwordSetter = PSetter<SecureConfig, SecureConfig, String, String> { config, f ->
 *         config.apply { password = f(password) }
 *     }
 * }
 *
 * config.passwordSetter.modify(config) { hash(it) }  // Can hash
 * // But cannot read the password!
 * ```
 *
 * ## Setter vs Other Optics
 *
 * All optics can become Setters (they lose read capability):
 *
 * ```
 * Iso ─────> Setter  (lose bidirectionality)
 * Lens ────> Setter  (lose get)
 * Prism ───> Setter  (lose getOrNull)
 * Optional -> Setter  (lose getOrNull)
 * Traversal > Setter  (lose get all)
 * ```
 *
 * ## Composition
 *
 * Setters compose with everything (result is always a Setter):
 *
 * - `Setter andThen Setter = Setter`
 * - `Lens andThen Setter = Setter`
 * - `Prism andThen Setter = Setter`
 * - `Optional andThen Setter = Setter`
 * - `Traversal andThen Setter = Setter`
 *
 * Once you compose with a Setter, you can only modify, never read.
 *
 * ## Examples of Converting to Setter
 *
 * ```kotlin
 * val nameLens: Lens<Person, String> = ...
 * val ageLens: Lens<Person, Int> = ...
 *
 * // Convert to setter - now you can't read name
 * val nameSetter: Setter<Person, String> = nameLens.toSetter()
 *
 * // Compose setters
 * data class Company(val ceo: Person)
 * val ceoLens: Lens<Company, Person> = ...
 * val ceoAgeSetter = ceoLens.toSetter() andThen ageLens.toSetter()
 *
 * company.modify(ceoAgeSetter) { it + 1 }  // Increment CEO's age
 * // But cannot read the CEO's age through this optic
 * ```
 *
 * ## Practical Use: Batch Updates
 *
 * Setters are perfect for "update strategies" where you don't care about reading:
 *
 * ```kotlin
 * data class Config(val timeout: Int, val retries: Int, val verbose: Boolean)
 *
 * val timeoutSetter = PSetter<Config, Config, Int, Int> { cfg, f ->
 *     cfg.copy(timeout = f(cfg.timeout))
 * }
 *
 * val retriesSetter = PSetter<Config, Config, Int, Int> { cfg, f ->
 *     cfg.copy(retries = f(cfg.retries))
 * }
 *
 * // Apply multiple updates
 * fun applyUpdates(config: Config, updates: List<PSetter<Config, Config, Int, Int>>): Config =
 *     updates.fold(config) { cfg, setter -> setter.modify(cfg) { it * 2 } }
 * ```
 *
 * ## Type Parameters
 *
 * - [S]: Source structure type
 * - [T]: Target structure type (after modification)
 * - [A]: Focus type before modification
 * - [B]: Focus type after modification
 *
 * For monomorphic setters, the type alias is Setter<S, A> = PSetter<S, S, A, A> (not defined here as it conflicts with the interface)
 *
 * @see PLens for read-write access
 * @see PTraversal for modifying multiple elements
 */
data class PSetter<S, T, A, B>(
    /** Modify the focus using a transformation function */
    val modify: (S, (A) -> B) -> T
): Setter<S, B, T> {

    /**
     * Set all focuses to a constant value.
     *
     * Example:
     * ```kotlin
     * setter.set(structure, newValue)
     * ```
     */
    override fun set(s: S, b: B): T = modify(s) { b }

    /**
     * Compose two Setters sequentially.
     *
     * Example:
     * ```kotlin
     * val outer: PSetter<Outer, Outer, Middle, Middle> = ...
     * val inner: PSetter<Middle, Middle, Inner, Inner> = ...
     * val composed = outer andThen inner
     *
     * composed.modify(outerValue) { it.transform() }
     * ```
     */
    infix fun <C, D> andThen(other: PSetter<A, B, C, D>): PSetter<S, T, C, D> =
        PSetter { s, f -> modify(s) { a -> other.modify(a, f) } }

    /**
     * Compose Setters in reverse order.
     */
    infix fun <C, D> compose(other: PSetter<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Lens.
     */
    infix fun <C, D> compose(other: PLens<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Optional.
     */
    infix fun <C, D> compose(other: POptional<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Prism.
     */
    infix fun <C, D> compose(other: PPrism<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this

    /**
     * Reverse composition with Traversal.
     */
    infix fun <C, D> compose(other: PTraversal<C, D, S, T>): PSetter<C, D, A, B> =
        other andThen this
}