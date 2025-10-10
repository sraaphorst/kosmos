package org.vorpal.kosmos.combinatorial.recurrence

/**
 * A mathematical sequence defined by a recurrence relation.
 *
 * Every [Recurrence] is a [Sequence], meaning it can be iterated over lazily.
 */
interface Recurrence<T> : Sequence<T>
