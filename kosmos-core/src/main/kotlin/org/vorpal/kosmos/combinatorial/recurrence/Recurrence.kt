package org.vorpal.kosmos.combinatorial.recurrence

/**
 * A mathematical sequence defined by a recurrence relation.
 *
 * Both [LinearRecurrence] and [NonlinearRecurrence] implement this interface.
 *
 * Every [Recurrence] is a [Sequence], meaning it can be iterated over lazily.
 */
interface Recurrence<T> : Sequence<T> {
    /** Initial terms that seed the recurrence. */
    val initial: List<T>
}
