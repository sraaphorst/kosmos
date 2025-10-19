package org.vorpal.kosmos.datastructures.trie

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * An immutable, serializable wrapper around a MutableRadixTrie<V>.
 */
@Serializable
class ImmutableRadixTrie<V>(
    private val root: MutableRadixTrie<V>
) : Trie<V> by root

/**
 * Create a plain RadixTrie with no data attached to the nodes.
 */
fun MutableRadixTrie() = MutableRadixTrie<Unit>()

/**
 * A RadixTrie implementation.
 */
@Serializable
class MutableRadixTrie<V> : MutableRadixTrieNode<V>(false), Trie<V> {
    /**
     * Create an immutable wrapper so the Trie cannot be mutated further.
     */
    fun toImmutable(): Trie<V> = ImmutableRadixTrie(this)
}

/**
 * Internal node representation for [RadixTrie].
 *
 * Not intended for direct instantiation or external subclassing.
 * Construction is restricted to within this module.
 */
@Serializable
open class MutableRadixTrieNode<V> internal constructor(var isTerminal: Boolean = false) {
    val children: MutableMap<String, MutableRadixTrieNode<V>> = mutableMapOf()

    fun insert(word: String) {
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
            val newNode = MutableRadixTrieNode<V>(true)
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
            val sharedNodeTerminal = wordSuffix.isEmpty() //|| isTerminal
            val sharedNode = MutableRadixTrieNode<V>(sharedNodeTerminal)
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

    operator fun contains(word: String): Boolean {
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

    fun toSequence(): Sequence<String> = sequence {
        if (isTerminal) yield("")
        for ((key, node) in children) {
            for (suffix in node.toSequence()) {
                yield(key + suffix)
            }
        }
    }

    fun toList(): List<String> = toSequence().toList()
    fun toSet(): Set<String> = toSequence().toSet()

    /**
     * Return the nodes of the RadixTrie that are branches, i.e. represent the shared prefixes of words in the
     * tree but that are not words themselves.
     */
    fun branchSequence(): Sequence<String> = sequence {
        if (!isTerminal) yield("")
        for ((key, node) in children) {
            for (suffix in node.branchSequence()) {
                yield(key + suffix)
            }
        }
    }

    fun wordCount(): Int =
        (if (isTerminal) 1 else 0) + children.values.sumOf(MutableRadixTrieNode<V>::wordCount)

    fun nodeCount(): Int = 1 + children.values.sumOf(MutableRadixTrieNode<V>::nodeCount)

    fun depth(): Int = 1 + (children.values.maxOfOrNull(MutableRadixTrieNode<V>::depth) ?: 0)

    // In RadixTrieNode
    override fun toString(): String = toPrettyString(useAscii = false)

    fun toPrettyString(useAscii: Boolean = false): String {
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
            sb.append(child.renderPretty(edge, prefix = "", isLast = isLast, tee = tee, el = el, bar = bar, sp = sp))
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
            sb.append(child.renderPretty(childEdge, nextPrefix, i == entries.lastIndex, tee, el, bar, sp))
        }
        return sb.toString()
    }

    fun merge(other: MutableRadixTrieNode<V>) =
        other.toSequence().forEach(::insert)
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

    println("\n\nIn JSON:")
    val json = Json.encodeToString<Trie<Unit>>(immTrie)
    val myTrie = Json.decodeFromString<Trie<Unit>>(json)
    println(Json.encodeToString(immTrie))

    if ("alpha" in myTrie && myTrie.contains("alpha"))
        println("alpha in trie")
}
