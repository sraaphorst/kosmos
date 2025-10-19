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
    fun toPrettyString(useAscii: Boolean): String

    fun isEmpty(): Boolean = wordCount() == 0
    fun isNotEmpty(): Boolean = !isEmpty()
    fun startsWith(prefix: String): Boolean =
        toSequence().any { it.startsWith(prefix) }

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

    companion object {
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
            override fun subTrie(prefix: String): Trie = this
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

    // In RadixTrieNode
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
