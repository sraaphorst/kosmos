package org.vorpal.kosmos.core.relations

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Op

/**
 * A named binary relation on A. The [symbol] is for pretty-printing and law output.
 */
data class Relation<A : Any>(
    override val symbol: String = Symbols.REL,
    val rel: (A, A) -> Boolean
) : Op {
    constructor(rel: (A, A) -> Boolean) : this(Symbols.REL, rel)

    operator fun invoke(a: A, b: A): Boolean = rel(a, b)

    /** Converse relation Rᵒ(a,b) ≔ R(b,a). */
    fun converse(symbol: String = "${this.symbol}ᵒ"): Relation<A> =
        Relation(symbol) { a, b -> rel(b, a) }

    /** Pointwise conjunction: (R ∧ S)(a,b) ≔ R(a,b) ∧ S(a,b). */
    infix fun and(other: Relation<A>): Relation<A> =
        Relation("(${symbol} ∧ ${other.symbol})") { a, b -> this(a, b) && other(a, b) }

    /** Pointwise disjunction: (R ∨ S)(a,b) ≔ R(a,b) ∨ S(a,b). */
    infix fun or(other: Relation<A>): Relation<A> =
        Relation("(${symbol} ∨ ${other.symbol})") { a, b -> this(a, b) || other(a, b) }

    /** Precompose both arguments by f (contravariant in both slots). */
    fun <B : Any> contramap(f: (B) -> A): Relation<B> =
        Relation(symbol) { x, y -> rel(f(x), f(y)) }
}

/* ---------- Current-relation carriers (no capitalized property names) ---------- */

interface HasRelation<A : Any>       { val relation: Relation<A> }       // usually “≤”
interface HasStrictRelation<A : Any> { val strictRelation: Relation<A> } // usually “<”

/* ---------- Law markers (checked elsewhere; declarative only) ---------- */

// Non-strict (≤) properties
interface Reflexive<A : Any>     : HasRelation<A>
interface Symmetric<A : Any>     : HasRelation<A>
interface Antisymmetric<A : Any> : HasRelation<A>
interface Transitive<A : Any>    : HasRelation<A>
interface Connex<A : Any>        : HasRelation<A>   // total/connected: ∀a,b R(a,b) ∨ R(b,a)

// Strict (<) properties
interface Irreflexive<A : Any>       : HasStrictRelation<A>
interface Asymmetric<A : Any>        : HasStrictRelation<A>
interface TransitiveStrict<A : Any>  : HasStrictRelation<A>

/** Totality on inequality: ∀a≠b. lt(a,b) ∨ lt(b,a). */
interface TotalOnInequality<A : Any> : HasStrictRelation<A>

/* ---------- Concrete structures around a chosen relation ---------- */

/** Preorder: reflexive + transitive. */
interface Preorder<A : Any> : Reflexive<A>, Transitive<A>, HasRelation<A> {
    val le: Relation<A>
    override val relation: Relation<A> get() = le

    /** Induced equivalence: a ≡ b  :⇔  a ≤ b ∧ b ≤ a. */
    val eq: Relation<A> get() = Relation("≡") { a, b -> le(a, b) && le(b, a) }
    fun equivalent(a: A, b: A): Boolean = eq(a, b)
}

/** Poset: preorder + antisymmetry. */
interface Poset<A : Any> : Preorder<A>, Antisymmetric<A> {
    val ge: Relation<A> get() = le.converse()
    /** Strict part: a < b  :⇔  a ≤ b ∧ ¬(b ≤ a). */
    val lt: Relation<A> get() = Relation(Symbols.LESS_THAN) { a, b -> le(a, b) && !le(b, a) }
    val gt: Relation<A> get() = lt.converse()

    fun dual(): Poset<A> = Posets.of(le.converse())
}

/** Equivalence relation given directly. */
interface Equivalence<A : Any> : Reflexive<A>, Symmetric<A>, Transitive<A>, HasRelation<A> {
    val eq: Relation<A>
    override val relation: Relation<A> get() = eq
}

/** Total (linear) order: poset + connex (law). */
interface TotalOrder<A : Any> : Poset<A>

/** Strict order carried by `<`. */
interface StrictOrder<A : Any> : HasStrictRelation<A>, TransitiveStrict<A> {
    val lt: Relation<A>
    override val strictRelation: Relation<A> get() = lt

    /** Recover a non-strict ≤ via an equality/equivalence `eq`: a ≤ b :⇔ a<b ∨ a≡b. */
    fun leFrom(eq: Relation<A>): Relation<A> =
        Relation(Symbols.LESS_THAN_EQ) { a, b -> lt(a, b) || eq(a, b) }
}

/** Total strict order (trichotomy is a law). */
interface TotalStrictOrder<A : Any> : StrictOrder<A>, TotalOnInequality<A>

/* ---------- Lightweight builders ---------- */

object Preorders {
    fun <A : Any> of(le: Relation<A>): Preorder<A> = object : Preorder<A> { override val le = le }
}

object Posets {
    fun <A : Any> of(le: Relation<A>): Poset<A> = object : Poset<A> { override val le = le }
    fun <A : Any> totalOf(le: Relation<A>): TotalOrder<A> = object : TotalOrder<A> { override val le = le }
}

object Equivalences {
    fun <A : Any> of(eq: Relation<A>): Equivalence<A> = object : Equivalence<A> { override val eq = eq }
}

/* ---------- Bridges between ≤ and < ---------- */

object StrictOrders {
    /** From a poset ≤, the canonical strict part: a < b :⇔ a ≤ b ∧ ¬(b ≤ a). */
    fun <A : Any> fromPoset(poset: Poset<A>): StrictOrder<A> =
        object : StrictOrder<A> {
            override val lt: Relation<A> =
                Relation(Symbols.LESS_THAN) { a, b -> poset.le(a, b) && !poset.le(b, a) }
        }

    /** From a total order ≤, produce a total strict order. */
    fun <A : Any> fromTotalOrder(total: TotalOrder<A>): TotalStrictOrder<A> =
        object : TotalStrictOrder<A> {
            override val lt: Relation<A> =
                Relation(Symbols.LESS_THAN) { a, b -> total.le(a, b) && !total.le(b, a) }
        }

    /**
     * From a strict order `<` and an equivalence `eq`, build a poset with
     *   a ≤ b :⇔ a < b ∨ a ≡ b.
     */
    fun <A : Any> toPoset(strict: StrictOrder<A>, eq: Relation<A>): Poset<A> =
        Posets.of(strict.leFrom(eq))

    /** Same bridge but asserting totality in laws. */
    fun <A : Any> toTotalOrder(strict: TotalStrictOrder<A>, eq: Relation<A>): TotalOrder<A> =
        Posets.totalOf(strict.leFrom(eq))
}
