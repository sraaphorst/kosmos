package org.vorpal.kosmos.laws

interface TestingLaw : Law {
    suspend fun test()
    override suspend fun check(): LawOutcome = throwingCheck {
        test()
    }()
}
