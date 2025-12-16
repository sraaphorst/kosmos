package org.vorpal.kosmos.datastructures.trie

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.*
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeLessThan
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.vorpal.kosmos.core.math.toReal

/**
 * Integration tests that verify consistent behavior across different Trie implementations
 * and test cross-cutting concerns.
 */
class TrieIntegrationTest : FunSpec({

    context("Cross-Implementation Consistency") {
        test("standard and radix produce identical word lists") {
            val words = listOf(
                "alphabet", "alpha", "alpine", "alien",
                "airport", "airline", "air", "airbill",
                "banana", "ban", "barn", "bar"
            )

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            standard.toList() shouldContainExactlyInAnyOrder radix.toList()
        }

        test("standard and radix have same word count") {
            val words = listOf("cat", "cats", "dog", "dogs", "bird")

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            standard.wordCount() shouldBe radix.wordCount()
        }

        test("standard and radix handle contains identically") {
            val words = listOf("test", "testing", "tested", "tester")

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            val testWords = words + listOf("tes", "te", "t", "testings", "xyz")
            testWords.forEach { word ->
                standard.contains(word) shouldBe radix.contains(word)
            }
        }

        test("standard and radix handle startsWith identically") {
            val words = listOf("alphabet", "alpha", "alpine")

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            val prefixes = listOf("", "a", "al", "alp", "alph", "alpha", "xyz")
            prefixes.forEach { prefix ->
                standard.startsWith(prefix) shouldBe radix.startsWith(prefix)
            }
        }

        test("standard and radix produce same subTrie results") {
            val words = listOf("alphabet", "alpha", "alpine", "alien", "banana")

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            val prefixes = listOf("", "a", "al", "alp", "ban", "xyz")
            prefixes.forEach { prefix ->
                val standardSub = standard.subTrie(prefix)
                val radixSub = radix.subTrie(prefix)

                standardSub.toList() shouldContainExactlyInAnyOrder radixSub.toList()
                standardSub.wordCount() shouldBe radixSub.wordCount()
            }
        }
    }

    context("Efficiency Comparisons") {
        test("radix has fewer nodes than standard for long words") {
            val words = listOf(
                "abcdefghijklmnopqrstuvwxyz",
                "abcdefghijklmnopqrstuvwxy",
                "abcdefghijklmnopqrstuvwx"
            )

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            radix.nodeCount() shouldBeLessThan standard.nodeCount()
        }

        test("radix compression is most effective with long shared prefixes") {
            val words = listOf(
                "internationalization",
                "international",
                "internationally"
            )

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            // Radix should have significantly fewer nodes
            val compressionRatio = radix.nodeCount().toReal() / standard.nodeCount()
            compressionRatio shouldBeLessThan 0.5
        }

        test("radix and standard have similar node counts with no shared prefixes") {
            val words = listOf("a", "b", "c", "d", "e")

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            // Both should have similar structure with no shared prefixes
            radix.nodeCount() shouldBe standard.nodeCount()
        }
    }

    context("Serialization Round-Trips") {
        test("standard trie serialization preserves all operations") {
            val original = Trie.standard()
            val words = listOf("alpha", "alphabet", "beta", "gamma")
            words.forEach { original.insert(it) }

            val immutable = original.toImmutable()
            val json = Json.encodeToString<Trie>(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            // Test all operations
            deserialized.wordCount() shouldBe original.wordCount()
            deserialized.toList() shouldContainExactlyInAnyOrder original.toList()
            words.forEach { word ->
                deserialized.contains(word) shouldBe original.contains(word)
            }
            deserialized.startsWith("alp") shouldBe original.startsWith("alp")
            deserialized.countWithPrefix("alp") shouldBe original.countWithPrefix("alp")
        }

        test("radix trie serialization preserves all operations") {
            val original = Trie.radix()
            val words = listOf("alpha", "alphabet", "beta", "gamma")
            words.forEach { original.insert(it) }

            val immutable = original.toImmutable()
            val json = Json.encodeToString<Trie>(immutable)
            val deserialized: Trie = Json.decodeFromString(json)

            // Test all operations
            deserialized.wordCount() shouldBe original.wordCount()
            deserialized.toList() shouldContainExactlyInAnyOrder original.toList()
            words.forEach { word ->
                deserialized.contains(word) shouldBe original.contains(word)
            }
            deserialized.startsWith("alp") shouldBe original.startsWith("alp")
            deserialized.countWithPrefix("alp") shouldBe original.countWithPrefix("alp")
        }

        test("deserialized tries support further operations") {
            val original = Trie.standard()
            original.insert("test")

            val json = Json.encodeToString<Trie>(original.toImmutable())
            val deserialized: Trie = Json.decodeFromString(json)

            // Operations on deserialized trie
            val subTrie = deserialized.subTrie("te")
            subTrie.contains("st") shouldBe true

            val filtered = deserialized.filter { it.length == 4 }
            filtered.wordCount() shouldBe 1
        }
    }

    context("Complex Scenarios") {
        test("large dictionary of common English words") {
            val words = listOf(
                "the", "be", "to", "of", "and", "a", "in", "that", "have", "I",
                "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
                "this", "but", "his", "by", "from", "they", "we", "say", "her", "she"
            )

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            standard.wordCount() shouldBe words.size
            radix.wordCount() shouldBe words.size

            standard.toList() shouldContainExactlyInAnyOrder radix.toList()

            // Test various prefixes
            standard.countWithPrefix("th") shouldBe radix.countWithPrefix("th")
            standard.countWithPrefix("he") shouldBe radix.countWithPrefix("he")
        }

        test("words with varying lengths from 1 to 20 characters") {
            val words = (1..20).map { "a".repeat(it) }

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            words.forEach { word ->
                standard.contains(word) shouldBe true
                radix.contains(word) shouldBe true
            }

            standard.wordCount() shouldBe words.size
            radix.wordCount() shouldBe words.size
        }

        test("filter creates new trie that can be queried") {
            val original = Trie.radix()
            listOf("cat", "cats", "dog", "dogs", "bird", "birds").forEach {
                original.insert(it)
            }

            val filtered = original.filter { it.endsWith("s") }

            filtered.wordCount() shouldBe 3
            filtered.contains("cats") shouldBe true
            filtered.contains("dogs") shouldBe true
            filtered.contains("birds") shouldBe true
            filtered.contains("cat") shouldBe false

            // Filtered trie supports further operations
            filtered.countWithPrefix("d") shouldBe 1
            filtered.startsWith("c") shouldBe true
        }

        test("chaining operations") {
            val trie = Trie.standard()
            listOf("alphabet", "alpha", "alpine", "alien", "beta", "gamma").forEach {
                trie.insert(it)
            }

            val result = trie
                .subTrie("al")
                .filter { it.length > 2 }

            result.toList() shouldContainExactlyInAnyOrder listOf("phabet", "pha", "pine", "ien")
        }
    }

    context("Edge Cases Across Implementations") {
        test("both handle empty strings correctly") {
            val standard = Trie.standard()
            val radix = Trie.radix()

            standard.insert("")
            radix.insert("")

            standard.contains("") shouldBe true
            radix.contains("") shouldBe true

            standard.insert("a")
            radix.insert("a")

            standard.wordCount() shouldBe 2
            radix.wordCount() shouldBe 2
        }

        test("both handle single characters") {
            val chars = ('a'..'z').map { it.toString() }

            val standard = Trie.standard()
            val radix = Trie.radix()

            chars.forEach { char ->
                standard.insert(char)
                radix.insert(char)
            }

            standard.wordCount() shouldBe 26
            radix.wordCount() shouldBe 26

            chars.forEach { char ->
                standard.contains(char) shouldBe true
                radix.contains(char) shouldBe true
            }
        }

        test("both handle unicode correctly") {
            val words = listOf("café", "naïve", "日本", "🎉🎊", "Москва")

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            words.forEach { word ->
                standard.contains(word) shouldBe true
                radix.contains(word) shouldBe true
            }
        }

        test("both handle very long words") {
            val longWord = "a".repeat(1000)

            val standard = Trie.standard()
            val radix = Trie.radix()

            standard.insert(longWord)
            radix.insert(longWord)

            standard.contains(longWord) shouldBe true
            radix.contains(longWord) shouldBe true

            // Radix should have far fewer nodes
            radix.nodeCount() shouldBeLessThan standard.nodeCount()
        }
    }

    context("Merge Operations") {
        test("merging two tries combines their words") {
            val trie1 = Trie.standard()
            val trie2 = Trie.standard()

            trie1.insert("alpha")
            trie1.insert("beta")

            trie2.insert("gamma")
            trie2.insert("delta")

            trie1.merge(trie2)

            trie1.wordCount() shouldBe 4
            trie1.contains("alpha") shouldBe true
            trie1.contains("beta") shouldBe true
            trie1.contains("gamma") shouldBe true
            trie1.contains("delta") shouldBe true
        }

        test("merging handles duplicate words") {
            val trie1 = Trie.radix()
            val trie2 = Trie.radix()

            trie1.insert("alpha")
            trie1.insert("beta")

            trie2.insert("alpha")
            trie2.insert("gamma")

            trie1.merge(trie2)

            trie1.wordCount() shouldBe 3
            trie1.contains("alpha") shouldBe true
            trie1.contains("beta") shouldBe true
            trie1.contains("gamma") shouldBe true
        }

        test("merging standard into radix") {
            val standard = Trie.standard()
            val radix = Trie.radix()

            standard.insert("test")
            standard.insert("testing")

            radix.insert("other")
            radix.merge(standard)

            radix.wordCount() shouldBe 3
            radix.contains("test") shouldBe true
            radix.contains("testing") shouldBe true
            radix.contains("other") shouldBe true
        }
    }

    context("Performance Characteristics") {
        test("both handle large prefix queries efficiently") {
            val words = (1..100).map { "prefix$it" }

            val standard = Trie.standard()
            val radix = Trie.radix()

            words.forEach { word ->
                standard.insert(word)
                radix.insert(word)
            }

            standard.countWithPrefix("prefix") shouldBe 100
            radix.countWithPrefix("prefix") shouldBe 100

            standard.allWordsWithPrefix("prefix").toList().size shouldBe 100
            radix.allWordsWithPrefix("prefix").toList().size shouldBe 100
        }
    }

    context("Depth and Structure") {
        test("depth increases with word length") {
            val standard = Trie.standard()
            standard.insert("a")
            val depth1 = standard.depth()

            standard.insert("ab")
            val depth2 = standard.depth()

            standard.insert("abc")
            val depth3 = standard.depth()

            depth2 shouldBe depth1 + 1
            depth3 shouldBe depth2 + 1
        }

        test("radix depth reflects compression") {
            val radix = Trie.radix()
            radix.insert("abcdefghij")

            // Single word should compress to minimal depth
            radix.depth() shouldBe 2 // root + compressed node
        }
    }
})