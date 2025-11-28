package org.vorpal.kosmos.laws

/**
 * A single property / law that can be checked.
 *
 * Implementations should throw assertion failures (Kotest, etc.)
 * if the law does not hold.
 */
interface TestingLaw {
    val name: String
    suspend fun test()
}

/**
 * Utility to run several laws in sequence.
 *
 * If any law fails (throws), the surrounding test framework
 * will report the failure.
 */
suspend fun runLaws(vararg laws: TestingLaw) {
    laws.forEach { t -> t.test() }
}