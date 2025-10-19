package org.vorpal.kosmos.datastructures.trie

import kotlinx.serialization.Serializable
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@Serializable
sealed interface Trie {
    operator fun contains(word: String): Boolean
    fun toSequence(): Sequence<String>
    fun toList(): List<String>
    fun toSet(): Set<String>
    fun branchSequence(): Sequence<String>
    fun wordCount(): Int
    fun nodeCount(): Int
    fun depth(): Int
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean = !isEmpty()
    fun startsWith(prefix: String): Boolean

    /**
     * Given a prefix, return a trie that recognizes a dictionary where all words
     * are assumed to start with prefix, but with prefix removed. For example, if
     * a trie recognizes:
     * * undead, unite, unsure
     *
     * and prefix "un" is given, the returned trie recognizes:
     * * dead, ite, sure
     */
    fun subTrie(prefix: String): Trie

    /**
     * Generate a sequence of all words with a given prefix from this trie.
     */
    fun allWordsWithPrefix(prefix: String): Sequence<String>

    /**
     * Count the number of words with the specified prefix from this trie.
     */
    fun countWithPrefix(prefix: String): Int =
        allWordsWithPrefix(prefix).count()

    /**
     * Given a predicate on this trie, create a new trie containing only words that match
     * the predicate.
     *
     * Note that in all implementations, filter manifests the entire list
     * of words in the trie upon which it is called, so it can be memory-intensive.
     */
    fun filter(predicate: (String) -> Boolean): Trie

    /**
     * Returns a sequence of all n-grams (consecutive n-character substrings)
     * that appear as word prefixes in this trie.
     */
    fun prefixNGrams(n: Int): Sequence<String> = sequence {
        if (n <= 0) return@sequence
        toSequence().forEach { word ->
            (0..(word.length - n)).forEach { i ->
                yield(word.substring(i, i + n))
            }
        }
    }

    /**
     * Walk down the spine of the trie while there is only one child, accumulating the
     * prefix as we go.
     */
    fun longestCommonPrefix(): String

    /**
     * Render the trie as a tree structure, displaying all nodes, their depths, and whether or not
     * they are terminal points. This can be done using ASCII or UTF8.
     */
    fun toPrettyString(useAscii: Boolean): String

    companion object {
        /**
         * An immutable and empty trie.
         */
        object EMPTY : Trie {
            override operator fun contains(word: String): Boolean = false
            override fun toSequence(): Sequence<String> = emptySequence()
            override fun toList(): List<String> = emptyList()
            override fun toSet(): Set<String> = emptySet()
            override fun branchSequence(): Sequence<String> = emptySequence()
            override fun wordCount(): Int = 0
            override fun nodeCount(): Int = 0
            override fun depth(): Int = 0
            override fun toPrettyString(useAscii: Boolean): String = ""
            override fun isEmpty(): Boolean = true
            override fun startsWith(prefix: String): Boolean = false
            override fun subTrie(prefix: String): Trie = this
            override fun allWordsWithPrefix(prefix: String): Sequence<String> = emptySequence()
            override fun prefixNGrams(n: Int): Sequence<String> = emptySequence()
            override fun longestCommonPrefix(): String = ""
            override fun filter(predicate: (String) -> Boolean): Trie = this
        }
    }
}

sealed interface MutableTrie : Trie {
    fun toImmutable(): Trie
}

/**
 * The core functionality for a mutable trie node, shared between
 * the trie implementations.
 */
@Serializable
sealed class MutableTrieCoreNode<K : Comparable<K>> : MutableTrie {
    abstract val isTerminal: Boolean
    abstract val children: Map<K, MutableTrieCoreNode<K>>

    abstract fun insert(word: String)

    fun merge(other: Trie) =
        other.toList().forEach(::insert)

    override fun toSequence(): Sequence<String> = sequence {
        if (isTerminal) yield("")
        for ((key, node) in children) {
            for (suffix in node.toSequence()) {
                yield(key.toString() + suffix)
            }
        }
    }

    override fun toList(): List<String> = toSequence().toList()
    override fun toSet(): Set<String> = toSequence().toSet()

    /**
     * Return the nodes of the RadixTrie that are branches, i.e. represent the shared prefixes of words in the
     * tree but that are not words themselves.
     */
    override fun branchSequence(): Sequence<String> = sequence {
        if (!isTerminal) yield("")
        for ((key, node) in children) {
            for (suffix in node.branchSequence()) {
                yield(key.toString() + suffix)
            }
        }
    }

    override fun wordCount(): Int =
        (if (isTerminal) 1 else 0) + children.values.sumOf { it.wordCount() }

    override fun nodeCount(): Int = 1 + children.values.sumOf { it.nodeCount() }

    override fun depth(): Int = 1 + (children.values.maxOfOrNull { it.depth() } ?: 0)

    override fun isEmpty(): Boolean = !isTerminal && children.isEmpty()

    override fun longestCommonPrefix(): String =
        if (children.size != 1 || isTerminal) ""
        else {
            val (key, node) = children.entries.first()
            key.toString() + node.longestCommonPrefix()
        }

    override fun toString(): String = toPrettyString(useAscii = false)

    override fun toPrettyString(useAscii: Boolean): String {
        val sb = StringBuilder()
        val tee = if (useAscii) "+--"  else "├── "
        val el  = if (useAscii) "+-- " else "└── "
        val bar = if (useAscii) "|   " else "│   "
        val sp  = if (useAscii) "    " else "    "

        sb.append("<empty>")
        if (isTerminal) sb.append(" *")
        sb.append('\n')

        val entries = children.entries.sortedBy { it.key }
        entries.forEachIndexed { i, (edge, child) ->
            val isLast = i == entries.lastIndex
            sb.append(child.renderPretty(edge.toString(), prefix = "", isLast = isLast, tee = tee, el = el, bar = bar, sp = sp))
        }
        return sb.toString()
    }

    private fun renderPretty(
        edge: String,
        prefix: String,
        isLast: Boolean,
        tee: String,
        el: String,
        bar: String,
        sp: String
    ): String {
        val sb = StringBuilder()
        sb.append(prefix)
        sb.append(if (isLast) el else tee)
        sb.append(edge)
        if (isTerminal) sb.append(" *")
        sb.append('\n')

        // Children get an extended prefix that either continues the bar or spaces it out.
        val nextPrefix = prefix + if (isLast) sp else bar
        val entries = children.entries.sortedBy { it.key }
        entries.forEachIndexed { i, (childEdge, child) ->
            sb.append(child.renderPretty(childEdge.toString(), nextPrefix, i == entries.lastIndex, tee, el, bar, sp))
        }
        return sb.toString()
    }
}
