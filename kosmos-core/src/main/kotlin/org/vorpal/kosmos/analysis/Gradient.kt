package org.vorpal.kosmos.analysis

/**
 * We cannot implement gradient yet: to turn a [Covector] `df_p ∈ V^*` into a vector `∇f(p) ∈ V`, we need:
 * - a sharp map `♯_p: V^* → V`
 * - which is the inverse of the flat map `♭_p: V → V^*`, `v ↦ ⟨v,·⟩_p`.
 *
 * If you have an inner product ⟨·,·⟩, you can identify vectors and covectors via the musical isomorphisms
 * flat and sharp.
 *
 * TODO: For now, gradient is unimplemented until we introduce:
 * -`InnerProductSpace` or a `MetricTensor` with invertibility
 * - coordinate / basis machinery (finite-dimensional)
 * - or a linear-solver capability.
 */
fun <F : Any, V : Any> gradient(
    f: ScalarField<F, V>,
    metric: (V) -> (V, V) -> F
): VectorField<F, V> = TODO()
//    VectorField.of(f.space) { p ->
//        metric(p)
//        differential(f) { point, func ->
//            // Build Covector<F, V> at point using the local derivative of f
//            TODO("Implement derivative")
//        }
//        // Map covector to vector via metric inverse (sharp)
//        TODO("Implement sharp isomorphism")
//    }
