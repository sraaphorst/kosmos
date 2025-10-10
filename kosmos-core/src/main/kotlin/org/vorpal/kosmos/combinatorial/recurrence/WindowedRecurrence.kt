package org.vorpal.kosmos.combinatorial.recurrence

/** Windowed recurrence with explicit initial segment.
 *
 * Both [LinearRecurrence] and [NonlinearRecurrence] implement this interface.
 */
interface WindowedRecurrence<T> : Recurrence<T> {
    /** Initial terms that seed the recurrence. */
    val initial: List<T>
}
