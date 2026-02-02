package org.vorpal.kosmos.bridge

import org.vorpal.kosmos.algebra.morphisms.GroupHomomorphism
import org.vorpal.kosmos.algebra.morphisms.GroupIsomorphism
import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.combinatorics.Permutation
import org.vorpal.kosmos.combinatorics.instances.PermutationAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.functional.datastructures.getOrElse
import org.vorpal.kosmos.linear.instances.PermMatAlgebras
import org.vorpal.kosmos.linear.instances.PermutationMatrixAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.PermMat

object PermutationMatrixIsomorphisms {

    /**
     * S_n as Permutation<Int>  ≅  S_n as PermMat
     */
    fun snPermutationToPermMat(n: Int): GroupIsomorphism<Permutation<Int>, PermMat> {
        require(n >= 0) { "n must be nonnegative: $n" }

        val domain = FiniteSet.ordered(0 until n)

        // This must be the group whose op matches Permutation.andThen.
        val snPerm: Group<Permutation<Int>> =
            PermutationAlgebras.symmetricGroup(domain)

        val snPermMat: Group<PermMat> =
            PermMatAlgebras.symmetricGroup(n)

        val forward = GroupHomomorphism.of(
            domain = snPerm,
            codomain = snPermMat
        ) { p ->
            with(PermutationBridge) { p.toPermMat() }
        }

        val backward = GroupHomomorphism.of(
            domain = snPermMat,
            codomain = snPerm
        ) { pm ->
            with(PermutationBridge) { pm.toPermutation() }
        }

        return GroupIsomorphism.of(forward, backward)
    }

    /**
     * S_n as PermMat  ≅  S_n as DenseMat<A> (with the ∘ operation defined in PermutationMatrixAlgebras.symmetric)
     */
    fun <A : Any> snPermMatToDenseMat(
        semiring: Semiring<A>,
        n: Int,
        eq: Eq<A> = Eq.default()
    ): GroupIsomorphism<PermMat, DenseMat<A>> {
        require(n >= 0) { "n must be nonnegative: $n" }

        val snPermMat: Group<PermMat> =
            PermMatAlgebras.symmetricGroup(n)

        val snDense: Group<DenseMat<A>> =
            PermutationMatrixAlgebras.symmetricGroup(semiring, n, eq)

        val forward = GroupHomomorphism.of(
            domain = snPermMat,
            codomain = snDense
        ) { pm ->
            with(PermutationBridge) { pm.toDenseMat(semiring) }
        }

        val backward = GroupHomomorphism.of(
            domain = snDense,
            codomain = snPermMat
        ) { m ->
            // Since snDense is a group of permutation matrices (by construction),
            // conversion should succeed, but we keep a safe fallback.
            with(PermutationBridge) {
                m.toPermMat(
                    zero = semiring.add.identity,
                    one = semiring.mul.identity,
                    eq = eq
                ).getOrElse {
                    error("Unreachable: element of permutation-matrix group failed to decode.")
                }
            }
        }

        return GroupIsomorphism.of(forward, backward)
    }

    /**
     * S_n as Permutation<Int>  ≅  S_n as DenseMat<A>
     *
     * Implemented as composition of the two isomorphisms above.
     */
    fun <A : Any> snPermutationToDenseMat(
        semiring: Semiring<A>,
        n: Int,
        eq: Eq<A> = Eq.default()
    ): GroupIsomorphism<Permutation<Int>, DenseMat<A>> {
        val iso1 = snPermutationToPermMat(n)
        val iso2 = snPermMatToDenseMat(semiring, n, eq)

        val forward = iso1.forward andThen iso2.forward
        val backward = iso2.backward andThen iso1.backward

        return GroupIsomorphism.of(forward, backward)
    }

    /**
     * A_n as Permutation<Int>  ≅  A_n as PermMat
     */
    fun anPermutationToPermMat(n: Int): GroupIsomorphism<Permutation<Int>, PermMat> {
        require(n >= 0) { "n must be nonnegative: $n" }

        val domain = FiniteSet.ordered(0 until n)

        val anPerm =
            PermutationAlgebras.alternatingSubgroup(domain) // returns Subgroup<Permutation<Int>>

        val anPermMat =
            PermMatAlgebras.alternatingSubgroup(n) // returns Subgroup<PermMat>

        val forward = GroupHomomorphism.of(
            domain = anPerm,
            codomain = anPermMat
        ) { p ->
            with(PermutationBridge) { p.toPermMat() }
        }

        val backward = GroupHomomorphism.of(
            domain = anPermMat,
            codomain = anPerm
        ) { pm ->
            with(PermutationBridge) { pm.toPermutation() }
        }

        return GroupIsomorphism.of(forward, backward)
    }

    /**
     * A_n as PermMat  ≅  A_n as DenseMat<A>
     */
    fun <A : Any> anPermMatToDenseMat(
        semiring: Semiring<A>,
        n: Int,
        eq: Eq<A> = Eq.default()
    ): GroupIsomorphism<PermMat, DenseMat<A>> {
        require(n >= 0) { "n must be nonnegative: $n" }

        val anPermMat =
            PermMatAlgebras.alternatingSubgroup(n)

        val anDense =
            PermutationMatrixAlgebras.alternatingSubgroup(semiring, n, eq)

        val forward = GroupHomomorphism.of(
            domain = anPermMat,
            codomain = anDense
        ) { pm ->
            with(PermutationBridge) { pm.toDenseMat(semiring) }
        }

        val backward = GroupHomomorphism.of(
            domain = anDense,
            codomain = anPermMat
        ) { m ->
            with(PermutationBridge) {
                m.toPermMat(
                    zero = semiring.add.identity,
                    one = semiring.mul.identity,
                    eq = eq
                ).getOrElse {
                    error("Unreachable: element of alternating permutation-matrix subgroup failed to decode.")
                }
            }
        }

        return GroupIsomorphism.of(forward, backward)
    }

    /**
     * A_n as Permutation<Int>  ≅  A_n as DenseMat<A>
     *
     * Implemented as composition of the two isomorphisms above.
     */
    fun <A : Any> anPermutationToDenseMat(
        semiring: Semiring<A>,
        n: Int,
        eq: Eq<A> = Eq.default()
    ): GroupIsomorphism<Permutation<Int>, DenseMat<A>> {
        val iso1 = anPermutationToPermMat(n)
        val iso2 = anPermMatToDenseMat(semiring, n, eq)

        val forward = iso1.forward andThen iso2.forward
        val backward = iso2.backward andThen iso1.backward

        return GroupIsomorphism.of(forward, backward)
    }
}