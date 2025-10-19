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
}

/**
 * The core functionality for a mutable trie node, shared between
 * the trie implementations.
 */
@Serializable
sealed class MutableTrieCoreNode<K : Comparable<K>> : Trie {
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
