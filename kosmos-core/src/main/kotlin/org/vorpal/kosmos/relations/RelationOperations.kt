package org.vorpal.kosmos.relations

/**
 * Derive the converse relation from a given relation over one type.
 */
fun <A> Relation<A>.converse(): Relation<A> = object : Relation<A> {
    override fun rel(a: A, b: A) = this@converse.rel(b, a)
    override operator fun invoke(a: A, b: A) = this@converse.invoke(b, a)
}
