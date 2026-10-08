package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

/**
 * Transport of algebraic structure from a carrier [A] to a carrier [B].
 *
 * Given maps
 * ```text
 * wrap:   A → B
 * unwrap: B → A
 * ```
 * with `wrap ∘ unwrap = id_B`, every operation on `A` induces an operation on `B`:
 * ```text
 * x ⋆_B y = wrap(unwrap(x) ⋆_A unwrap(y)).
 * ```
 *
 * There are two intended uses:
 * 1. [wrap] and [unwrap] are mutually inverse, i.e. `B` is a *different name* for the same
 *    algebra. For example, `Bicomplex` is a value class over `CD<Complex>`: the arithmetic is the
 *    Cayley-Dickson arithmetic, and the wrapper only exists so that `𝔹` and `ℍ` are distinct Kotlin
 *    types even though both are carried by `CD<Complex>`.
 * 2. [unwrap] is the inclusion of a *substructure* `B ⊆ A` closed under the transported operations,
 *    and [wrap] is the (trusted) corestriction onto `B`. For example, the Hurwitz order
 *    `𝓗 ⊂ ℍ_ℚ` is closed under `+`, `-` and `·`, so its ring structure is transported from
 *    `ℍ_ℚ`. Operations under which the substructure is *not* closed (e.g. the reciprocal) must not
 *    be transported.
 *
 * In both cases the transported structure satisfies the same laws as the original, because [unwrap]
 * is an injective homomorphism for the transported operations.
 */
class CarrierTransport<A : Any, B : Any>(
    val wrap: UnaryOp<A, B>,
    val unwrap: UnaryOp<B, A>
) {
    /**
     * Typically used in:
     * ```
     * newOp = this(op)
     * ```
     * calls to transport the `op` operator from `A` to `B`.
     *
     * Currently unused outside of this class: could be made private.
     */
    operator fun invoke(op: BinOp<A>): BinOp<B> =
        BinOp(op.symbol) { x, y -> wrap(op(unwrap(x), unwrap(y))) }

    /**
     * Typically used in:
     * ```
     * newEndo = this(endo)
     * ```
     * calls to transport the `endo` operator from `A` to `B`.
     *
     * Currently unused outside of this class: could be made private.
     */
    operator fun invoke(endo: Endo<A>): Endo<B> =
        Endo(endo.symbol) { x -> wrap(endo(unwrap(x))) }

    /**
     * Transport a map *out of* the carrier, e.g. a norm `A → N`.
     *
     * Currently unused outside of this class: could be made private.
     */
    fun <N> from(f: UnaryOp<A, N>): UnaryOp<B, N> =
        UnaryOp(f.symbol) { x -> f(unwrap(x)) }

    fun abelianGroup(group: AbelianGroup<A>): AbelianGroup<B> =
        AbelianGroup.of(
            identity = wrap(group.identity),
            op = this(group.op),
            inverse = this(group.inverse)
        )

    fun monoid(monoid: Monoid<A>): Monoid<B> =
        Monoid.of(
            identity = wrap(monoid.identity),
            op = this(monoid.op)
        )

    fun commutativeMonoid(monoid: CommutativeMonoid<A>): CommutativeMonoid<B> =
        CommutativeMonoid.of(
            identity = wrap(monoid.identity),
            op = this(monoid.op)
        )

    fun nonAssociativeMonoid(monoid: NonAssociativeMonoid<A>): NonAssociativeMonoid<B> =
        NonAssociativeMonoid.of(
            identity = wrap(monoid.identity),
            op = this(monoid.op)
        )

    fun nonAssociativeInvolutiveRing(ring: NonAssociativeInvolutiveRing<A>): NonAssociativeInvolutiveRing<B> {
        val transport = this
        return object : NonAssociativeInvolutiveRing<B> {
            override val add = transport.abelianGroup(ring.add)
            override val mul = transport.nonAssociativeMonoid(ring.mul)
            override val conj = transport(ring.conj)

            override fun fromBigInt(n: BigInteger): B =
                wrap(ring.fromBigInt(n))
        }
    }

    fun involutiveRing(ring: InvolutiveRing<A>): InvolutiveRing<B> {
        val transport = this
        return object : InvolutiveRing<B> {
            override val add = transport.abelianGroup(ring.add)
            override val mul = transport.monoid(ring.mul)
            override val conj = transport(ring.conj)
            override val one = mul.identity

            override fun fromBigInt(n: BigInteger): B =
                wrap(ring.fromBigInt(n))
        }
    }

    fun commutativeInvolutiveRing(ring: CommutativeInvolutiveRing<A>): CommutativeInvolutiveRing<B> {
        val transport = this
        return object : CommutativeInvolutiveRing<B> {
            override val add = transport.abelianGroup(ring.add)
            override val mul = transport.commutativeMonoid(ring.mul)
            override val conj = transport(ring.conj)
            override val one = mul.identity

            override fun fromBigInt(n: BigInteger): B =
                wrap(ring.fromBigInt(n))
        }
    }
}
