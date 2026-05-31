package org.vorpal.kosmos.algebra.free

/**
 * A finite word over an alphabet of letters of type [A].
 */
data class Word<A : Any>(
    val letters: List<A>
) {
    val length: Int
        get() = letters.size

    val isEmpty: Boolean
        get() = letters.isEmpty()

    infix fun concatenate(other: Word<A>): Word<A> =
        Word(letters + other.letters)

    fun reversed(): Word<A> =
        Word(letters.reversed())

    fun splitAt(pos: Int): Pair<Word<A>, Word<A>> {
        require(pos in 0..length) {
            "Position must be between 0 and $length, got: $pos"
        }

        return Word(letters.subList(0, pos)) to Word(letters.subList(pos, length))
    }

    fun deconcatenations(): List<Pair<Word<A>, Word<A>>> =
        (0..length).map(::splitAt)

    companion object {
        fun <A : Any> empty(): Word<A> =
            Word(emptyList())
    }
}
