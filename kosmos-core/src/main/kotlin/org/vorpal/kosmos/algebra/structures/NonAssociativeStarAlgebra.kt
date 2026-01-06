package org.vorpal.kosmos.algebra.structures

interface NonAssociativeStarAlgebra<R : Any, A : Any> :
    NonAssociativeAlgebra<R, A>,
    HasConj<A>
