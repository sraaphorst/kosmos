package org.vorpal.kosmos.datastructures.trie

interface Trie<V> {
    operator fun contains(word: String): Boolean
    fun toSequence(): Sequence<String>
    fun toList(): List<String>
    fun toSet(): Set<String>
    fun branchSequence(): Sequence<String>
    fun wordCount(): Int
    fun nodeCount(): Int
    fun depth(): Int
    fun toPrettyString(useAscii: Boolean): String
}
