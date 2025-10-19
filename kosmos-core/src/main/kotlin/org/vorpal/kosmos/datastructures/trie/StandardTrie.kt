package org.vorpal.kosmos.datastructures.trie

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * An immutable, serializable wrapper around a MutableStandardTrie.
 */
@Serializable
class ImmutableStandardTrie(
    private val root: MutableStandardTrieNode
) : Trie by root

/**
 * Simple way to create a standard trie.
 */
fun Trie.Companion.standard() = MutableStandardTrie()

/**
 * A standard trie implementation, where each node represents a single transition of one character.
 */
@Serializable
class MutableStandardTrie : MutableStandardTrieNode(false)

/**
 * Internal node representation for [MutableStandardTrie].
 *
 * Not intended for direct instantiation or external subclassing.
 * Construction is restricted to within this module.
 */
@Serializable
open class MutableStandardTrieNode internal constructor(override var isTerminal: Boolean = false)
    : MutableTrieCoreNode<Char>() {
    override val children: MutableMap<Char, MutableStandardTrieNode> = mutableMapOf()

    override fun insert(word: String) {
        if (word.isEmpty()) isTerminal = true
        else
            children.getOrPut(word.first(), ::MutableStandardTrieNode)
                .insert(word.drop(1))
    }

    override operator fun contains(word: String): Boolean {
        return if (word.isEmpty()) isTerminal
        else children[word.first()]?.contains(word.drop(1)) ?: false
    }

    override fun subTrie(prefix: String): Trie {
        var current = this
        for (c in prefix) current = current.children.get(c) ?: return Trie.Companion.EMPTY
        return current
    }

    override fun allWordsWithPrefix(prefix: String): Sequence<String> {
        return when {
            prefix.isEmpty() ->
                // We've matched the prefix fully — yield everything from here down
                toSequence()
            else -> {
                val first = prefix.first()
                val child = children[first] ?: return emptySequence()
                child.allWordsWithPrefix(prefix.drop(1))
                    .map { first + it }
            }
        }
    }

    override fun filter(predicate: (String) -> Boolean): Trie {
        val filtered = Trie.standard()
        toSequence().filter(predicate).forEach(filtered::insert)
        return filtered.toImmutable()
    }

    override fun startsWith(prefix: String): Boolean {
        return if (prefix.isEmpty()) true
        else children[prefix.first()]?.startsWith(prefix.drop(1)) ?: false
    }

    override fun toImmutable(): Trie = ImmutableStandardTrie(this)
}
