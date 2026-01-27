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
        Relation("$symbol(f)") { x, y -> rel(f(x), f(y)) }
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

/** Totality on inequality (Trichotomy): ∀a≠b. lt(a,b) ∨ lt(b,a). */
interface TotalOnInequality<A : Any> : HasStrictRelation<A>

/* ---------- Concrete structures around a chosen relation ---------- */

/** Preorder: reflexive + transitive. */
interface Preorder<A : Any> : Reflexive<A>, Transitive<A>, HasRelation<A> {
    val le: Relation<A>
    override val relation: Relation<A>
        get() = le

    /** Induced equivalence: a ≡ b  :⇔  a ≤ b ∧ b ≤ a. */
    val eq: Relation<A>
        get() = Relation("≡") { a, b -> le(a, b) && le(b, a) }

    fun equivalent(a: A, b: A): Boolean =
        eq(a, b)

    companion object {
        fun <A : Any> of(le: Relation<A>): Preorder<A> =
            object : Preorder<A> {
                override val le = le
            }
    }
}

/** Poset: preorder + antisymmetry. */
interface Poset<A : Any> : Preorder<A>, Antisymmetric<A> {
    val ge: Relation<A>
        get() = le.converse()

    /** Strict part: a < b  :⇔  a ≤ b ∧ ¬(b ≤ a). */
    val lt: Relation<A>
        get() = Relation(Symbols.LESS_THAN) { a, b -> le(a, b) && !le(b, a) }

    val gt: Relation<A>
        get() = lt.converse()

    fun dual(): Poset<A> =
        of(le.converse())

    /** From a poset ≤, the canonical strict part: a < b :⇔ a ≤ b ∧ ¬(b ≤ a). */
    fun toStrictOrder(): StrictOrder<A> =
        StrictOrder.of(lt)

    companion object {
        fun <A : Any> of(le: Relation<A>): Poset<A> =
            object : Poset<A> {
                override val le = le
            }
    }
}

/** Equivalence relation given directly. */
interface Equivalence<A : Any> : Reflexive<A>, Symmetric<A>, Transitive<A>, HasRelation<A> {
    val eq: Relation<A>
    override val relation: Relation<A>
        get() = eq

    companion object {
        fun <A : Any> of(eq: Relation<A>): Equivalence<A> =
            object : Equivalence<A> {
                override val eq = eq
            }
    }
}

/** Total (linear) order: poset + connex (law). */
interface TotalOrder<A : Any> : Poset<A>, Connex<A> {

    /** From a total order ≤, produce a total strict order <. */
    fun toTotalStrictOrder(): TotalStrictOrder<A> =
        TotalStrictOrder.of(lt)

    companion object {
        fun <A : Any> of(le: Relation<A>): TotalOrder<A> =
            object : TotalOrder<A> {
                override val le = le
            }
    }
}

/** Strict order carried by `<`. */
interface StrictOrder<A : Any> : HasStrictRelation<A>, TransitiveStrict<A> {
    val lt: Relation<A>
    override val strictRelation: Relation<A>
        get() = lt

    /**
     * Recover a non-strict ≤ from this strict order < via an equality/equivalence:
     * `eq`: a ≤ b :⇔ a<b ∨ a≡b.
     */
    fun leFrom(eq: Relation<A>): Relation<A> =
        Relation(Symbols.LESS_THAN_EQ) { a, b -> lt(a, b) || eq(a, b) }

    /**
     * Recover a non-strict ≤ using intrinsic equality (Any.equals).
     */
    fun leDefault(): Relation<A> =
        leFrom(Relation("=") { a, b -> a == b })

    /**
     * From a strict order `<` and an equivalence `eq`, build a poset with
     * a ≤ b :⇔ a < b ∨ a ≡ b.
     */
    fun toPoset(eq: Relation<A>): Poset<A> =
        Poset.of(leFrom(eq))

    companion object {
        fun <A : Any> of(lt: Relation<A>): StrictOrder<A> =
            object : StrictOrder<A> {
                override val lt = lt
            }
    }
}

/** Total strict order (trichotomy is a law). */
interface TotalStrictOrder<A : Any> : StrictOrder<A>, TotalOnInequality<A> {
    /**
     * From a total strict order `<` and an equivalence `eq`, build a poset with
     * a ≤ b :⇔ a < b ∨ a ≡ b.
     */
    fun toTotalOrder(eq: Relation<A>): TotalOrder<A> =
        TotalOrder.of(leFrom(eq))

    companion object {
        fun <A : Any> of(lt: Relation<A>): TotalStrictOrder<A> =
            object : TotalStrictOrder<A> {
                override val lt: Relation<A> = lt
            }
    }
}

/* ---------- Product Constructions ---------- */

/**
 * Creates the component-wise Product Order of two Posets.
 * (a1, b1) ≤ (a2, b2) iff a1 ≤ a2 AND b1 ≤ b2.
 */
fun <A : Any, B : Any> productPoset(pA: Poset<A>, pB: Poset<B>): Poset<Pair<A, B>> {
    val leA = pA.le
    val leB = pB.le
    val leProd = Relation<Pair<A, B>>("(${leA.symbol}×${leB.symbol})") { p1, p2 ->
        leA(p1.first, p2.first) && leB(p1.second, p2.second)
    }
    return Poset.of(leProd)
}

/**
 * Creates the Lexicographical Order of two Total Orders.
 * (a1, b1) ≤ (a2, b2) iff a1 < a2 OR (a1 == a2 AND b1 ≤ b2).
 *
 * Note: Uses strict inequality from A to resolve the first component.
 */
fun <A : Any, B : Any> lexTotalOrder(oA: TotalOrder<A>, oB: TotalOrder<B>): TotalOrder<Pair<A, B>> {
    val ltA = oA.lt
    val eqA = oA.eq
    val leB = oB.le

    val leLex = Relation<Pair<A, B>>("(${oA.le.symbol}ₗₑₓ${oB.le.symbol})") { p1, p2 ->
        val (a1, b1) = p1
        val (a2, b2) = p2
        // a1 < a2 implies ≤
        // a1 == a2 implies we check b
        ltA(a1, a2) || (eqA(a1, a2) && leB(b1, b2))
    }
    return TotalOrder.of(leLex)
}

/* ---------- Adapters ---------- */

/**
 * This allows us to use a Comparator to create a [Relation] over [A] with the [Comparator].
 *
 * We can then do things like this to make a [TotalOrder] on [A].
 * ```
 * val realTotalOrder: TotalOrder<Real> = Posets.totalOf(RealComparator.leRelation())
 * ```
 */
fun <A : Any> Comparator<A>.leRelation(symbol: String = Symbols.LESS_THAN_EQ): Relation<A> =
    Relation(symbol) { a, b -> this.compare(a, b) <= 0 }

/**
 * This allows us to use a [Comparator] to create a [Relation] over [A].
 */
fun <A : Any> Comparator<A>.ltRelation(symbol: String = Symbols.LESS_THAN): Relation<A> =
    Relation(symbol) { a, b -> this.compare(a, b) < 0 }

fun <A : Any> Comparator<A>.eqRelation(symbol: String = Symbols.EQUALS): Relation<A> =
    Relation(symbol) { a, b -> this.compare(a, b) == 0 }