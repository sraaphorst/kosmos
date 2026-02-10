package org.vorpal.kosmos.algebra.structures.instances.embeddings

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * Embeddings CD<B> -> CD<CD<B>> (Cayley–Dickson tower), selecting where i_C goes: ±I, ±J, ±K.
 *
 * This is genuinely doing something different than what is happening in `QuaternionEmbeddingKits`
 * and is useful when you want to stay inside the Cayley-Dickson tower generically.
 */
object ComplexIntoQuaternionCDEmbeddings {

    fun <B : Any> embed(
        base: NonAssociativeInvolutiveRing<B>,
        quaternions: NonAssociativeInvolutiveRing<CD<CD<B>>>,
        embedding: AxisSignEmbeddings.AxisSignEmbedding =
            AxisSignEmbeddings.AxisSignEmbedding.canonical
    ): NonAssociativeRingMonomorphism<CD<B>, CD<CD<B>>> {

        val complexLike: NonAssociativeInvolutiveRing<CD<B>> =
            CayleyDickson.usual(base)

        val zeroB = base.zero
        val oneB = base.one

        val zeroC = CD(zeroB, zeroB)
        val oneC  = CD(oneB, zeroB)
        val iC    = CD(zeroB, oneB)

        val qI = CD(iC, zeroC)    // I = (i_C, 0)
        val qJ = CD(zeroC, oneC)  // J = (0, 1)
        val qK = CD(zeroC, iC)    // K = (0, i_C)

        val axisUnit = when (embedding.axis) {
            AxisSignEmbeddings.ImagAxis.I -> qI
            AxisSignEmbeddings.ImagAxis.J -> qJ
            AxisSignEmbeddings.ImagAxis.K -> qK
        }

        val signedAxisUnit =
            if (embedding.sign == AxisSignEmbeddings.Sign.PLUS) axisUnit
            else quaternions.add.inverse(axisUnit)

        fun injectB(b: B): CD<B> =
            CD(b, zeroB)

        fun lift(z: CD<B>): CD<CD<B>> =
            CD(z, zeroC)

        return NonAssociativeRingMonomorphism.of(
            domain = complexLike,
            codomain = quaternions,
            map = UnaryOp { z ->
                val u = z.a
                val v = z.b

                val uQ = lift(injectB(u))
                val vQ = lift(injectB(v))

                quaternions.add(uQ, quaternions.mul(vQ, signedAxisUnit))
            }
        )
    }
}
