package org.vorpal.kosmos.datastructures.trie

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.types.shouldBeSameInstanceAs

class EmptyTrieSpec : FunSpec({

    test("EMPTY trie should not contain any word") {
        Trie.EMPTY.contains("") shouldBe false
        Trie.EMPTY.contains("a") shouldBe false
        Trie.EMPTY.contains("hello") shouldBe false
    }

    test("EMPTY trie should have zero word count") {
        Trie.EMPTY.wordCount() shouldBe 0
    }

    test("EMPTY trie should have zero node count") {
        Trie.EMPTY.nodeCount() shouldBe 0
    }

    test("EMPTY trie should have zero depth") {
        Trie.EMPTY.depth() shouldBe 0
    }

    test("EMPTY trie should be empty") {
        Trie.EMPTY.isEmpty() shouldBe true
        Trie.EMPTY.isNotEmpty() shouldBe false
    }

    test("EMPTY trie should not start with any prefix") {
        Trie.EMPTY.startsWith("") shouldBe false
        Trie.EMPTY.startsWith("a") shouldBe false
        Trie.EMPTY.startsWith("test") shouldBe false
    }

    test("EMPTY trie toSequence should be empty") {
        Trie.EMPTY.toSequence().toList().shouldBeEmpty()
    }

    test("EMPTY trie toList should be empty") {
        Trie.EMPTY.toList().shouldBeEmpty()
    }

    test("EMPTY trie toSet should be empty") {
        Trie.EMPTY.toSet().shouldBeEmpty()
    }

    test("EMPTY trie branchSequence should be empty") {
        Trie.EMPTY.branchSequence().toList().shouldBeEmpty()
    }

    test("EMPTY trie subTrie should return itself") {
        Trie.EMPTY.subTrie("") shouldBeSameInstanceAs Trie.EMPTY
        Trie.EMPTY.subTrie("a") shouldBeSameInstanceAs Trie.EMPTY
        Trie.EMPTY.subTrie("prefix") shouldBeSameInstanceAs Trie.EMPTY
    }

    test("EMPTY trie allWordsWithPrefix should be empty") {
        Trie.EMPTY.allWordsWithPrefix("").toList().shouldBeEmpty()
        Trie.EMPTY.allWordsWithPrefix("a").toList().shouldBeEmpty()
    }

    test("EMPTY trie countWithPrefix should be zero") {
        Trie.EMPTY.countWithPrefix("") shouldBe 0
        Trie.EMPTY.countWithPrefix("test") shouldBe 0
    }

    test("EMPTY trie filter should return itself") {
        Trie.EMPTY.filter { true } shouldBeSameInstanceAs Trie.EMPTY
        Trie.EMPTY.filter { false } shouldBeSameInstanceAs Trie.EMPTY
        Trie.EMPTY.filter { it.length > 3 } shouldBeSameInstanceAs Trie.EMPTY
    }

    test("EMPTY trie prefixNGrams should be empty") {
        Trie.EMPTY.prefixNGrams(1).toList().shouldBeEmpty()
        Trie.EMPTY.prefixNGrams(2).toList().shouldBeEmpty()
        Trie.EMPTY.prefixNGrams(5).toList().shouldBeEmpty()
    }

    test("EMPTY trie longestCommonPrefix should be empty string") {
        Trie.EMPTY.longestCommonPrefix() shouldBe ""
    }

    test("EMPTY trie toPrettyString should be empty") {
        Trie.EMPTY.toPrettyString(true) shouldBe ""
        Trie.EMPTY.toPrettyString(false) shouldBe ""
    }
})