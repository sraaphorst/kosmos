package org.vorpal.kosmos.lawkit

import arrow.core.None
import arrow.core.Some
import org.vorpal.kosmos.core.Law
import org.vorpal.kosmos.core.Lawful
import org.vorpal.kosmos.core.accumulate

/**
 * Certify a subject as a type T with respect to the given laws.
 * If any failures occurred during the process, the subject did not meet the conditions required
 * to be a T:L errors that occurred when testing subject are returned. */
@arrow.core.PotentiallyUnsafeNonEmptyOperation
suspend fun <A> certify(subject: A, laws: List<Law>): Lawful<A> {
    val errors = laws.map  { it.check() }.accumulate()
    return when (errors) {
        is None -> Lawful.Pass(subject)
        is Some -> Lawful.Fail(subject, errors.value)
    }
}
