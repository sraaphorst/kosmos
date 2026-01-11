package org.vorpal.kosmos.core

import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.std.Rational
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

/** Core type + fluent combinators. */

fun interface Eq<A> {
    fun eqv(a: A, b: A): Boolean
    operator fun invoke(a: A, b: A): Boolean = eqv(a, b)
    fun neqv(a: A, b: A): Boolean = !eqv(a, b)

    companion object {
        fun <A: Any> default(): Eq<A> = Eq { a, b -> a == b }
    }
}

fun <A> Eq<A>.assertEquals(x: A, y: A) =
    require(eqv(x, y)) { "Law failure: expected $x == $y" }

/** Contravariant: reuse Eq<B> via projection A -> B. */
inline fun <A, B> Eq<B>.contramap(crossinline f: (A) -> B): Eq<A> =
    Eq { x, y -> eqv(f(x), f(y)) }

/** Alias for contramap to read nicely with "key selectors." */
inline fun <A, K> Eq<K>.by(crossinline key: (A) -> K): Eq<A> = contramap(key)

/** Fluent derived Eqs for common containers. */

fun <A> Eq<A>.nullable(): Eq<A?> =
    Eq { x, y ->
        if (x == null && y == null) true
        else if (x == null || y == null) false
        else eqv(x, y)
    }

fun <A> Eq<A>.list(): Eq<List<A>> =
    Eq { xs, ys -> xs.size == ys.size && xs.indices.all { i -> eqv(xs[i], ys[i]) } }

fun <A> Eq<A>.sequence(): Eq<Sequence<A>> =
    Eq { xs, ys ->
        val ix = xs.iterator(); val iy = ys.iterator()
        while (ix.hasNext() && iy.hasNext()) if (neqv(ix.next(), iy.next())) return@Eq false
        !ix.hasNext() && !iy.hasNext()
    }

fun <A> Eq<A>.array(): Eq<Array<A>> =
    Eq { xs, ys -> xs.size == ys.size && xs.indices.all { i -> eqv(xs[i], ys[i]) } }

/** Unordered set equality using this Eq for membership. Runs in O(n^2). */
fun <A> Eq<A>.finiteSet(): Eq<FiniteSet<A>> =
    Eq { sa, sb ->
        if (sa.size != sb.size) false
        else {
            fun containsEq(s: FiniteSet<A>, a: A) = s.any { eqv(it, a) }
            sa.all { containsEq(sb, it) } && sb.all { containsEq(sa, it) }
        }
    }

/** Map equality with strict key lookup (uses equals() on keys). */
fun <K, V> Eq<V>.mapStrict(): Eq<Map<K, V>> =
    Eq { ma, mb ->
        if (ma.size != mb.size) false
        else ma.entries.all { (k, va) -> mb[k]?.let { vb -> eqv(va, vb) } == true }
    }

/** Map equality up to Eq on keys and values (no hashing assumptions). */
fun <K, V> mapEq(eqK: Eq<K>, eqV: Eq<V>): Eq<Map<K, V>> =
    Eq { ma, mb ->
        if (ma.size != mb.size) return@Eq false
        fun findKeyLike(key: K, m: Map<K, V>): Map.Entry<K, V>? =
            m.entries.firstOrNull { (k, _) -> eqK(k, key) }

        ma.entries.all { (ka, va) ->
            val hit = findKeyLike(ka, mb) ?: return@all false
            eqV.eqv(va, hit.value)
        } && mb.entries.all { (kb, vb) ->
            val hit = ma.entries.firstOrNull { (k, _) -> eqK(k, kb) } ?: return@all false
            eqV(hit.value, vb)
        }
    }

/** Product helpers. */

fun <A, B> pairEq(eqA: Eq<A>, eqB: Eq<B>): Eq<Pair<A, B>> =
    Eq { (a1, b1), (a2, b2) -> eqA(a1, a2) && eqB(b1, b2) }

fun <A, B, C> tripleEq(eqA: Eq<A>, eqB: Eq<B>, eqC: Eq<C>): Eq<Triple<A, B, C>> =
    Eq { (a1, b1, c1), (a2, b2, c2) ->
        eqA(a1, a2) && eqB(b1, b2) && eqC(c1, c2)
    }

/** Instances and factories. */

