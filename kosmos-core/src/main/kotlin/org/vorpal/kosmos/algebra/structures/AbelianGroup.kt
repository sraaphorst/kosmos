package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.OpTag
import org.vorpal.kosmos.core.props.Commutative

interface AbelianGroup<A, TAG : OpTag> : Group<A, TAG>, Commutative
