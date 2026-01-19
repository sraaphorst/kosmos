package org.vorpal.kosmos.analysis.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.analysis.Covector
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction

object CovectorAlgebras {
    private fun <F : Any, V : Any> requireSameSpace(
        actual: VectorSpace<F, V>,
        expected: VectorSpace<F, V>,
    ) {
        require(actual === expected) { "Covectors must be over the same VectorSpace instance." }
    }

    /**
     * [Covector]s on a fixed [space] form a vector space themselves (pointwise add, scalar scaling).
     *
     * This treats [Covector]s as arbitrary functionals; linearity is a semantic promise, not enforced by the type.
     * - This vector space accepts arbitrary functionals F^V, i.e. all functions `V -> F`.
     * - The actual linear functionals V* are a much smaller set where:
     *
     *
     *    ω(au + bv) = aω(u) + bω(v)
     *
     * To represent `V^*` properly in code as `F^n`, we need (at least) one of:
     * - A basis / coordinates for `V`, i.e. an isomorphism `V ≅ F^n`.
     * - A LinearMap representation.
     * Then we can represent a covector by its coefficients (a_1, ..., a_n) and evaluate via dot product in coordinates.
     */
    fun <F : Any, V : Any> functionalVectorSpace(
        space: VectorSpace<F, V>
    ): VectorSpace<F, Covector<F, V>> {
        val field = space.field

        return VectorSpace.of(
            scalars = field,
            add = AbelianGroup.of(
                identity = Covector.of(space) { field.add.identity },
                op = BinOp(Symbols.PLUS) { c1, c2 ->
                    requireSameSpace(c1.space, space)
                    requireSameSpace(c2.space, space)
                    Covector.of(space) { v ->
                        field.add(c1(v), c2(v))
                    }
                },
                inverse = Endo(Symbols.MINUS) { c ->
                    requireSameSpace(c.space, space)
                    Covector.of(space) { v ->
                        field.add.inverse(c(v))
                    }
                }
            ),
            leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { a, c ->
                requireSameSpace(c.space, space)
                Covector.of(space) { v ->
                    field.mul(a, c(v))
                }
            }
        )
    }

    /**
     * TODO: Invent basis machinery later:
     * TODO: basis/coordinates for finite-dimensional spaces to represent V* as F^n.
     *
     * Something like:
     * data class LinearCovector<F: Any, V: Any>(
     *     val space: FiniteVectorSpace<F, V>,
     *     val coords: DenseVec<F>,   // length = dimension
     *     val toCoords: (V) -> DenseVec<F>,  // chosen basis
     * )
     *
     * Even cleaner would be to have an isomorphism VectorSpaceIsomorphism<V, DenseVec<F>>.
     *
     * To represent V^\* as coefficient vectors F^n, you must choose structure you currently don’t have:
     * 	•	a finite dimension (FiniteVectorSpace)
     * 	•	a coordinate isomorphism V \cong F^n (a basis / coordinate chart)
     * 	•	and then you can identify covectors with row vectors (coefficients)
     *
     * Without that data, “a covector is a coefficient vector” is not well-defined.
     *
     * So the “isomorphism version” isn’t just a refactor; it requires new mathematical infrastructure.
     *
     * When we should introduce the coordinate/isomorphism representation
     *
     * The moment you want any of these features in a serious way:
     * 	1.	Gradients as vectors (need an inner product + coordinates or a “sharp” map)
     * 	2.	Jacobians and Hessians as matrices
     * 	3.	Efficient linear-algebra operations on covectors (dot products, matrix–vector, etc.)
     * 	4.	Explicit dual basis computations, transpose maps, etc.
     *
     * That’s when “covectors as coefficient vectors” stops being optional.
     *
     * What the final design will look like (and it won’t delete Covector)
     *
     * We won’t replace Covector. We’ll add a coordinate model alongside it:
     * 	•	Covector<F,V> remains the semantic “thing that acts on vectors”.
     * 	•	Coordinates<F,V> (or Basis<F,V>) provides toCoords: V -> DenseVec<F> and optionally fromCoords.
     * 	•	Then V* can be represented as DenseVec<F> relative to that chosen coordinates.
     *
     * And we provide bridges:
     * 	•	coeffsToCovector(coords, alpha: DenseVec<F>): Covector<F,V>
     * 	•	covectorToCoeffs(coords, ω: Covector<F,V>): DenseVec<F> (only if ω is known to be linear; by construction in this path)
     *
     * So: no temporary hack, just “coordinate-free core now”, “coordinate machinery later”.
     *
     * The exact point on our list when it happens
     *
     * After we finish:
     * 	•	CovectorField + Derivative cleanup (so analysis is stable)
     * 	•	Hypergraphs
     * 	•	then when you want tropical/combinatorial geometry, we’ll likely need bases/coordinates anyway (Newton polytopes, fans, etc.)
     *
     * At that point we add:
     * 	1.	FiniteVectorSpace-based Coordinates/Basis object
     * 	2.	DualSpace view: FiniteVectorSpace<F, DenseVec<F>> for V^\*
     * 	3.	conversion helpers between coefficient covectors and functional covectors
     *
     * So you’ll get the isomorphism model exactly when it becomes useful, not as an abstract “purity” move.
     */
}
