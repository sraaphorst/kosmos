package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.combinatorics.FanoPlane

/**
 * The finite combinatorics of the octonion multiplication table, checked exhaustively and exactly.
 *
 * Everything here is derived from the products of the seven imaginary basis units `e₁, …, e₇` of
 * the rational octonions, so no floating point is involved. The multiplication table is recorded as
 * ```text
 * e_i · e_j = sign(i, j) · e_{third(i, j)}      (i ≠ j)
 * ```
 * and the facts verified are:
 * - **168 nonassociative triples.** Of the `7·6·5 = 210` ordered triples of distinct units, the `42`
 *   collinear ones (the `7` lines of the Fano plane in their `6` orders) associate, and the other
 *   `168` anti-associate. Unordered: `35 = 7 + 28`.
 * - **84 associator pairs.** The associator is alternating, so reversing a nonassociative triple
 *   negates its associator, pairing the 168 into 84.
 * - **Order 21.** Exactly `21` permutations of the units preserve the table; they form the
 *   Frobenius group `C₇ ⋊ C₃`. Ignoring orientation there are `168 = |PSL(2, 7)|` collineations,
 *   and allowing sign changes there are `1344 = 2³ · 168` signed automorphisms.
 * - **480 tables.** The `2⁷ · 7! = 645120` signed permutations of the units produce exactly
 *   `645120 / 1344 = 480` distinct multiplication tables: `30` Fano planes on seven labelled points,
 *   each with `16` octonionic orientations.
 */
