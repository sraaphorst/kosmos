package org.vorpal.kosmos.laws.property

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck

/**
 * This asserts that a structure (e.g., a field) cannot be trivial.
 */
class DistinctIdentitiesLaw<A : Any>(
    private val zero: A,
    private val one: A,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    override val name: String = "distinct identities / nontriviality (1 ≠ 0)"

    override suspend fun test() {
        lawCheck(!eq(zero, one)) {
            buildString {
                appendLine("Nontriviality failed:")
                appendLine("0 = ${pr(zero)}")
                appendLine("1 = ${pr(one)}")
            }
        }
    }
}
