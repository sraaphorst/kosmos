package org.vorpal.kosmos.datastructures.trie

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * An immutable, serializable wrapper around a MutableRadixTrie.
 */
@Serializable
class ImmutableRadixTrie(
    private val root: MutableRadixTrie
) : Trie by root

/**
 * A RadixTrie implementation.
 */
@Serializable
class MutableRadixTrie : MutableRadixTrieNode(false), Trie {
    /**
     * Create an immutable wrapper so the Trie cannot be mutated further.
     */
    fun toImmutable(): Trie = ImmutableRadixTrie(this)
}

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
}

fun main() {
    val trie = MutableRadixTrie()
    trie.insert("alphabet")
    trie.insert("a")
    trie.insert("alpha")
    trie.insert("alien")
    trie.insert("alpine")
    trie.insert("banana")
    trie.insert("airport")
    trie.insert("airline")
    trie.insert("air")
    trie.insert("airbill")
    trie.insert("ban")
    trie.insert("barn")
    trie.insert("bar")

    val immTrie = trie.toImmutable()
    immTrie.toList().forEach { println(it) }
    println("Words: ${immTrie.wordCount()}, nodes: ${immTrie.nodeCount()}, depth: ${immTrie.depth()}")

    println("\n\nTrie:")
    println(immTrie.toString())

    println("\n\nAscii:")
    println(immTrie.toPrettyString(false))

    if ("alpha" in immTrie && immTrie.contains("alpha"))
        println("alpha in trie")

    // Note that we need the type specifications here or Json will crash.
    println("\n\nIn JSON:")
    val json = Json.encodeToString(immTrie)
    val myTrie: Trie = Json.decodeFromString(json)
    println(Json.encodeToString(immTrie))

    if ("alpha" in myTrie && myTrie.contains("alpha"))
        println("alpha in trie")

    // False
    println(immTrie == myTrie)
    // True
    println(immTrie.toList() == myTrie.toList())
}
