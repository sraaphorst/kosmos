package org.vorpal.kosmos.datastructures.trie

import kotlinx.serialization.Serializable

/**
 * An immutable, serializable wrapper around a MutableRadixTrie.
 */
@Serializable
class ImmutableRadixTrie(
    private val root: MutableRadixTrieNode
) : Trie by root

/**
 * A RadixTrie implementation.
 */
@Serializable
class MutableRadixTrie : MutableRadixTrieNode(false)

/**
 * Simple way to create a radix trie.
 */
fun Trie.Companion.radix() = MutableRadixTrie()

/**
 * Internal node representation for [MutableRadixTrie].
 *
 * Not intended for direct instantiation or external subclassing.
 * Construction is restricted to within this module.
 */
@Serializable
open class MutableRadixTrieNode internal constructor(override var isTerminal: Boolean = false)
    : MutableTrieCoreNode<String>() {
    override val children: MutableMap<String, MutableRadixTrieNode> = mutableMapOf()

    override fun insert(word: String) {
        // If we are done processing the word, mark this node as terminal.
        if (word.isEmpty()) {
            isTerminal = true
            return
        }

        // Otherwise, determine if there is a key in the children that has an intersection with
        // word. If so, determine how far this intersection extends. There cannot be more than one intersection
        // so validChildren will have size 0 or 1.
        val validChildren = children.filter { it.key.commonPrefixWith(word).isNotEmpty() }
        check(validChildren.size <= 1) { "Radix tree is in an illegal state: overlapping children: ${validChildren.keys}" }

        // If there is no child, then we can just create a new node with word and insert it.
        if (validChildren.isEmpty()) {
            val newNode = MutableRadixTrieNode(true)
            children[word] = newNode
            return
        }

        // Otherwise, there is a child. Retrieve it and determine where the two differ, if anywhere.
        val (keyWord, node) = validChildren.entries.first()

        // Determine the shared prefix and the suffixes.
        val prefix = keyWord.commonPrefixWith(word)
        val keyWordSuffix = keyWord.drop(prefix.length)
        val wordSuffix = word.drop(prefix.length)

        // If there is a keyWordSuffix, then we need to create a new node with the shared prefix.
        if (keyWordSuffix.isNotEmpty()) {
            val sharedNodeTerminal = wordSuffix.isEmpty()
            val sharedNode = MutableRadixTrieNode(sharedNodeTerminal)
            sharedNode.children[keyWordSuffix] = node
            children.remove(keyWord)
            children[prefix] = sharedNode
            sharedNode.insert(wordSuffix)
        }
        // Otherwise, the keyWordSuffix is empty, so the node represents word up to its suffix.
        else {
            node.insert(wordSuffix)
        }
    }

    override operator fun contains(word: String): Boolean {
        if (word.isEmpty()) return isTerminal

        // Determine if there is an entry in the children that word starts with.
        // If there is, recurse down into it, and if not, then word is not in the trie.
        val entry = children.entries.find { word.startsWith(it.key) }
        if (entry != null) {
            val (key, node) = entry
            return node.contains(word.drop(key.length))
        } else {
            return false
        }
    }

    /**
     * Returns a trie representing words with [prefix] removed.
     * For RadixTrie, this may create a "virtual" trie structure
     * that doesn't preserve the original edge compression.
     */
    override fun subTrie(prefix: String): Trie {
        if (prefix.isEmpty()) return this

        val (edge, child) = children.entries.find { prefix.startsWith(it.key) || it.key.startsWith(prefix) }
            ?: return Trie.EMPTY

        return when {
            // Exact edge match: continue searching in that subtree
            prefix == edge -> child.subTrie("")
            // Prefix is shorter than the edge: split the edge and descend virtually
            edge.startsWith(prefix) -> {
                // Virtual subtree rooted at the suffix
                val virtualRoot = MutableRadixTrie()
                virtualRoot.children[edge.drop(prefix.length)] = child
                virtualRoot
            }
            // Edge is shorter than the prefix: recurse deeper into child
            prefix.startsWith(edge) -> child.subTrie(prefix.drop(edge.length))
            else -> Trie.EMPTY
        }
    }

    override fun allWordsWithPrefix(prefix: String): Sequence<String> {
        return when {
            prefix.isEmpty() ->
                // We've matched the prefix fully — yield everything from here down
                toSequence()
            else -> {
                // alp
                // alph - yes, stop and return all.
                // al - yes, recurse.
                // First case: there is a node that contains (or is) prefix.
                // Example: prefix = al, child = alp
                // Then we return the entire sequence from the child.
                val childTerminal = children.entries.find { it.key.startsWith(prefix) }
                if (childTerminal != null) return childTerminal.value.toSequence().map { childTerminal.key + it }

                // Second case: prefix contains a child.
                // Example: prefix = alp, child = al
                // Then we recurse on the child with prefix p.
                val childSub = children.entries.find { prefix.startsWith(it.key) }
                if (childSub != null) return childSub.value.allWordsWithPrefix(prefix.drop(childSub.key.length))
                    .map { childSub.key + it }

                // Else neither case: there are no words.
                return emptySequence()
            }
        }
    }

    override fun filter(predicate: (String) -> Boolean): Trie {
        val filtered = Trie.radix()
        toSequence().filter(predicate).forEach(filtered::insert)
        return filtered.toImmutable()
    }

    override fun startsWith(prefix: String): Boolean {
        if (prefix.isEmpty()) return true

        // If there is an entry that contains prefix, true.
        if (children.keys.find { it.startsWith(prefix) } != null) return true

        // Otherwise, find out if there is an entry that prefix contains and recurse to it.
        val entry = children.entries.find { prefix.startsWith(it.key) }
        if (entry == null) return false

        return entry.value.startsWith(prefix.drop(entry.key.length))
    }

    override fun toImmutable(): Trie = ImmutableRadixTrie(this)
}
