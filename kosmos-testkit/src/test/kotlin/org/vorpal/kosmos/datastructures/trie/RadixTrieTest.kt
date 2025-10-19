package org.vorpal.kosmos.datastructures.trie

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.*
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RadixTrieTest : FunSpec({

    context("Basic Operations") {
        test("empty trie should be empty") {
            val trie = Trie.radix()
            trie.isEmpty() shouldBe true
            trie.isNotEmpty() shouldBe false
            trie.wordCount() shouldBe 0
        }

        test("insert single word") {
            val trie = Trie.radix()
            trie.insert("hello")
            trie.contains("hello") shouldBe true
            trie.wordCount() shouldBe 1
            trie.isEmpty() shouldBe false
        }

        test("insert empty string") {
            val trie = Trie.radix()
            trie.insert("")
            trie.contains("") shouldBe true
            trie.wordCount() shouldBe 1
        }

        test("insert multiple words") {
            val trie = Trie.radix()
            val words = listOf("cat", "dog", "bird", "fish")
            words.forEach { trie.insert(it) }

            words.forEach { word ->
                trie.contains(word) shouldBe true
            }
            trie.wordCount() shouldBe words.size
        }

        test("contains operator") {
            val trie = Trie.radix()
            trie.insert("test")
            ("test" in trie) shouldBe true
            ("not" in trie) shouldBe false
        }

        test("non-existent words should not be found") {
            val trie = Trie.radix()
            trie.insert("hello")
            trie.contains("hell") shouldBe false
            trie.contains("helloworld") shouldBe false
            trie.contains("hi") shouldBe false
        }
    }

    context("Edge Compression") {
        test("compressed edges reduce node count") {
            val standard = Trie.standard()
            val radix = Trie.radix()

            val word = "abcdefghij"
            standard.insert(word)
            radix.insert(word)

            // Radix should have fewer nodes due to compression
            radix.nodeCount() shouldBeLessThan standard.nodeCount()
        }

        test("single word creates single edge") {
            val trie = Trie.radix()
            trie.insert("alphabet")

            // Root + 1 node for the entire word
            trie.nodeCount() shouldBe 2
        }

        test("words with common prefix share edge") {
            val trie = Trie.radix()
            trie.insert("alphabet")
            trie.insert("alpha")

            // Should share "alpha" edge and then split
            trie.wordCount() shouldBe 2
        }

        test("edge splitting with common prefix") {
            val trie = Trie.radix()
            trie.insert("alphabet")
            trie.insert("alpine")

            // Both words share "alp" prefix
            trie.contains("alphabet") shouldBe true
            trie.contains("alpine") shouldBe true
            trie.contains("alp") shouldBe false
        }

        test("inserting prefix of existing word") {
            val trie = Trie.radix()
            trie.insert("alphabet")
            trie.insert("alpha")

            trie.contains("alphabet") shouldBe true
            trie.contains("alpha") shouldBe true
            trie.wordCount() shouldBe 2
        }

        test("inserting word that extends existing") {
            val trie = Trie.radix()
            trie.insert("alpha")
            trie.insert("alphabet")

            trie.contains("alpha") shouldBe true
            trie.contains("alphabet") shouldBe true
            trie.wordCount() shouldBe 2
        }
    }

    context("Complex Prefix Scenarios") {
        test("multiple words with varying prefixes") {
            val trie = Trie.radix()
            trie.insert("alphabet")
            trie.insert("alpha")
            trie.insert("alpine")
            trie.insert("alien")
            trie.insert("airport")
            trie.insert("airline")
            trie.insert("air")

            listOf("alphabet", "alpha", "alpine", "alien", "airport", "airline", "air")
                .forEach { trie.contains(it) shouldBe true }

            trie.wordCount() shouldBe 7
        }

        test("branching at different depths") {
            val trie = Trie.radix()
            trie.insert("a")
            trie.insert("ab")
            trie.insert("abc")
            trie.insert("abd")
            trie.insert("ac")

            listOf("a", "ab", "abc", "abd", "ac")
                .forEach { trie.contains(it) shouldBe true }

            trie.wordCount() shouldBe 5
        }

        test("no common prefix words") {
            val trie = Trie.radix()
            trie.insert("apple")
            trie.insert("banana")
            trie.insert("cherry")

            trie.contains("apple") shouldBe true
            trie.contains("banana") shouldBe true
            trie.contains("cherry") shouldBe true
            trie.wordCount() shouldBe 3
        }
    }

    context("Prefix Operations") {
        test("startsWith should detect prefixes") {
            val trie = Trie.radix()
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

        test("startsWith with compressed edges") {
            val trie = Trie.radix()
            trie.insert("testing")

            trie.startsWith("t") shouldBe true
            trie.startsWith("te") shouldBe true
            trie.startsWith("tes") shouldBe true
            trie.startsWith("test") shouldBe true
            trie.startsWith("testing") shouldBe true
            trie.startsWith("testingX") shouldBe false
        }

        test("allWordsWithPrefix") {
            val trie = Trie.radix()
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
            val trie = Trie.radix()
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
        test("subTrie with exact edge match") {
            val trie = Trie.radix()
            trie.insert("alphabet")
            trie.insert("alpha")

            val subTrie = trie.subTrie("alpha")
            subTrie.contains("") shouldBe true
            subTrie.contains("bet") shouldBe true
            subTrie.wordCount() shouldBe 2
        }

        test("subTrie with partial edge match") {
            val trie = Trie.radix()
            trie.insert("alpine")

            val subTrie = trie.subTrie("alpi")
            subTrie.contains("ne") shouldBe true
            subTrie.wordCount() shouldBe 1
        }

        test("subTrie with prefix shorter than edge") {
            val trie = Trie.radix()
            trie.insert("testing")

            val subTrie = trie.subTrie("test")
            subTrie.contains("ing") shouldBe true
        }

        test("subTrie with empty prefix returns same trie") {
            val trie = Trie.radix()
            trie.insert("test")

            val subTrie = trie.subTrie("")
            subTrie shouldBeSameInstanceAs trie
        }

        test("subTrie with non-matching prefix returns EMPTY") {
            val trie = Trie.radix()
            trie.insert("hello")

            val subTrie = trie.subTrie("xyz")
            subTrie shouldBeSameInstanceAs Trie.Companion.EMPTY
        }

        test("subTrie complex example from main") {
            val trie = Trie.radix()
            trie.insert("alpine")

            val subTrie = trie.subTrie("alpi")
            subTrie.toList() shouldContainExactlyInAnyOrder listOf("ne")
        }
    }

    context("Collection Operations") {
        test("toSequence returns all words") {
            val trie = Trie.radix()
            val words = listOf("cat", "dog", "bird")
            words.forEach { trie.insert(it) }

            trie.toSequence().toList() shouldContainExactlyInAnyOrder words
        }

        test("toList returns all words") {
            val trie = Trie.radix()
            val words = listOf("apple", "apricot", "banana")
            words.forEach { trie.insert(it) }

            trie.toList() shouldContainExactlyInAnyOrder words
        }

        test("toSet returns all unique words") {
            val trie = Trie.radix()
            val words = listOf("apple", "apricot", "banana")
            words.forEach { trie.insert(it) }

            trie.toSet() shouldBe words.toSet()
        }

        test("duplicate inserts don't increase word count") {
            val trie = Trie.radix()
            trie.insert("hello")
            trie.insert("hello")
            trie.insert("hello")

            trie.wordCount() shouldBe 1
            trie.contains("hello") shouldBe true
        }
    }

    context("Metrics") {
        test("wordCount counts all words") {
            val trie = Trie.radix()
            trie.insert("a")
            trie.insert("ab")
            trie.insert("abc")

            trie.wordCount() shouldBe 3
        }

        test("nodeCount with compression") {
            val trie = Trie.radix()
            trie.insert("abc")
            // Root + single compressed node
            trie.nodeCount() shouldBe 2
        }

        test("nodeCount with branching") {
            val trie = Trie.radix()
            trie.insert("abc")
            trie.insert("abd")
            // Should have: root, "ab" node, "c" node, "d" node
            trie.nodeCount() shouldBe 4
        }

        test("depth calculation") {
            val trie = Trie.radix()
            trie.insert("a")
            trie.depth() shouldBe 2 // root + 'a'

            trie.insert("abc")
            // Depth depends on compression
            trie.depth() shouldBe 3 // root + "a" + "bc"
        }
    }

    context("Filter Operations") {
        test("filter by predicate") {
            val trie = Trie.radix()
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
            val trie = Trie.radix()
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
            val trie = Trie.radix()
            trie.insert("hello")

            val filtered = trie.filter { false }
            filtered.isEmpty() shouldBe true
        }
    }

    context("Advanced Operations") {
        test("longestCommonPrefix with single path") {
            val trie = Trie.radix()
            trie.insert("alphabet")

            trie.longestCommonPrefix() shouldBe "alphabet"
        }

        test("longestCommonPrefix with branching") {
            val trie = Trie.radix()
            trie.insert("alpha")
            trie.insert("alpine")

            // Radix should show "alp" as the common part
            trie.longestCommonPrefix() shouldBe "alp"
        }

        test("longestCommonPrefix stops at terminal") {
            val trie = Trie.radix()
            trie.insert("a")
            trie.insert("b")

            trie.longestCommonPrefix().isEmpty() shouldBe true
        }

        test("longestCommonPrefix with compressed edges") {
            val trie = Trie.radix()
            trie.insert("testing")

            // Single word path should return entire word
            trie.longestCommonPrefix() shouldBe "testing"
        }

        test("branchSequence returns non-terminal nodes") {
            val trie = Trie.radix()
            trie.insert("abc")
            trie.insert("abd")

            val branches = trie.branchSequence().toList()
            branches.shouldContain("")
            branches.shouldContain("ab")
        }

        test("prefixNGrams generates n-grams") {
            val trie = Trie.radix()
            trie.insert("hello")

            trie.prefixNGrams(2).toList() shouldContainExactlyInAnyOrder
                    listOf("he", "el", "ll", "lo")
        }

        test("prefixNGrams with n=1") {
            val trie = Trie.radix()
            trie.insert("abc")

            trie.prefixNGrams(1).toList() shouldContainExactlyInAnyOrder
                    listOf("a", "b", "c")
        }
    }

    context("Immutability") {
        test("toImmutable creates immutable wrapper") {
            val mutable = Trie.radix()
            mutable.insert("test")

            val immutable = mutable.toImmutable()
            immutable.contains("test") shouldBe true

            // Modify mutable
            mutable.insert("new")

            // Immutable shares structure, so sees changes
            immutable.contains("new") shouldBe true
        }
    }

    context("Serialization") {
        test("serialize and deserialize preserves data") {
            val trie = Trie.radix()
            val words = listOf("alphabet", "alpha", "alpine", "alien", "banana")
            words.forEach { trie.insert(it) }

            val immutable = trie.toImmutable()
            val json = Json.encodeToString<Trie>(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            words.forEach { word ->
                deserialized.contains(word) shouldBe true
            }
            deserialized.wordCount() shouldBe words.size
            deserialized.toList() shouldContainExactlyInAnyOrder words
        }

        test("serialize empty trie") {
            val trie = Trie.radix()
            val immutable = trie.toImmutable()

            val json = Json.encodeToString<Trie>(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            deserialized.isEmpty() shouldBe true
            deserialized.wordCount() shouldBe 0
        }

        test("serialize trie with empty string") {
            val trie = Trie.radix()
            trie.insert("")
            trie.insert("a")

            val immutable = trie.toImmutable()
            val json = Json.encodeToString<Trie>(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            deserialized.contains("") shouldBe true
            deserialized.contains("a") shouldBe true
            deserialized.wordCount() shouldBe 2
        }

        test("serialize complex trie structure") {
            val trie = Trie.radix()
            listOf("air", "airport", "airline", "airbill", "alpha", "alphabet").forEach {
                trie.insert(it)
            }

            val immutable = trie.toImmutable()
            val json = Json.encodeToString<Trie>(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            deserialized.wordCount() shouldBe 6
            listOf("air", "airport", "airline", "airbill", "alpha", "alphabet").forEach {
                deserialized.contains(it) shouldBe true
            }
        }
    }

    context("Pretty Printing") {
        test("toPrettyString should not be empty for non-empty trie") {
            val trie = Trie.radix()
            trie.insert("test")

            trie.toPrettyString(true).shouldNotBe("")
            trie.toPrettyString(false).shouldNotBe("")
        }

        test("toString uses pretty printing") {
            val trie = Trie.radix()
            trie.insert("a")

            trie.toString().shouldNotBe("")
        }
    }

    context("Edge Cases") {
        test("single character words") {
            val trie = Trie.radix()
            trie.insert("a")
            trie.insert("b")
            trie.insert("c")

            trie.contains("a") shouldBe true
            trie.contains("b") shouldBe true
            trie.contains("c") shouldBe true
            trie.wordCount() shouldBe 3
        }

        test("nested words with radix compression") {
            val trie = Trie.radix()
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
            val trie = Trie.radix()
            trie.insert("hello-world")
            trie.insert("test_case")
            trie.insert("foo.bar")

            trie.contains("hello-world") shouldBe true
            trie.contains("test_case") shouldBe true
            trie.contains("foo.bar") shouldBe true
        }

        test("unicode characters") {
            val trie = Trie.radix()
            trie.insert("café")
            trie.insert("naïve")
            trie.insert("日本")

            trie.contains("café") shouldBe true
            trie.contains("naïve") shouldBe true
            trie.contains("日本") shouldBe true
        }

        test("words that share no common prefix") {
            val trie = Trie.radix()
            trie.insert("xyz")
            trie.insert("abc")
            trie.insert("123")

            trie.contains("xyz") shouldBe true
            trie.contains("abc") shouldBe true
            trie.contains("123") shouldBe true
            trie.wordCount() shouldBe 3
        }
    }

    context("Radix-Specific Edge Cases") {
        test("inserting words that cause multiple splits") {
            val trie = Trie.radix()
            trie.insert("abcdefgh")
            trie.insert("abcd")
            trie.insert("abcxyz")

            trie.contains("abcdefgh") shouldBe true
            trie.contains("abcd") shouldBe true
            trie.contains("abcxyz") shouldBe true
            trie.wordCount() shouldBe 3
        }

        test("complete prefix overlap") {
            val trie = Trie.radix()
            trie.insert("test")
            trie.insert("t")
            trie.insert("te")
            trie.insert("tes")

            listOf("test", "t", "te", "tes").forEach {
                trie.contains(it) shouldBe true
            }
            trie.wordCount() shouldBe 4
        }

        test("reverse insertion order") {
            val trie1 = Trie.radix()
            trie1.insert("alphabet")
            trie1.insert("alpha")

            val trie2 = Trie.radix()
            trie2.insert("alpha")
            trie2.insert("alphabet")

            trie1.toList() shouldContainExactlyInAnyOrder trie2.toList()
            trie1.wordCount() shouldBe trie2.wordCount()
        }
    }

    context("Property-Based Tests") {
        test("inserted words are always found") {
            checkAll(Arb.string(1..20)) { word ->
                val trie = Trie.radix()
                trie.insert(word)
                trie.contains(word) shouldBe true
            }
        }

        test("word count equals number of unique insertions") {
            checkAll(Arb.string(1..10)) { word ->
                val trie = Trie.radix()
                repeat(5) { trie.insert(word) }
                trie.wordCount() shouldBe 1
            }
        }

        test("radix compression maintains correctness") {
            checkAll(Arb.string(1..15)) { word1 ->
                checkAll(Arb.string(1..15)) { word2 ->
                    val trie = Trie.radix()
                    trie.insert(word1)
                    trie.insert(word2)

                    trie.contains(word1) shouldBe true
                    trie.contains(word2) shouldBe true
                }
            }
        }
    }

    context("Comparison with StandardTrie") {
        test("both implementations produce same results") {
            val words = listOf("alphabet", "alpha", "alpine", "alien", "banana", "bar", "barn")

            val radix = Trie.radix()
            val standard = Trie.standard()

            words.forEach {
                radix.insert(it)
                standard.insert(it)
            }

            radix.toList() shouldContainExactlyInAnyOrder standard.toList()
            radix.wordCount() shouldBe standard.wordCount()

            words.forEach { word ->
                radix.contains(word) shouldBe standard.contains(word)
            }
        }

        test("radix has fewer nodes than standard for same words") {
            val words = listOf("testing", "tester", "tested")

            val radix = Trie.radix()
            val standard = Trie.standard()

            words.forEach {
                radix.insert(it)
                standard.insert(it)
            }

            radix.nodeCount() shouldBeLessThan standard.nodeCount()
        }
    }
})