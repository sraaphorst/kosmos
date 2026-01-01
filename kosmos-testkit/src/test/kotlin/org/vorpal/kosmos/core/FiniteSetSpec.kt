package org.vorpal.kosmos.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.minus
import org.vorpal.kosmos.core.finiteset.plus
import org.vorpal.kosmos.core.finiteset.toOrderedFiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet

class FiniteSetSpec : StringSpec({

    // ===== Generators =====
    val arbInt = Arb.int(-100..100)
    val arbString = Arb.string(1..5)
    Arb.char('a'..'z')

    val arbSmallOrderedSet = generateArbOrderedFiniteSet(arbInt, 0, 10)
    val arbSmallUnorderedSet = generateArbUnorderedFiniteSet(arbInt, 0, 10)
    val arbSmallSet = generateArbFiniteSet(arbInt, 0, 10)

    val arbNonEmptyOrderedSet = generateArbNonEmptyOrderedFiniteSet(arbInt, 10)
    generateArbNonEmptyUnorderedFiniteSet(arbInt, 10)

    // ===== Basic Invariants =====

    "ordered set backing and order are consistent" {
        checkAll(arbSmallOrderedSet) { set ->
            set.backing shouldBe set.order.toSet()
            set.size shouldBe set.order.size
            set.size shouldBe set.backing.size
        }
    }

    "unordered set backing and order are consistent" {
        checkAll(arbSmallUnorderedSet) { set ->
            set.backing shouldBe set.order.toSet()
            set.size shouldBe set.order.size
            set.size shouldBe set.backing.size
        }
    }

    "ordered set preserves insertion order for distinct elements" {
        checkAll(Arb.list(arbInt, 0..10)) { elements ->
            val set = FiniteSet.ordered(elements)
            val expectedOrder = elements.distinct()
            set.order shouldBe expectedOrder
        }
    }

    "isEmpty and isNotEmpty are consistent" {
        checkAll(arbSmallSet) { set ->
            set.isEmpty shouldBe (set.size == 0)
            set.isNotEmpty shouldBe (set.size > 0)
            set.isEmpty shouldBe !set.isNotEmpty
        }
    }

    "contains operation works correctly" {
        checkAll(arbSmallOrderedSet, arbInt) { set, element ->
            set.contains(element) shouldBe (element in set.backing)
            (element in set) shouldBe set.contains(element)
        }
    }

    // ===== Set Algebra Laws =====

    "unordered union is commutative" {
        checkAll(TestingCombinations.arbSetPair(arbInt)) { (setA, setB) ->
            (setA union setB).toUnordered() shouldBe (setB union setA).toUnordered()
        }
    }

    "union is associative" {
        checkAll(TestingCombinations.arbSetTriple(arbInt)) { (setA, setB, setC) ->
            ((setA union setB) union setC) shouldBe (setA union (setB union setC))
        }
    }

    "unordered intersection is commutative" {
        checkAll(TestingCombinations.arbSetPair(arbInt)) { (setA, setB) ->
            (setA intersect setB).toUnordered() shouldBe (setB intersect setA).toUnordered()
        }
    }

    "intersection is associative" {
        checkAll(TestingCombinations.arbSetTriple(arbInt)) { (setA, setB, setC) ->
            ((setA intersect setB) intersect setC) shouldBe (setA intersect (setB intersect setC))
        }
    }

    "union distributes over intersection" {
        checkAll(TestingCombinations.arbSetTriple(arbInt, 5)) { (setA, setB, setC) ->
            (setA union (setB intersect setC)) shouldBe ((setA union setB) intersect (setA union setC))
        }
    }

    "unordered intersection distributes over union" {
        checkAll(TestingCombinations.arbSetTriple(arbInt, 5)) { (setA, setB, setC) ->
            (setA intersect (setB union setC)).toUnordered() shouldBe ((setA intersect setB) union (setA intersect setC)).toUnordered()
        }
    }

    "difference satisfies expected properties" {
        checkAll(TestingCombinations.arbSetPair(arbInt)) { (setA, setB) ->
            val difference = setA - setB
            difference.all { it in setA && it !in setB }
            (difference intersect setB).isEmpty
        }
    }

    "unordered symmetric difference is commutative" {
        checkAll(TestingCombinations.arbSetPair(arbInt)) { (setA, setB) ->
            (setA symmetricDifference setB).toUnordered() shouldBe (setB symmetricDifference setA).toUnordered()
        }
    }

    "symmetric difference with self is empty" {
        checkAll(arbSmallOrderedSet) { set ->
            (set symmetricDifference set).isEmpty shouldBe true
        }
    }

    // ===== Subset Relationships =====

    "every set is subset of itself" {
        checkAll(arbSmallSet) { set ->
            (set isSubsetOf set) shouldBe true
            (set isSupersetOf set) shouldBe true
        }
    }

    "no set is proper subset of itself" {
        checkAll(arbSmallSet) { set ->
            (set isProperSubsetOf set) shouldBe false
            (set isProperSupersetOf set) shouldBe false
        }
    }

    "subset relationships are transitive" {
        checkAll(TestingCombinations.arbSetTriple(arbInt, 8)) { (setA, setB, setC) ->
            if ((setA isSubsetOf setB) && (setB isSubsetOf setC)) {
                (setA isSubsetOf setC) shouldBe true
            }
        }
    }

    "generated subset relationships work correctly" {
        checkAll(TestingCombinations.arbSetAndSubset(arbInt)) { (parentSet, subset) ->
            (subset isSubsetOf parentSet) shouldBe true
            (parentSet isSupersetOf subset) shouldBe true
            if (subset.size < parentSet.size) {
                (subset isProperSubsetOf parentSet) shouldBe true
                (parentSet isProperSupersetOf subset) shouldBe true
            }
        }
    }

    "disjoint sets have empty intersection" {
        checkAll(TestingCombinations.arbSetPair(arbInt)) { (setA, setB) ->
            if (setA isDisjointFrom setB) {
                (setA intersect setB).isEmpty shouldBe true
                (setB isDisjointFrom setA) shouldBe true
            }
        }
    }

    // ===== Functional Operations =====

    "map preserves size relationships" {
        checkAll(arbSmallOrderedSet) { set ->
            val mapped = set.map { it * 2 }
            mapped.size <= set.size // Can be smaller due to collisions
            set.all { it * 2 in mapped }
        }
    }

    "map preserves order for ordered sets" {
        checkAll(arbSmallOrderedSet) { set ->
            val mapped = set.map { it.toString() }
            when (mapped) {
                is FiniteSet.Ordered -> {
                    val expectedOrder = set.order.map { it.toString() }.distinct()
                    mapped.order shouldBe expectedOrder
                }

                else -> {} // Should not happen for ordered input
            }
        }
    }

    "filter preserves subset relationship" {
        checkAll(arbSmallSet) { set ->
            val predicate = { x: Int -> x % 2 == 0 }
            val filtered = set.filter(predicate)

            (filtered isSubsetOf set) shouldBe true
            (filtered.all(predicate)) shouldBe true
            set.filter { !predicate(it) }.none(predicate) shouldBe true
        }
    }

    "filterNot is complement of filter" {
        checkAll(arbSmallSet) { set ->
            val predicate = { x: Int -> x > 0 }
            val filtered = set.filter(predicate)
            val filteredNot = set.filterNot(predicate)

            (filtered intersect filteredNot).isEmpty shouldBe true
            (filtered union filteredNot).toUnordered() shouldBe set.toUnordered()
        }
    }

    "partition splits set correctly" {
        checkAll(arbSmallSet) { set ->
            val predicate = { x: Int -> x % 3 == 0 }
            val (matching, notMatching) = set.partition(predicate)

            matching.all(predicate) shouldBe true
            notMatching.all { !predicate(it) } shouldBe true
            (matching union notMatching).toUnordered() shouldBe set.toUnordered()
            (matching intersect notMatching).isEmpty shouldBe true
        }
    }

    "flatMap flattens correctly" {
        checkAll(arbSmallOrderedSet) { set ->
            val result = set.flatMap { x -> FiniteSet.ordered(x, x + 1, x + 2) }

            // Every original element and its successors should be in result
            set.all { it in result } shouldBe true
            set.all { it + 1 in result } shouldBe true
            set.all { it + 2 in result } shouldBe true
        }
    }

    // ===== Collection Operations =====

    "any/all/none are consistent" {
        checkAll(arbSmallSet.filter { it.isNotEmpty }) { set ->
            val predicate = { x: Int -> x > 50 }

            if (set.all(predicate)) {
                set.any(predicate) shouldBe true
                set.none(predicate) shouldBe false
            }

            if (set.none(predicate)) {
                set.any(predicate) shouldBe false
                set.all(predicate) shouldBe false
            }

            set.any(predicate) shouldBe !set.none(predicate)
        }
    }

    "count matches filter size" {
        checkAll(arbSmallSet) { set ->
            val predicate = { x: Int -> x % 4 == 0 }
            set.count(predicate) shouldBe set.filter(predicate).size
        }
    }

    "fold accumulates correctly" {
        checkAll(arbNonEmptyOrderedSet) { set ->
            val sum = set.fold(0) { acc, x -> acc + x }
            sum shouldBe set.order.sum()
        }
    }

    "reduce works on non-empty sets" {
        checkAll(arbNonEmptyOrderedSet) { set ->
            val sum = set.reduce { acc, x -> acc + x }
            sum shouldBe set.order.sum()
        }
    }

    "groupBy partitions correctly" {
        checkAll(arbSmallOrderedSet) { set ->
            val grouped = set.groupBy { it % 3 }

            // All original elements should appear exactly once
            val allElements = grouped.values.flatMap { it.order }.toSet()
            allElements shouldBe set.backing

            // Each group should satisfy the grouping condition
            grouped.forEach { (key, group) ->
                group.all { it % 3 == key } shouldBe true
            }
        }
    }

    // ===== Combinatorial Operations =====

    "cartesian product has correct size" {
        checkAll(arbSmallOrderedSet, arbSmallOrderedSet) { setA, setB ->
            val product = (setA cartesianProduct setB)
            product.size shouldBe (setA.size * setB.size)
        }
    }

    "cartesian product contains all pairs" {
        checkAll(
            generateArbOrderedFiniteSet(arbInt, 1, 3),
            generateArbOrderedFiniteSet(arbString, 1, 3)
        ) { setA, setB ->
            val product = (setA cartesianProduct setB)

            setA.all { a ->
                setB.all { b ->
                    (a to b) in product
                }
            } shouldBe true
        }
    }

    "cartesian power properties" {
        checkAll(generateArbOrderedFiniteSet(arbInt, 1, 3)) { set ->
            val power0 = set.cartesianPower(0)
            val power1 = set.cartesianPower(1)
            val power2 = set.cartesianPower(2)

            power0.size shouldBe 1
            power0.contains(emptyList()) shouldBe true

            power1.size shouldBe set.size
            power1.all { it.size == 1 } shouldBe true

            power2.size shouldBe (set.size * set.size)
            power2.all { it.size == 2 } shouldBe true
        }
    }

    // ===== Ordered-Specific Operations =====

    "ordered set find returns first match" {
        val set = FiniteSet.ordered(1, 2, 3, 4, 5, 2, 6) // Will deduplicate to [1,2,3,4,5,6]
        set.find { it > 3 } shouldBe 4 // First element > 3 in order
        set.find { it > 10 } shouldBe null
    }

    "indexOf and lastIndexOf work correctly" {
        checkAll(arbNonEmptyOrderedSet) { set ->
            val firstElement = set.order.first()
            val lastElement = set.order.last()

            set.indexOf(firstElement) shouldBe 0
            set.indexOf(lastElement) shouldBe (set.size - 1)
            set.lastIndexOf(firstElement) shouldBe set.order.lastIndexOf(firstElement)
        }
    }

    "take and drop are complements" {
        checkAll(arbSmallOrderedSet, Arb.int(0..15)) { set, n ->
            val taken = set.take(n)
            val dropped = set.drop(n)

            taken.size shouldBe minOf(n, set.size)
            if (n < set.size) {
                (taken union dropped) shouldBe set
                taken.order shouldBe set.order.take(n)
                dropped.order shouldBe set.order.drop(n)
            }
        }
    }

    "takeWhile and dropWhile are complements" {
        checkAll(arbSmallOrderedSet) { set ->
            val predicate = { x: Int -> x < 50 }
            val taken = set.takeWhile(predicate)
            val dropped = set.dropWhile(predicate)

            // Taken elements should all satisfy predicate
            taken.all(predicate) shouldBe true

            // Together they should reconstruct the original
            FiniteSet.ordered(taken.order + dropped.order) shouldBe set
        }
    }

    "reversed twice gives original" {
        checkAll(arbSmallOrderedSet) { set ->
            set.reversed().reversed() shouldBe set
        }
    }

    "zip creates pairs correctly" {
        checkAll(
            generateArbOrderedFiniteSet(arbInt, 1, 5),
            generateArbOrderedFiniteSet(arbString, 1, 5)
        ) { setA, setB ->
            val zipped = setA.zip(setB)
            val expectedSize = minOf(setA.size, setB.size)

            zipped.size shouldBe expectedSize
            zipped.order.forEachIndexed { i, (a, b) ->
                a shouldBe setA.order[i]
                b shouldBe setB.order[i]
            }
        }
    }

    "zipWithIndex creates indexed pairs" {
        checkAll(arbSmallOrderedSet) { set ->
            val indexed = set.zipWithIndex()
            indexed.size shouldBe set.size
            indexed.order.forEachIndexed { i, (element, index) ->
                element shouldBe set.order[i]
                index shouldBe i
            }
        }
    }

    "windowed creates sliding windows" {
        val set = FiniteSet.ordered(1, 2, 3, 4, 5)
        val windowed = set.windowed(3)

        windowed.size shouldBe 3
        windowed[0] shouldBe FiniteSet.ordered(1, 2, 3)
        windowed[1] shouldBe FiniteSet.ordered(2, 3, 4)
        windowed[2] shouldBe FiniteSet.ordered(3, 4, 5)
    }

    "chunked splits into fixed-size groups" {
        val set = FiniteSet.ordered(1, 2, 3, 4, 5, 6, 7)
        val chunked = set.chunked(3)

        chunked.size shouldBe 3
        chunked[0] shouldBe FiniteSet.ordered(1, 2, 3)
        chunked[1] shouldBe FiniteSet.ordered(4, 5, 6)
        chunked[2] shouldBe FiniteSet.ordered(7)
    }

    // ===== Type Conversions =====

    "toOrdered and toUnordered preserve elements" {
        checkAll(arbSmallSet) { set ->
            val asOrdered = set.toOrdered()
            val asUnordered = set.toUnordered()

            asOrdered.backing shouldBe set.backing
            asUnordered.backing shouldBe set.backing
            asOrdered.shouldBeInstanceOf<FiniteSet.Ordered<Int>>()
            asUnordered.shouldBeInstanceOf<FiniteSet.Unordered<Int>>()
        }
    }

    // ===== Equality Semantics =====

    "ordered sets have list-like equality" {
        val set1 = FiniteSet.ordered(1, 2, 3)
        val set2 = FiniteSet.ordered(1, 2, 3)
        val set3 = FiniteSet.ordered(3, 2, 1)

        set1 shouldBe set2
        set1 shouldNotBe set3 // Different order
        set1.hashCode() shouldBe set2.hashCode()
    }

    "unordered sets have set-like equality" {
        val set1 = FiniteSet.unordered(1, 2, 3)
        val set2 = FiniteSet.unordered(3, 2, 1)
        val set3 = FiniteSet.unordered(1, 2, 4)

        set1 shouldBe set2 // Same elements, different order
        set1 shouldNotBe set3 // Different elements
        set1.hashCode() shouldBe set2.hashCode()
    }

    // ===== Builder Functions =====

    "buildOrdered creates correct set" {
        val set = FiniteSet.buildOrdered {
            add(1)
            add(2)
            add(1) // Duplicate
            addAll(listOf(3, 4))
        }
        set shouldBe FiniteSet.ordered(1, 2, 3, 4)
    }

    "buildUnordered creates correct set" {
        val set = FiniteSet.buildUnordered {
            add(1)
            add(2)
            add(1) // Duplicate
            addAll(listOf(3, 4))
        }
        set.backing shouldBe setOf(1, 2, 3, 4)
    }

    // ===== Range and Sequence Builders =====

    "rangeOrdered creates sequential sets" {
        FiniteSet.rangeOrdered(1, 5) shouldBe FiniteSet.ordered(1, 2, 3, 4, 5)
        FiniteSet.rangeOrdered(3, 3) shouldBe FiniteSet.ordered(3)
    }

    "sequence conversion preserves order" {
        val sequence = sequenceOf(1, 2, 1, 3, 2, 4)
        val orderedSet = FiniteSet.fromSequenceOrdered(sequence)
        val unorderedSet = FiniteSet.fromSequenceUnordered(sequence)

        orderedSet shouldBe FiniteSet.ordered(1, 2, 3, 4)
        unorderedSet.backing shouldBe setOf(1, 2, 3, 4)
    }

    // ===== Extension Function Tests =====

    "extension functions work correctly" {
        val list = listOf(1, 2, 1, 3)
        val array = arrayOf(1, 2, 1, 3)
        val sequence = sequenceOf(1, 2, 1, 3)

        list.toOrderedFiniteSet() shouldBe FiniteSet.ordered(1, 2, 3)
        list.toUnorderedFiniteSet().backing shouldBe setOf(1, 2, 3)

        array.toOrderedFiniteSet() shouldBe FiniteSet.ordered(1, 2, 3)
        array.toUnorderedFiniteSet().backing shouldBe setOf(1, 2, 3)

        sequence.toOrderedFiniteSet() shouldBe FiniteSet.ordered(1, 2, 3)
        sequence.toUnorderedFiniteSet().backing shouldBe setOf(1, 2, 3)
    }

    // ===== Operator Overloads =====

    "plus and minus operators work with single elements" {
        checkAll(arbSmallOrderedSet, arbInt) { set, element ->
            val withElement = set + element
            val withoutElement = set - element

            withElement.contains(element) shouldBe true
            !withoutElement.contains(element) shouldBe true
        }
    }
})