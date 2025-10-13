package org.vorpal.kosmos.categories

interface Category<Obj, Mor : Morphism<Obj, Obj>> {
    val id: (Obj) -> Mor
    val compose: (Mor, Mor) -> Mor
}