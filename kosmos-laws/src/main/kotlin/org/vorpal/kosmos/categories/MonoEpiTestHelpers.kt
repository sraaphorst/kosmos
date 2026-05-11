package org.vorpal.kosmos.categories

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.finiteset.FiniteSet

fun <A : Any, B : Any> isMonoSet(
    f: Morphism<A, B>,
    domain: FiniteSet<A>,
    eqB: Eq<B> = Eq.default()
): Boolean {
    val xs = domain.toList()
    val img = xs.map(f::apply)
    return xs.indices.none { i ->
        (i + 1 until xs.size).any { j -> eqB(img[i], img[j]) }
    }
}

fun <A : Any, B : Any> isEpiSet(
    f: Morphism<A, B>,
    domain: FiniteSet<A>,
    codomain: FiniteSet<B>,
    eqB: Eq<B> = Eq.default()
): Boolean {
    val image = domain.toList().map(f::apply)
    return codomain.toList().all { b -> image.any { ib -> eqB(ib, b) } }
}

/** Small enumerator: use only for tiny FiniteSets. */
fun <A : Any, B : Any> allFunctions(
    domain: FiniteSet<A>,
    codomain: FiniteSet<B>
): Sequence<Morphism<A, B>> {
    val dom = domain.toList()
    val cod = codomain.toList()
    if (cod.isEmpty() && dom.isNotEmpty()) return emptySequence()

    val assignments: Sequence<List<B>> =
        dom.fold(sequenceOf(emptyList())) { acc, _ ->
            acc.flatMap { partial -> cod.asSequence().map { b -> partial + b } }
        }

    return assignments.map { assignment ->
        val table = dom.zip(assignment).toMap()
        Morphism { a -> table.getValue(a) }
    }
}
