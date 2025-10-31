package org.vorpal.kosmos.functional.optics

import org.vorpal.kosmos.core.Identity

object Lenses {
    // Lens into a pair
    fun <A, B> first(): Lens<Pair<A, B>, A> = Lens(
        getter = { it.first },
        setter = { p, a -> p.copy(first = a) }
    )

    fun <A, B> second(): Lens<Pair<A, B>, B> = Lens(
        getter = { it.second },
        setter = { p, b -> p.copy(second = b) }
    )

    // Lens into a list element
    fun <A> at(index: Int): Optional<List<A>, A> = Optional(
        getterOrNull = { it.getOrNull(index) },
        setter = { list, a ->
            list.toMutableList().apply { set(index, a) }
        },
        identityT = Identity()
    )
}
