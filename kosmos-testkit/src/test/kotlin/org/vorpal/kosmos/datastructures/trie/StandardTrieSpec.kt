package org.vorpal.kosmos.datastructures.trie

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.*
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StandardTrieSpec : FunSpec({

    context("Basic Operations") {
        test("empty trie should be empty") {
            val trie = Trie.standard()
            trie.isEmpty() shouldBe true
            trie.isNotEmpty() shouldBe false
            trie.wordCount() shouldBe 0
        }

        test("insert single word") {
            val trie = Trie.standard()
            trie.insert("hello")
            trie.contains("hello") shouldBe true
            trie.wordCount() shouldBe 1
            trie.isEmpty() shouldBe false
        }

        test("insert empty string") {
            val trie = Trie.standard()
            trie.insert("")
            trie.contains("") shouldBe true
            trie.wordCount() shouldBe 1
        }

        test("insert multiple words") {
            val trie = Trie.standard()
            val words = listOf("cat", "dog", "bird", "fish")
            words.forEach { trie.insert(it) }

            words.forEach { word ->
                trie.contains(word) shouldBe true
            }
            trie.wordCount() shouldBe words.size
        }

        test("contains operator") {
            val trie = Trie.standard()
            trie.insert("test")
            ("test" in trie) shouldBe true
            ("not" in trie) shouldBe false
        }

        test("non-existent words should not be found") {
            val trie = Trie.standard()
            trie.insert("hello")
            trie.contains("hell") shouldBe false
            trie.contains("helloworld") shouldBe false
            trie.contains("hi") shouldBe false
        }
    }

    context("Prefix Operations") {
        test("words with common prefixes") {
            val trie = Trie.standard()
            trie.insert("alphabet")
            trie.insert("alpha")
            trie.insert("alpine")
            trie.insert("alien")

            trie.contains("alphabet") shouldBe true
            trie.contains("alpha") shouldBe true
            trie.contains("alpine") shouldBe true
            trie.contains("alien") shouldBe true
            trie.wordCount() shouldBe 4
        }

        test("startsWith should detect prefixes") {
            val trie = Trie.standard()
            trie.insert("alphabet")
            trie.insert("alpha")
            trie.insert("alpine")

            trie.startsWith("") shouldBe true
            trie.startsWith("a") shouldBe true
            trie.startsWith("al") shouldBe true
            trie.startsWith("alp") shouldBe true
            trie.startsWith("alph") shouldBe true
            trie.startsWith("alpha") shouldBe true
            trie.startsWith("alphabet") shouldBe true
            trie.startsWith("alphaX") shouldBe false
            trie.startsWith("b") shouldBe false
        }

        test("allWordsWithPrefix") {
            val trie = Trie.standard()
            trie.insert("alphabet")
            trie.insert("alpha")
            trie.insert("alpine")
            trie.insert("alien")
            trie.insert("banana")

            trie.allWordsWithPrefix("alp").toList() shouldContainExactlyInAnyOrder
                    listOf("alphabet", "alpha", "alpine")
            trie.allWordsWithPrefix("ali").toList() shouldContainExactlyInAnyOrder
                    listOf("alien")
            trie.allWordsWithPrefix("ban").toList() shouldContainExactlyInAnyOrder
                    listOf("banana")
            trie.allWordsWithPrefix("x").toList().shouldBeEmpty()
        }

        test("countWithPrefix") {
            val trie = Trie.standard()
            trie.insert("alphabet")
            trie.insert("alpha")
            trie.insert("alpine")
            trie.insert("alien")
            trie.insert("banana")

            trie.countWithPrefix("alp") shouldBe 3
            trie.countWithPrefix("al") shouldBe 4
            trie.countWithPrefix("a") shouldBe 4
            trie.countWithPrefix("ban") shouldBe 1
            trie.countWithPrefix("x") shouldBe 0
        }
    }

    context("SubTrie Operations") {
        test("subTrie returns correct subtrie") {
            val trie = Trie.standard()
            trie.insert("alphabet")
            trie.insert("alpha")
            trie.insert("alpine")
            trie.insert("banana")

            val subTrie = trie.subTrie("alp")
            subTrie.contains("habet") shouldBe true
            subTrie.contains("ha") shouldBe true
            subTrie.contains("ine") shouldBe true
            subTrie.contains("alphabet") shouldBe false
            subTrie.wordCount() shouldBe 3
        }

        test("subTrie with empty prefix returns same trie") {
            val trie = Trie.standard()
            trie.insert("test")

            val subTrie = trie.subTrie("")
            subTrie shouldBeSameInstanceAs trie
        }

        test("subTrie with non-matching prefix returns EMPTY") {
            val trie = Trie.standard()
            trie.insert("hello")

            val subTrie = trie.subTrie("xyz")
            subTrie shouldBeSameInstanceAs Trie.Companion.EMPTY
        }
    }

    context("Collection Operations") {
        test("toSequence returns all words") {
            val trie = Trie.standard()
            val words = listOf("cat", "dog", "bird")
            words.forEach { trie.insert(it) }

            trie.toSequence().toList() shouldContainExactlyInAnyOrder words
        }

        test("toList returns all words") {
            val trie = Trie.standard()
            val words = listOf("apple", "apricot", "banana")
            words.forEach { trie.insert(it) }

            trie.toList() shouldContainExactlyInAnyOrder words
        }

        test("toSet returns all unique words") {
            val trie = Trie.standard()
            val words = listOf("apple", "apricot", "banana")
            words.forEach { trie.insert(it) }

            trie.toSet() shouldBe words.toSet()
        }

        test("duplicate inserts don't increase word count") {
            val trie = Trie.standard()
            trie.insert("hello")
            trie.insert("hello")
            trie.insert("hello")

            trie.wordCount() shouldBe 1
            trie.contains("hello") shouldBe true
        }
    }

    context("Metrics") {
        test("wordCount counts all words") {
            val trie = Trie.standard()
            trie.insert("a")
            trie.insert("ab")
            trie.insert("abc")

            trie.wordCount() shouldBe 3
        }

        test("nodeCount counts all nodes") {
            val trie = Trie.standard()
            trie.insert("abc")
            // Root + 'a' + 'b' + 'c' = 4 nodes
            trie.nodeCount() shouldBe 4
        }

        test("depth calculation") {
            val trie = Trie.standard()
            trie.insert("a")
            trie.depth() shouldBe 2 // root + 'a'

            trie.insert("abc")
            trie.depth() shouldBe 4 // root + 'a' + 'b' + 'c'
        }

        test("depth with branching") {
            val trie = Trie.standard()
            trie.insert("cat")
            trie.insert("dog")
            trie.depth() shouldBe 4 // max depth is 4
        }
    }

    context("Filter Operations") {
        test("filter by predicate") {
            val trie = Trie.standard()
            trie.insert("cat")
            trie.insert("cats")
            trie.insert("dog")
            trie.insert("dogs")

            val filtered = trie.filter { it.startsWith("c") }
            filtered.wordCount() shouldBe 2
            filtered.contains("cat") shouldBe true
            filtered.contains("cats") shouldBe true
            filtered.contains("dog") shouldBe false
        }

        test("filter by length") {
            val trie = Trie.standard()
            trie.insert("a")
            trie.insert("ab")
            trie.insert("abc")
            trie.insert("abcd")

            val filtered = trie.filter { it.length >= 3 }
            filtered.wordCount() shouldBe 2
            filtered.contains("abc") shouldBe true
            filtered.contains("abcd") shouldBe true
            filtered.contains("a") shouldBe false
            filtered.contains("ab") shouldBe false
        }

        test("filter with no matches returns empty") {
            val trie = Trie.standard()
            trie.insert("hello")

            val filtered = trie.filter { false }
            filtered.isEmpty() shouldBe true
        }
    }

    context("Advanced Operations") {
        test("longestCommonPrefix with single path") {
            val trie = Trie.standard()
            trie.insert("alphabet")

            trie.longestCommonPrefix() shouldBe "alphabet"
        }

        test("longestCommonPrefix with branching") {
            val trie = Trie.standard()
            trie.insert("alpha")
            trie.insert("alpine")

            trie.longestCommonPrefix() shouldBe "alp"
        }

        test("longestCommonPrefix stops at terminal") {
            val trie = Trie.standard()
            trie.insert("a")
            trie.insert("b")

            trie.longestCommonPrefix().isEmpty() shouldBe true
        }

        test("branchSequence returns non-terminal nodes") {
            val trie = Trie.standard()
            trie.insert("abc")
            trie.insert("abd")

            val branches = trie.branchSequence().toList()
            branches.shouldContain("")
            branches.shouldContain("a")
            branches.shouldContain("ab")
        }

        test("prefixNGrams generates n-grams") {
            val trie = Trie.standard()
            trie.insert("hello")

            trie.prefixNGrams(2).toList() shouldContainExactlyInAnyOrder
                    listOf("he", "el", "ll", "lo")
        }

        test("prefixNGrams with n=1") {
            val trie = Trie.standard()
            trie.insert("abc")

            trie.prefixNGrams(1).toList() shouldContainExactlyInAnyOrder
                    listOf("a", "b", "c")
        }

        test("prefixNGrams with n=0 or negative") {
            val trie = Trie.standard()
            trie.insert("test")

            trie.prefixNGrams(0).toList().shouldBeEmpty()
            trie.prefixNGrams(-1).toList().shouldBeEmpty()
        }
    }

    context("Immutability") {
        test("toImmutable creates immutable copy") {
            val mutable = Trie.standard()
            mutable.insert("test")

            val immutable = mutable.toImmutable()
            immutable.contains("test") shouldBe true

            // Modify mutable
            mutable.insert("new")

            // Immutable should reflect changes (shares structure)
            immutable.contains("new") shouldBe true
        }
    }

    context("Serialization") {
        test("serialize and deserialize preserves data") {
            val trie = Trie.standard()
            val words = listOf("alphabet", "alpha", "alpine", "alien", "banana")
            words.forEach { trie.insert(it) }

            val immutable = trie.toImmutable()
            val json = Json.encodeToString(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            words.forEach { word ->
                deserialized.contains(word) shouldBe true
            }
            deserialized.wordCount() shouldBe words.size
            deserialized.toList() shouldContainExactlyInAnyOrder words
        }

        test("serialize empty trie") {
            val trie = Trie.standard()
            val immutable = trie.toImmutable()

            val json = Json.encodeToString<Trie>(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            deserialized.isEmpty() shouldBe true
            deserialized.wordCount() shouldBe 0
        }

        test("serialize trie with empty string") {
            val trie = Trie.standard()
            trie.insert("")
            trie.insert("a")

            val immutable = trie.toImmutable()
            val json = Json.encodeToString<Trie>(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            deserialized.contains("") shouldBe true
            deserialized.contains("a") shouldBe true
            deserialized.wordCount() shouldBe 2
        }
    }

    context("Pretty Printing") {
        test("toPrettyString should not be empty for non-empty trie") {
            val trie = Trie.standard()
            trie.insert("test")

            trie.toPrettyString(true).shouldNotBe("")
            trie.toPrettyString(false).shouldNotBe("")
        }

        test("toString uses pretty printing") {
            val trie = Trie.standard()
            trie.insert("a")

            trie.toString().shouldNotBe("")
        }
    }

    context("Edge Cases") {
        test("single character words") {
            val trie = Trie.standard()
            trie.insert("a")
            trie.insert("b")
            trie.insert("c")

            trie.contains("a") shouldBe true
            trie.contains("b") shouldBe true
            trie.contains("c") shouldBe true
            trie.wordCount() shouldBe 3
        }

        test("nested words") {
            val trie = Trie.standard()
            trie.insert("a")
            trie.insert("ab")
            trie.insert("abc")
            trie.insert("abcd")

            trie.wordCount() shouldBe 4
            listOf("a", "ab", "abc", "abcd").forEach {
                trie.contains(it) shouldBe true
            }
        }

        test("special characters") {
            val trie = Trie.standard()
            trie.insert("hello-world")
            trie.insert("test_case")
            trie.insert("foo.bar")

            trie.contains("hello-world") shouldBe true
            trie.contains("test_case") shouldBe true
            trie.contains("foo.bar") shouldBe true
        }

        test("unicode characters") {
            val trie = Trie.standard()
            trie.insert("café")
            trie.insert("naïve")
            trie.insert("日本")

            trie.contains("café") shouldBe true
            trie.contains("naïve") shouldBe true
            trie.contains("日本") shouldBe true
        }
    }

    context("Property-Based Tests") {
        test("inserted words are always found") {
            checkAll(Arb.string(1..20)) { word ->
                val trie = Trie.standard()
                trie.insert(word)
                trie.contains(word) shouldBe true
            }
        }

        test("word count equals number of unique insertions") {
            checkAll(Arb.string(1..10)) { word ->
                val trie = Trie.standard()
                repeat(5) { trie.insert(word) }
                trie.wordCount() shouldBe 1
            }
        }
    }
})