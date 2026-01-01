package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.util.midfix

private sealed interface BilinearityCore<S : Any, V : Any, W : Any, X : Any> {
    val f: (V, W) -> X

    val symbol: String
        get() = Symbols.BILINEAR

    // Additions
    val addX: BinOp<X>
    val addV: BinOp<V>
    val addW: BinOp<W>

    // Scalar actions
    val scalarActionV: LeftAction<S, V>  // S × V → V
    val scalarActionW: LeftAction<S, W>  // S × W → W
    val scalarActionX: LeftAction<S, X>  // S × X → X

    // Generators
    val scalarArb: Arb<S>
    val arbV: Arb<V>
    val arbW: Arb<W>

    // Equality & printing on X and friends
    val eq: Eq<X>
    val prS: Printable<S>
    val prV: Printable<V>
    val prW: Printable<W>
    val prX: Printable<X>

    // TODO: The expr should not be hardcoded, but it looks much nicer this way.
    // TODO: We need to figure out a better rendering mechanism at some point.
    private fun expr(left: String, right: String): String =
        if (symbol.contains("·,·")) symbol.replace("·,·", "$left, $right")
        else "$symbol($left, $right)"

    // (v1, v2, w) with v1, v2 ∈ V and w ∈ W
    private fun vvwArb() = Arb.triple(arbV, arbV, arbW)

    // (v, w1, w2) with v ∈ V and w1, w2 ∈ W
    private fun vwwArb() = Arb.triple(arbV, arbW, arbW)

    // (s, v, w) with s ∈ S, v ∈ V, w ∈ W
    private fun svwArb() = Arb.triple(scalarArb, arbV, arbW)

