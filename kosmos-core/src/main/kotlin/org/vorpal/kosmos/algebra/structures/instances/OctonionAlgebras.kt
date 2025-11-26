package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.QuaternionDivisionRing
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.InvolutiveAlgebra
import org.vorpal.kosmos.algebra.structures.LeftRModule
import org.vorpal.kosmos.algebra.structures.NonAssociativeDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.QuaternionModule
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.asQuaternion
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.normSq
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.quaternion
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.w
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.x
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.y
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.z
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

typealias Octonion = CD<Quaternion>

object OctonionAlgebras {
    fun Octonion.normSq(): Real =
        a.normSq() + b.normSq()

    /**
     * In this case, the most we can say about the Octonions are that they are an [NonAssociativeDivisionAlgebra].
     *
     * We get everything but the reciprocal from the [InvolutiveAlgebra] returned by the [CayleyDickson] construction.
     */
    object OctonionDivisionAlgebra : NonAssociativeDivisionAlgebra<Octonion> {
        private val base = CayleyDickson(QuaternionDivisionRing)
        override val add: AbelianGroup<Octonion> = base.add
        override val mul: NonAssociativeMonoid<Octonion> = base.mul
        override fun fromBigInt(n: BigInteger) = base.fromBigInt(n)
        override val conj: Endo<Octonion> = base.conj
        override val reciprocal: Endo<Octonion> = Endo(Symbols.SLASH) { o ->
            val n2 = o.normSq()
            require(n2 != 0.0) { "Zero has no multiplicative inverse in ${Symbols.BB_O}."}

            val oc = conj(o)
            val scale = 1.0 / n2

            // Use the QuaternionModule's action to scale.
            // We could use OctonionModule, but we fall back to QuaternionModule to avoid circular dependencies.
            Octonion(QuaternionModule.action(scale,oc.a), QuaternionModule.action(scale,oc.b))
        }

        val one = octonion(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // quaternion 1 in "a"
        val e1 = octonion(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // quaternion i in "a"
        val e2 = octonion(0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0) // quaternion j in "a"
        val e3 = octonion(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0) // quaternion k in "a"
        val e4 = octonion(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0) // quaternion 1 in "b"
        val e5 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0) // quaternion i in "b"
        val e6 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0) // quaternion j in "b"
        val e7 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0) // quaternion k in "b"
    }

    val Octonion.w: Double get() = a.w
    val Octonion.x: Double get() = a.x
    val Octonion.y: Double get() = a.y
    val Octonion.z: Double get() = a.z

    val Octonion.u: Double get() = b.w
    val Octonion.v: Double get() = b.x
    val Octonion.s: Double get() = b.y
    val Octonion.t: Double get() = b.z

    fun octonion(
        w: Double,
        x: Double,
        y: Double,
        z: Double,
        u: Double,
        v: Double,
        s: Double,
        t: Double
    ): Octonion {
        val a = quaternion(w, x, y, z)
        val b = quaternion(u, v, s, t)
        return Octonion(a, b)
    }

    val OctonionModule: RModule<Real, Octonion> = RModule.of(
        ring = RealField,
        group = OctonionDivisionAlgebra.add,
        action = Action { r, o -> octonion(
            r * o.w, r * o.x, r * o.y, r * o.z,
            r * o.u, r * o.v, r * o.s, r * o.t)
        }
    )

    /**
     * Embed a quaternion number into an octonion.
     */
    fun Quaternion.asOctonion(): Octonion =
        octonion(w, x, y, z, 0.0, 0.0, 0.0, 0.0)

    val OctonionLeftQuaternionModule: LeftRModule<Quaternion, Octonion> = LeftRModule.of(
        leftRing = QuaternionDivisionRing,
        group = OctonionDivisionAlgebra.add,
        leftAction = Action { q, o ->
            // Embed q into 𝕆 as (q, 0) and then multiply on the left.
            OctonionDivisionAlgebra.mul.op(q.asOctonion(), o)
        }
    )

    val OctonionLeftComplexModule: LeftRModule<Complex, Octonion> = LeftRModule.of(
        leftRing = ComplexAlgebras.ComplexField,
        group = OctonionDivisionAlgebra.add,
        leftAction = Action { c, o ->
            OctonionDivisionAlgebra.mul.op(c.asQuaternion().asOctonion(), o)
        }
    )
}
