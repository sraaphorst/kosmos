package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.ops.Mul

class VectorSpaceLaws<S, V>(
    VS: VectorSpace<S, V>,
    arbS: Arb<S>,
    arbV: Arb<V>,
    eqV: Eq<V>
) : RModuleLaws<S, V, AbelianGroup<S, Mul>>(VS, arbS, arbV, eqV)