class OctonionCombinatoricsSpec : StringSpec({
    val ring = RationalOctonionAlgebras.RationalOctonionDivisionAlgebra
    val e = ring.basis
    val units = 1..7

    fun neg(x: RationalOctonion): RationalOctonion = ring.add.inverse(x)
    fun minus(x: RationalOctonion, y: RationalOctonion): RationalOctonion = ring.add(x, neg(y))
    fun associator(x: RationalOctonion, y: RationalOctonion, z: RationalOctonion): RationalOctonion =
        minus(ring.mul(ring.mul(x, y), z), ring.mul(x, ring.mul(y, z)))

    // ── The multiplication table, read off the actual products ───────────────

    /** `third[i][j] = k` and `sign[i][j] = s` where `e_i · e_j = s · e_k`, for `i ≠ j` in `1..7`. */
    val third = Array(8) { IntArray(8) }
    val sign = Array(8) { IntArray(8) }
    units.forEach { i ->
        units.filter { j -> j != i }.forEach { j ->
            val product = ring.mul(e[i], e[j])
            val k = units.single { k -> product == e[k] || product == neg(e[k]) }
            third[i][j] = k
            sign[i][j] = if (product == e[k]) 1 else -1
        }
    }

    val orderedTriples: List<Triple<Int, Int, Int>> =
        units.flatMap { i -> units.flatMap { j -> units.map { k -> Triple(i, j, k) } } }
    val distinctTriples = orderedTriples.filter { (i, j, k) -> setOf(i, j, k).size == 3 }

    fun collinear(i: Int, j: Int, k: Int): Boolean = third[i][j] == k
    fun associates(i: Int, j: Int, k: Int): Boolean =
        ring.mul(ring.mul(e[i], e[j]), e[k]) == ring.mul(e[i], ring.mul(e[j], e[k]))

    /** All permutations of `1..7`, as arrays indexed `1..7` (index 0 unused). */
    val permutations: List<IntArray> = run {
        fun go(prefix: List<Int>, rest: List<Int>): List<List<Int>> =
            if (rest.isEmpty()) listOf(prefix)
            else rest.flatMap { x -> go(prefix + x, rest - x) }
        go(emptyList(), units.toList()).map { p -> intArrayOf(0) + p.toIntArray() }
    }

    fun inverse(p: IntArray): IntArray =
        IntArray(8).also { inv -> units.forEach { i -> inv[p[i]] = i } }

    /**
     * The multiplication table of the signed-permuted basis `f_i = ε_i · e_{π(i)}`, packed into two
     * longs: for each pair `i < j`, four bits holding `k` and the sign in `f_i · f_j = ± f_k`.
     */
    fun tableOf(p: IntArray, pInv: IntArray, eps: IntArray): Pair<Long, Long> {
        var lo = 0L
        var hi = 0L
        var slot = 0
        for (i in 1..7) {
            for (j in (i + 1)..7) {
                val k = pInv[third[p[i]][p[j]]]
                val s = eps[i] * eps[j] * eps[k] * sign[p[i]][p[j]]
                val code = (((k - 1) shl 1) or (if (s > 0) 1 else 0)).toLong()
                if (slot < 12) lo = lo or (code shl (4 * slot)) else hi = hi or (code shl (4 * (slot - 12)))
                slot++
            }
        }
        return lo to hi
    }

    val identity = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
    val noSigns = IntArray(8) { 1 }
    val standardTable = tableOf(identity, identity, noSigns)

    /** All `2⁷` sign vectors, as arrays indexed `1..7`. */
    val signVectors: List<IntArray> =
        (0 until 128).map { mask -> IntArray(8) { i -> if (i >= 1 && (mask shr (i - 1)) and 1 == 1) -1 else 1 } }

    // ── The table itself ─────────────────────────────────────────────────────

    "the table is the Fano plane: e_i·e_j = ±e_k with {i, j, k} a line, and it is antisymmetric" {
        units.forEach { i ->
            units.filter { j -> j != i }.forEach { j ->
                third[i][j] shouldBe FanoPlane.thirdPoint(i, j)
                third[j][i] shouldBe third[i][j]
                sign[j][i] shouldBe -sign[i][j]
            }
        }
    }

    "the table agrees with the derived OctonionFanoOrientation" {
        val orientation = OctonionFanoOrientations.deriveFromCurrentBasis()
        units.forEach { i ->
            units.filter { j -> j != i }.forEach { j ->
                orientation.thirdPoint(i, j) shouldBe third[i][j]
                orientation.sign(i, j) shouldBe sign[i][j]
            }
        }
    }

    // ── 168 nonassociative triples ───────────────────────────────────────────

    "of the 35 unordered triples of distinct units, exactly 7 are Fano lines and 28 are not" {
        val unordered = distinctTriples.map { (i, j, k) -> setOf(i, j, k) }.toSet()
        unordered.size shouldBe 35

        val lines = unordered.filter { t -> t.toList().let { (i, j, k) -> collinear(i, j, k) } }
        lines.size shouldBe 7
        lines.toSet() shouldBe FanoPlane.lines.map { line -> line.toList().toSet() }.toSet()
        (unordered - lines.toSet()).size shouldBe 28
    }

    "an ordered triple of distinct units associates if and only if it is collinear" {
        distinctTriples.size shouldBe 210
        distinctTriples.forEach { (i, j, k) ->
            associates(i, j, k) shouldBe collinear(i, j, k)
        }
    }

    "exactly 42 ordered triples of distinct units associate and exactly 168 do not" {
        distinctTriples.count { (i, j, k) -> associates(i, j, k) } shouldBe 42
        distinctTriples.count { (i, j, k) -> !associates(i, j, k) } shouldBe 168
        (28 * 6) shouldBe 168
    }

    "the 168 nonassociative triples anti-associate: (e_i e_j) e_k = -e_i (e_j e_k)" {
        distinctTriples.filterNot { (i, j, k) -> collinear(i, j, k) }.forEach { (i, j, k) ->
            ring.mul(ring.mul(e[i], e[j]), e[k]) shouldBe neg(ring.mul(e[i], ring.mul(e[j], e[k])))
        }
    }

    "every triple with a repeated unit associates, so 168 counts all nonassociative basis triples" {
        val repeated = orderedTriples.filter { (i, j, k) -> setOf(i, j, k).size < 3 }
        repeated.size shouldBe 343 - 210
        repeated.forEach { (i, j, k) -> associates(i, j, k) shouldBe true }
        orderedTriples.count { (i, j, k) -> !associates(i, j, k) } shouldBe 168
    }

    "each Fano line spans a quaternion subalgebra: {±1, ±e_a, ±e_b, ±e_c} is closed and associative" {
        FanoPlane.lines.forEach { line ->
            val (a, b, c) = line.toList()
            val positive = listOf(ring.one, e[a], e[b], e[c])
            val group = (positive + positive.map(::neg)).toSet()
            group.size shouldBe 8

            group.forEach { x ->
                group.forEach { y ->
                    (ring.mul(x, y) in group) shouldBe true
                    group.forEach { z ->
                        ring.mul(ring.mul(x, y), z) shouldBe ring.mul(x, ring.mul(y, z))
                    }
                }
            }
            // It is the quaternion group Q₈: every imaginary unit squares to -1 and they anticommute.
            listOf(e[a], e[b], e[c]).forEach { u -> ring.mul(u, u) shouldBe ring.negOne }
            ring.mul(e[a], e[b]) shouldBe neg(ring.mul(e[b], e[a]))
        }
    }

    // ── The associator and the 84 pairs ──────────────────────────────────────

    "the associator is alternating: it changes sign under every transposition of its arguments" {
        val arb = ArbRationalOctonion.smallRationalOctonion
        checkAll(arb, arb, arb) { x, y, z ->
            val a = associator(x, y, z)
            associator(y, x, z) shouldBe neg(a)
            associator(x, z, y) shouldBe neg(a)
            associator(z, y, x) shouldBe neg(a)
            // ... and so it is invariant under cyclic permutations.
            associator(y, z, x) shouldBe a
            associator(z, x, y) shouldBe a
        }
    }

    "the associator vanishes whenever two of its arguments coincide (alternativity and flexibility)" {
        val arb = ArbRationalOctonion.smallRationalOctonion
        checkAll(arb, arb) { x, y ->
            associator(x, x, y) shouldBe ring.zero
            associator(y, x, x) shouldBe ring.zero
            associator(x, y, x) shouldBe ring.zero
        }
    }

    "on a nonassociative basis triple the associator is ±2·e_m, with m the seventh point" {
        val two = ring.fromBigInt(2.toBigInteger())
        distinctTriples.filterNot { (i, j, k) -> collinear(i, j, k) }.forEach { (i, j, k) ->
            val m = third[third[i][j]][k]
            val spanned = setOf(i, j, k, third[i][j], third[j][k], third[i][k])
            spanned.size shouldBe 6
            (m in spanned) shouldBe false

            val a = associator(e[i], e[j], e[k])
            val twoEm = ring.mul(two, e[m])
            (a == twoEm || a == neg(twoEm)) shouldBe true
            a shouldBe ring.mul(two, ring.mul(ring.mul(e[i], e[j]), e[k]))
        }
    }

    "reversal (i, j, k) ↔ (k, j, i) pairs the 168 nonassociative triples into 84 pairs with opposite associators" {
        val nonassociative = distinctTriples.filterNot { (i, j, k) -> collinear(i, j, k) }.toSet()
        nonassociative.size shouldBe 168

        val pairs = nonassociative.map { t -> setOf(t, Triple(t.third, t.second, t.first)) }.toSet()
        pairs.size shouldBe 84
        pairs.forEach { pair ->
            pair.size shouldBe 2
            (pair.all { it in nonassociative }) shouldBe true
            val (s, t) = pair.toList()
            associator(e[s.first], e[s.second], e[s.third]) shouldBe
                neg(associator(e[t.first], e[t.second], e[t.third]))
        }
    }

    // ── Automorphisms of the oriented Fano plane ─────────────────────────────

    "the unoriented Fano plane has 168 collineations" {
        permutations.count { p ->
            units.all { i -> units.all { j -> i == j || third[p[i]][p[j]] == p[third[i][j]] } }
        } shouldBe 168
    }

    "exactly 21 permutations of the units preserve the multiplication table" {
        permutations.count { p -> tableOf(p, inverse(p), noSigns) == standardTable } shouldBe 21
    }

    "the 21 table-preserving permutations form the Frobenius group C₇ ⋊ C₃" {
        val group = permutations
            .filter { p -> tableOf(p, inverse(p), noSigns) == standardTable }
            .map { p -> p.toList() }
            .toSet()
        group.size shouldBe 21

        val id = identity.toList()
        fun compose(p: List<Int>, q: List<Int>): List<Int> = listOf(0) + units.map { i -> p[q[i]] }
        fun inv(p: List<Int>): List<Int> = inverse(p.toIntArray()).toList()
        fun order(p: List<Int>): Int =
            generateSequence(p) { q -> compose(p, q) }.indexOfFirst { q -> q == id } + 1

        // A group: contains the identity and is closed under composition and inverses.
        (id in group) shouldBe true
        group.forEach { p ->
            (inv(p) in group) shouldBe true
            group.forEach { q -> (compose(p, q) in group) shouldBe true }
        }

        // Element orders 1, 3, 7 with multiplicities 1, 14, 6: no element of order 21, so not cyclic.
        group.groupingBy(::order).eachCount() shouldBe mapOf(1 to 1, 3 to 14, 7 to 6)

        // Nonabelian.
        group.any { p -> group.any { q -> compose(p, q) != compose(q, p) } } shouldBe true

        // The elements of order 7 with the identity form a normal subgroup C₇ ...
        val c7 = group.filter { p -> order(p) == 7 }.toSet() + setOf(id)
        c7.size shouldBe 7
        c7.forEach { p -> c7.forEach { q -> (compose(p, q) in c7) shouldBe true } }
        group.forEach { g ->
            c7.forEach { n -> (compose(compose(g, n), inv(g)) in c7) shouldBe true }
        }

        // ... complemented by any C₃, which acts on it nontrivially.
        val g3 = group.first { p -> order(p) == 3 }
        val c3 = setOf(id, g3, compose(g3, g3))
        (c3 intersect c7) shouldBe setOf(id)
        c7.flatMap { n -> c3.map { h -> compose(n, h) } }.toSet() shouldBe group
        c7.any { n -> compose(compose(g3, n), inv(g3)) != n } shouldBe true
    }

    // ── Signed permutations: 1344 automorphisms and 480 tables ───────────────

    "the signed permutations give 1344 automorphisms and exactly 480 distinct multiplication tables" {
        val tables = HashSet<Pair<Long, Long>>()
        var stabilizer = 0
        var total = 0
        permutations.forEach { p ->
            val pInv = inverse(p)
            signVectors.forEach { eps ->
                val table = tableOf(p, pInv, eps)
                tables.add(table)
                if (table == standardTable) stabilizer++
                total++
            }
        }

        total shouldBe 645_120
        stabilizer shouldBe 1344
        tables.size shouldBe 480
        (total / stabilizer) shouldBe 480
        (5040 / 168 * 16) shouldBe 480
    }

    "the 480 tables are 30 Fano planes, each with 16 octonionic orientations out of 2⁷ = 128" {
        /** The set of lines of a packed table, each line a 7-bit mask of its three points. */
        fun linesOf(table: Pair<Long, Long>): Set<Int> {
            val lines = HashSet<Int>()
            var slot = 0
            for (i in 1..7) {
                for (j in (i + 1)..7) {
                    val word = if (slot < 12) table.first shr (4 * slot) else table.second shr (4 * (slot - 12))
                    val k = ((word and 0xF).toInt() shr 1) + 1
                    lines.add((1 shl i) or (1 shl j) or (1 shl k))
                    slot++
                }
            }
            return lines
        }

        val tables = HashSet<Pair<Long, Long>>()
        permutations.forEach { p ->
            val pInv = inverse(p)
            signVectors.forEach { eps -> tables.add(tableOf(p, pInv, eps)) }
        }

        val byPlane = tables.groupBy(::linesOf)
        byPlane.size shouldBe 30
        byPlane.values.forEach { orientations -> orientations.size shouldBe 16 }
        byPlane.keys.forEach { plane -> plane.size shouldBe 7 }

        // Sign changes alone never move the plane, and give all 16 orientations of the standard one.
        signVectors.map { eps -> tableOf(identity, identity, eps) }.toSet().size shouldBe 16
    }
})
