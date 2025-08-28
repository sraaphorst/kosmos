package org.vorpal.kosmos.core

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.wrapAsNonEmptyListOrThrow

@arrow.core.PotentiallyUnsafeNonEmptyOperation
fun <E> Iterable<Option<NonEmptyList<E>>>.accumulate(): Option<NonEmptyList<E>> {
    val lst = fold(emptyList<E>()) { acc, outcome -> when (outcome) {
        is None -> acc
        is Some -> acc + outcome.value.toList()
    } }

    // This should never actually throw since the list is guaranteed to be nonempty.
    return if (lst.isEmpty()) None else Some(lst.wrapAsNonEmptyListOrThrow())
}
