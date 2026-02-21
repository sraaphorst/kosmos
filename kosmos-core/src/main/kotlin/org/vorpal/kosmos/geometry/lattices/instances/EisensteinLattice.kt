package org.vorpal.kosmos.geometry.lattices.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.instances.Eisenstein.EisensteinAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.eisenstein.EisensteinInt
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.geometry.lattices.EuclideanLattice
import org.vorpal.kosmos.linear.values.Vec2
import java.math.BigInteger
import kotlin.math.sqrt

object EisensteinLattice: EuclideanLattice<EisensteinInt, Rational> {
    private val ring = EisensteinAlgebras.EisensteinIntRing

    override val dot: (EisensteinInt, EisensteinInt) -> Rational = { x, y ->
        Rational.of(ring.normSq((ring.add(x, y))) - ring.normSq(x) - ring.normSq(y), BigInteger.TWO)
    }

    override val rank: Int = 2

    override val basis: List<EisensteinInt> = listOf(
        EisensteinInt(BigInteger.ONE, BigInteger.ZERO),
        EisensteinInt(BigInteger.ZERO, BigInteger.ONE)
    )

    override val addV: AbelianGroup<EisensteinInt> = ring.add
    override val scale: LeftAction<BigInteger, EisensteinInt> = LeftAction(Symbols.TRIANGLE_RIGHT) { s, (a, b) ->
        EisensteinInt(s * a, s * b)
    }

    val embed: UnaryOp<EisensteinInt, Vec2<Real>> = UnaryOp { (a, b) ->
        val aReal = a.toReal()
        val bReal = b.toReal()
        Vec2(aReal - bReal / 2.0, bReal * sqrt(3.0) / 2.0)
    }
}
