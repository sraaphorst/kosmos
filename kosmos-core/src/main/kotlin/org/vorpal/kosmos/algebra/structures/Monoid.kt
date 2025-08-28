package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.OpTag
import org.vorpal.kosmos.core.props.Unital

interface Monoid<A, TAG: OpTag> : Semigroup<A>, Unital<A, TAG>

