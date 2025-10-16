package org.vorpal.kosmos.frameworks.lattice.instances

import org.vorpal.kosmos.combinatorics.sequences.Fibonacci
import org.vorpal.kosmos.frameworks.lattice.RecurrenceLattice
import org.vorpal.kosmos.frameworks.lattice.asIndexFunction
import org.vorpal.kosmos.frameworks.lattice.asIndexIdentity
import org.vorpal.kosmos.frameworks.lattice.asIndexPure
import java.math.BigInteger

/**
 * FibonacciLattice — specialization of [RecurrenceLattice] using the
 * global [Fibonacci] sequence as its base recurrence.
 */
val FibonacciLattice: RecurrenceLattice<BigInteger> =
    RecurrenceLattice.of("Fibonacci", Fibonacci) { it }

val F = FibonacciLattice.asIndexFunction()
val F_id = FibonacciLattice.asIndexIdentity()
val Fp1 = FibonacciLattice.asIndexPure { n -> n + 1 }