object Eqs {
    // Primitives / basic
    val boolean: Eq<Boolean> = Eq { a, b -> a == b }
    val byte: Eq<Byte> = Eq { a, b -> a == b }
    val short: Eq<Short> = Eq { a, b -> a == b }
    val int: Eq<Int> = Eq { a, b -> a == b }
    val long: Eq<Long> = Eq { a, b -> a == b }
    val char: Eq<Char> = Eq { a, b -> a == b }
    val string: Eq<String> = Eq { a, b -> a == b }
    val uuid: Eq<UUID> = Eq { a, b -> a == b }

    // Big numbers
    val bigInteger: Eq<BigInteger> = Eq { a, b -> a == b }
    /** BigDecimal structural equality with normalized scale (so 1.0 == 1.00). */
    val bigDecimalNormalized: Eq<BigDecimal> =
        Eq { a, b -> a.stripTrailingZeros().compareTo(b.stripTrailingZeros()) == 0 }
    /** BigDecimal strict (scale-sensitive) equality. */
    val bigDecimalStrict: Eq<BigDecimal> = Eq { a, b -> a == b }

    /** Time */
    val instant: Eq<Instant>               = Eq { a, b -> a.compareTo(b) == 0 }
    val localDate: Eq<LocalDate>           = Eq { a, b -> a.compareTo(b) == 0 }
    val localTime: Eq<LocalTime>           = Eq { a, b -> a.compareTo(b) == 0 }
    val localDateTime: Eq<LocalDateTime>   = Eq { a, b -> a.compareTo(b) == 0 }
    val offsetDateTime: Eq<OffsetDateTime> = Eq { a, b -> a.isEqual(b) }     // same instant
    val zonedDateTime: Eq<ZonedDateTime>   = Eq { a, b -> a.isEqual(b) }     // same instant
    val duration: Eq<Duration>             = Eq { a, b -> a.compareTo(b) == 0 }

    /** Reals / Floats: factories for strict, epsilon, ULPs... */

    /** Strict IEEE equality (NaN != NaN), mostly for low-level cases. */
    private fun canon0(x: Real): Real =
        if (x == 0.0) 0.0 else x

    val realStrict: Eq<Real> = Eq { a, b ->
        canon0(a) == canon0(b)
    }

    private fun canon0(x: Float): Float =
        if (x == 0.0f) 0.0f else x

    val floatStrict: Eq<Float> = Eq { a, b -> a == b }

    /**
     * Treat NaNs as not equal to anything, and use absolute/relative tolerance.
     */
    fun realApprox(
        absTol: Real = 1e-12,
        relTol: Real = 1e-12
    ): Eq<Real> = Eq { x, y ->
        if (x.isNaN() || y.isNaN()) false
        else if (x == 0.0 && y == 0.0) true
        else if (x.isInfinite() || y.isInfinite()) x == y
        else {
            val diff = abs(x - y)
            val scale = max(abs(x), abs(y))
            diff <= absTol + relTol * scale
        }
    }

    fun floatApprox(
        absTol: Float = 1e-6f,
        relTol: Float = 1e-6f
    ): Eq<Float> = Eq { x, y ->
        if (x.isNaN() || y.isNaN()) false
        else if (x == 0.0f && y == 0.0f) true
        else if (x.isInfinite() || y.isInfinite()) x == y
        else {
            val diff = abs(x - y)
            val scale = max(abs(x), abs(y))
            diff <= absTol + relTol * scale
        }
    }

    /** ULP-based for Real. `maxUlps` ~ 2..16 is typical. */
    fun realUlps(maxUlps: Int = 8): Eq<Real> = Eq { a, b ->
        if (a == b) true
        else if (a.isNaN() && b.isNaN()) true
        else {
            val ulp = max(Math.ulp(a), Math.ulp(b)) * maxUlps
            abs(a - b) <= ulp
        }
    }

    /** ULP-based for Float. */
    fun floatUlps(maxUlps: Int = 8): Eq<Float> = Eq { a, b ->
        if (a == b) true
        else if (a.isNaN() && b.isNaN()) true
        else {
            val ulp = max(Math.ulp(a), Math.ulp(b)) * maxUlps
            abs(a - b) <= ulp
        }
    }

    val rational: Eq<Rational> = Eq { x, y -> x == y }
}