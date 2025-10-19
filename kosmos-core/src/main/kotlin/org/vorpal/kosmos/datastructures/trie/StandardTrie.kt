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

    override fun toImmutable(): Trie = ImmutableStandardTrie(this)
}

fun main() {
    val trie = Trie.standard()
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
