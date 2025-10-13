package org.vorpal.kosmos.frameworks.sequence

/**
 * A method that clears out any memory associated with this recurrence, e.g. if any caching is done.
 * In the default case, it does nothing. It is declared here so that all subclasses of Recurrence can
 * safely have a clear method called on them, even if they do nothing.
 */
fun Recurrence<*>.clear() {
    if (this is CachedRecurrence<*>) {
        clearRecurrenceCache()
    }
    if (this is CachedClosedForm<*>) {
        clearClosedFormCache()
    }
}