    /**
     * Left additivity: `f(v1 + v2, w) = f(v1, w) + f(v2, w)`
     */
    suspend fun leftAdditivityCheck() {
        checkAll(vvwArb()) { (v1, v2, w) ->
            val left = f(addV(v1, v2), w)
            val right = addX(f(v1, w), f(v2, w))

            withClue(leftAdditivityFailure(v1, v2, w, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun leftAdditivityFailure(
        v1: V, v2: V, w: W,
        left: X, right: X
    ): () -> String = {
        buildString {
            val sx = prV(v1)
            val sy = prV(v2)
            val sz = prW(w)
            val sl = prX(left)
            val sr = prX(right)
            appendLine("Left additivity of $symbol failed:")
            appendLine("${expr("$sx${addV.symbol.midfix()}$sy", sz)} = $sl")
            appendLine("${expr(sx, sz)}${addX.symbol.midfix()}${expr(sy, sz)} = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    /**
     * Left homogeneity: `f(s·v, w) = s ⋅ f(v, w)`
     */
    suspend fun leftHomogeneityCheck() {
        checkAll(svwArb()) { (s, v, w) ->
            val left = f(scalarActionV(s, v), w)
            val right = scalarActionX(s, f(v, w))

            withClue(leftHomogeneityFailure(s, v, w, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun leftHomogeneityFailure(
        s: S, v: V, w: W,
        left: X, right: X
    ): () -> String = {
        buildString {
            val sa = prS(s)
            val sx = prV(v)
            val sz = prW(w)
            val sl = prX(left)
            val sr = prX(right)
            appendLine("Left homogeneity of $symbol failed:")
            appendLine("${expr("$sa${scalarActionV.symbol.midfix()}$sx", sz)} = $sl")
            appendLine("$sa${scalarActionX.symbol.midfix()}${expr(sx, sz)} = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    /**
     * Right additivity: `f(v, w1 + w2) = f(v, w1) + f(v, w2)`
     */
    suspend fun rightAdditivityCheck() {
        checkAll(vwwArb()) { (v, w1, w2) ->
            val left = f(v, addW(w1, w2))
            val right = addX(f(v, w1), f(v, w2))

            withClue(rightAdditivityFailure(v, w1, w2, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun rightAdditivityFailure(
        v: V, w1: W, w2: W,
        left: X, right: X
    ): () -> String = {
        buildString {
            val sx = prV(v)
            val sy = prW(w1)
            val sz = prW(w2)
            val sl = prX(left)
            val sr = prX(right)
            appendLine("Right additivity of $symbol failed:")
            appendLine("${expr(sx, "$sy${addW.symbol.midfix()}$sz")} = $sl")
            appendLine("${expr(sx, sy)}${addX.symbol.midfix()}${expr(sx, sz)} = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    /**
     * Right homogeneity: `f(v, s·w) = s ⋅ f(v, w)`
     */
    suspend fun rightHomogeneityCheck() {
        checkAll(svwArb()) { (s, v, w) ->
            val left = f(v, scalarActionW(s, w))
            val right = scalarActionX(s, f(v, w))

            withClue(rightHomogeneityFailure(s, v, w, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun rightHomogeneityFailure(
        s: S, v: V, w: W,
        left: X, right: X
    ): () -> String = {
        buildString {
            val sb = prS(s)
            val sx = prV(v)
            val sz = prW(w)
            val sl = prX(left)
            val sr = prX(right)
            appendLine("Right homogeneity of $symbol failed:")
            appendLine("${expr(sx, "$sb${scalarActionW.symbol.midfix()}$sz")} = $sl")
            appendLine("$sb${scalarActionX.symbol.midfix()}${expr(sx, sz)} = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }
}

class LinearInFirstArgLaw<S : Any, V : Any, W : Any, X : Any>(
    override val f: (V, W) -> X,
    override val addX: BinOp<X>,
    override val addV: BinOp<V>,
    override val addW: BinOp<W>,
    override val scalarActionV: LeftAction<S, V>,
    override val scalarActionW: LeftAction<S, W>,
    override val scalarActionX: LeftAction<S, X>,
    override val scalarArb: Arb<S>,
    override val arbV: Arb<V>,
    override val arbW: Arb<W>,
    override val eq: Eq<X> = Eq.default(),
    override val prS: Printable<S> = Printable.default(),
    override val prV: Printable<V> = Printable.default(),
    override val prW: Printable<W> = Printable.default(),
    override val prX: Printable<X> = Printable.default()
) : TestingLaw, BilinearityCore<S, V, W, X> {
    override val name = "linearity (1st arg) $symbol"
    override suspend fun test() {
        leftAdditivityCheck()
        leftHomogeneityCheck()
    }
}

class LinearInSecondArgLaw<S : Any, V : Any, W : Any, X : Any>(
    override val f: (V, W) -> X,
    override val addX: BinOp<X>,
    override val addV: BinOp<V>,
    override val addW: BinOp<W>,
    override val scalarActionV: LeftAction<S, V>,
    override val scalarActionW: LeftAction<S, W>,
    override val scalarActionX: LeftAction<S, X>,
    override val scalarArb: Arb<S>,
    override val arbV: Arb<V>,
    override val arbW: Arb<W>,
    override val eq: Eq<X> = Eq.default(),
    override val prS: Printable<S> = Printable.default(),
    override val prV: Printable<V> = Printable.default(),
    override val prW: Printable<W> = Printable.default(),
    override val prX: Printable<X> = Printable.default()
) : TestingLaw, BilinearityCore<S, V, W, X> {
    override val name = "linearity (2nd arg) $symbol"
    override suspend fun test() {
        rightAdditivityCheck()
        rightHomogeneityCheck()
    }
}

/**
 * Bilinearity of a map `f : V × W → X` over a scalar type `S`:
 *
 * 1. `f(x + y, z) = f(x, z) + f(y, z)`
 * 2. `f(a · x, z) = a ⋅ f(x, z)`
 * 3. `f(x, y + z) = f(x, y) + f(x, z)`
 * 4. `f(x, b · z) = b ⋅ f(x, z)`
 */
class BilinearityLaw<S : Any, V : Any, W : Any, X : Any>(
    override val f: (V, W) -> X,
    override val addX: BinOp<X>,
    override val addV: BinOp<V>,
    override val addW: BinOp<W>,
    override val scalarActionV: LeftAction<S, V>,
    override val scalarActionW: LeftAction<S, W>,
    override val scalarActionX: LeftAction<S, X>,
    override val scalarArb: Arb<S>,
    override val arbV: Arb<V>,
    override val arbW: Arb<W>,
    override val eq: Eq<X> = Eq.default(),
    override val prS: Printable<S> = Printable.default(),
    override val prV: Printable<V> = Printable.default(),
    override val prW: Printable<W> = Printable.default(),
    override val prX: Printable<X> = Printable.default()
) : TestingLaw, BilinearityCore<S, V, W, X> {
    override val name = "bilinearity $symbol"
    override suspend fun test() {
        leftAdditivityCheck()
        leftHomogeneityCheck()
        rightAdditivityCheck()
        rightHomogeneityCheck()
    }
}

/**
 * Simplified left-bilinearity of a map `f : V × V → S` in the first argument, i.e. when V = W and X = S.
 *
 * 1. `f(x + y, z) = f(x, z) + f(y, z)`
 * 2. `f(a · x, z) = a * f(x, z)`
 */
object LeftBilinearFormLaw {
    operator fun <S : Any, V : Any> invoke(
        f: (V, V) -> S,
        addS: BinOp<S>,
        mulS: BinOp<S>,
        addV: BinOp<V>,
        scalarAction: LeftAction<S, V>,
        scalarArb: Arb<S>,
        vectorArb: Arb<V>,
        eq: Eq<S> = Eq.default(),
        prS: Printable<S> = Printable.default(),
        prV: Printable<V> = Printable.default()
    ): TestingLaw = LinearInFirstArgLaw(
        f, addS, addV, addV,
        scalarAction, scalarAction, mulS,
        scalarArb, vectorArb, vectorArb,
        eq, prS, prV, prV, prS
    )
}

/**
 * Simplified right-bilinearity of a map `f : V × V → S` in the second argument, i.e. when V = W and X = S.
 *
 * 1. `f(x, y + z) = f(x, y) + f(x, z)`
 * 2. `f(x, b · z) = b * f(x, z)`
 */
object RightBilinearFormLaw {
    operator fun <S : Any, V : Any> invoke(
        f: (V, V) -> S,
        addS: BinOp<S>,
        mulS: BinOp<S>,
        addV: BinOp<V>,
        scalarAction: LeftAction<S, V>,
        scalarArb: Arb<S>,
        vectorArb: Arb<V>,
        eq: Eq<S> = Eq.default(),
        prS: Printable<S> = Printable.default(),
        prV: Printable<V> = Printable.default()
    ): TestingLaw = LinearInSecondArgLaw(
        f, addS, addV, addV,
        scalarAction, scalarAction, mulS,
        scalarArb, vectorArb, vectorArb,
        eq, prS, prV, prV, prS
    )
}

/**
 * Simplified bilinearity of a map `f : V × V → S` in both arguments, i.e. when V = W and X = S.
 *
 * 1. `f(x + y, z) = f(x, z) + f(y, z)`
 * 2. `f(a · x, z) = a * f(x, z)`
 * 3. `f(x, y + z) = f(x, y) + f(x, z)`
 * 4. `f(x, b · z) = b * f(x, z)`
 */
object BilinearFormLaw {
    operator fun <S : Any, V : Any> invoke(
        f: (V, V) -> S,
        addS: BinOp<S>,
        mulS: BinOp<S>,
        addV: BinOp<V>,
        scalarAction: LeftAction<S, V>,
        scalarArb: Arb<S>,
        vectorArb: Arb<V>,
        eq: Eq<S> = Eq.default(),
        prS: Printable<S> = Printable.default(),
        prV: Printable<V> = Printable.default()
    ): TestingLaw = BilinearityLaw(
        f, addS, addV, addV,
        scalarAction, scalarAction, mulS,
        scalarArb, vectorArb, vectorArb,
        eq, prS, prV, prV, prS
    )
}
