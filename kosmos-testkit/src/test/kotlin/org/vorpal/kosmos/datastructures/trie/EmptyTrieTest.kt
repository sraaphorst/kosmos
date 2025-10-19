package org.vorpal.kosmos.datastructures.trie

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.types.shouldBeSameInstanceAs

class EmptyTrieTest : FunSpec({

    test("EMPTY trie should not contain any word") {
        Trie.Companion.EMPTY.contains("") shouldBe false
        Trie.Companion.EMPTY.contains("a") shouldBe false
        Trie.Companion.EMPTY.contains("hello") shouldBe false
    }

    test("EMPTY trie should have zero word count") {
        Trie.Companion.EMPTY.wordCount() shouldBe 0
    }

    test("EMPTY trie should have zero node count") {
        Trie.Companion.EMPTY.nodeCount() shouldBe 0
    }

    test("EMPTY trie should have zero depth") {
        Trie.Companion.EMPTY.depth() shouldBe 0
    }

    test("EMPTY trie should be empty") {
        Trie.Companion.EMPTY.isEmpty() shouldBe true
        Trie.Companion.EMPTY.isNotEmpty() shouldBe false
    }

    test("EMPTY trie should not start with any prefix") {
        Trie.Companion.EMPTY.startsWith("") shouldBe false
        Trie.Companion.EMPTY.startsWith("a") shouldBe false
        Trie.Companion.EMPTY.startsWith("test") shouldBe false
    }

    test("EMPTY trie toSequence should be empty") {
        Trie.Companion.EMPTY.toSequence().toList().shouldBeEmpty()
    }

    test("EMPTY trie toList should be empty") {
        Trie.Companion.EMPTY.toList().shouldBeEmpty()
    }

    test("EMPTY trie toSet should be empty") {
        Trie.Companion.EMPTY.toSet().shouldBeEmpty()
    }

    test("EMPTY trie branchSequence should be empty") {
        Trie.Companion.EMPTY.branchSequence().toList().shouldBeEmpty()
    }

    test("EMPTY trie subTrie should return itself") {
        Trie.Companion.EMPTY.subTrie("") shouldBeSameInstanceAs Trie.Companion.EMPTY
        Trie.Companion.EMPTY.subTrie("a") shouldBeSameInstanceAs Trie.Companion.EMPTY
        Trie.Companion.EMPTY.subTrie("prefix") shouldBeSameInstanceAs Trie.Companion.EMPTY
    }

    test("EMPTY trie allWordsWithPrefix should be empty") {
        Trie.Companion.EMPTY.allWordsWithPrefix("").toList().shouldBeEmpty()
        Trie.Companion.EMPTY.allWordsWithPrefix("a").toList().shouldBeEmpty()
    }

    test("EMPTY trie countWithPrefix should be zero") {
        Trie.Companion.EMPTY.countWithPrefix("") shouldBe 0
        Trie.Companion.EMPTY.countWithPrefix("test") shouldBe 0
    }

    test("EMPTY trie filter should return itself") {
        Trie.Companion.EMPTY.filter { true } shouldBeSameInstanceAs Trie.Companion.EMPTY
        Trie.Companion.EMPTY.filter { false } shouldBeSameInstanceAs Trie.Companion.EMPTY
        Trie.Companion.EMPTY.filter { it.length > 3 } shouldBeSameInstanceAs Trie.Companion.EMPTY
    }

    test("EMPTY trie prefixNGrams should be empty") {
        Trie.Companion.EMPTY.prefixNGrams(1).toList().shouldBeEmpty()
        Trie.Companion.EMPTY.prefixNGrams(2).toList().shouldBeEmpty()
        Trie.Companion.EMPTY.prefixNGrams(5).toList().shouldBeEmpty()
    }

    test("EMPTY trie longestCommonPrefix should be empty string") {
        Trie.Companion.EMPTY.longestCommonPrefix() shouldBe ""
    }

    test("EMPTY trie toPrettyString should be empty") {
        Trie.Companion.EMPTY.toPrettyString(true) shouldBe ""
        Trie.Companion.EMPTY.toPrettyString(false) shouldBe ""
    }
})