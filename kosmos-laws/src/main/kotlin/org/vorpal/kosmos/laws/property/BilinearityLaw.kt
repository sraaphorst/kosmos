package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private fun <S : Any> mulAsScalarAction(mulS: BinOp<S>): Action<S, S> =
    Action(mulS.symbol, mulS::invoke)

private sealed interface BilinearityCore<S : Any, V : Any, W : Any, X : Any> {
    val f: (V, W) -> X

    // Additions
    val addX: BinOp<X>
    val addV: BinOp<V>
    val addW: BinOp<W>

    // Scalar actions
    val scalarActionV: Action<S, V>  // S × V → V
    val scalarActionW: Action<S, W>  // S × W → W
    val scalarActionX: Action<S, X>  // S × X → X

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

    private fun expr(left: String, right: String): String = "⟨$left, $right⟩"

    // (x, y, z) with x, y ∈ V and z ∈ W
    private fun vvwArb() = Arb.triple(arbV, arbV, arbW)

    // (x, y, z) with x ∈ V and y, z ∈ W
    private fun vwwArb() = Arb.triple(arbV, arbW, arbW)

    // (s, x, z) with s ∈ S, x ∈ V, z ∈ W
    private fun svwArb() = Arb.triple(scalarArb, arbV, arbW)

    /**
     * Left additivity: `f(x + y, z) = f(x, z) + f(y, z)`
     */
    suspend fun leftAdditivityCheck() {
        checkAll(vvwArb()) { (x, y, z) ->
            val left = f(addV(x, y), z)
            val right = addX(f(x, z), f(y, z))

            withClue(leftAdditivityFailure(x, y, z, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun leftAdditivityFailure(
        x: V, y: V, z: W,
        left: X, right: X
    ): () -> String = {
        buildString {
            val sx = prV(x)
            val sy = prV(y)
            val sz = prW(z)
            val sl = prX(left)
            val sr = prX(right)
            appendLine("Left additivity of $SYMBOL failed:")
            appendLine("${expr("$sx ${addV.symbol} $sy", sz)} = $sl")
            appendLine("${expr(sx, sz)} ${addX.symbol} ${expr(sy, sz)} = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    /**
     * Left homogeneity: `f(a·x, z) = a ⋅ f(x, z)`
     */
    suspend fun leftHomogeneityCheck() {
        checkAll(svwArb()) { (a, x, z) ->
            val left = f(scalarActionV(a, x), z)
            val right = scalarActionX(a, f(x, z))

            withClue(leftHomogeneityFailure(a, x, z, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun leftHomogeneityFailure(
        a: S, x: V, z: W,
        left: X, right: X
    ): () -> String = {
        buildString {
            val sa = prS(a)
            val sx = prV(x)
            val sz = prW(z)
            val sl = prX(left)
            val sr = prX(right)
            appendLine("Left homogeneity of $SYMBOL failed:")
            appendLine("${expr("$sa ${scalarActionV.symbol} $sx", sz)} = $sl")
            appendLine("$sa ${scalarActionX.symbol} ${expr(sx, sz)} = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    /**
     * Right additivity: `f(x, y + z) = f(x, y) + f(x, z)`
     */
    suspend fun rightAdditivityCheck() {
        checkAll(vwwArb()) { (x, y, z) ->
            val left = f(x, addW(y, z))
            val right = addX(f(x, y), f(x, z))

            withClue(rightAdditivityFailure(x, y, z, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun rightAdditivityFailure(
        x: V, y: W, z: W,
        left: X, right: X
    ): () -> String = {
        buildString {
            val sx = prV(x)
            val sy = prW(y)
            val sz = prW(z)
            val sl = prX(left)
            val sr = prX(right)
            appendLine("Right additivity of $SYMBOL failed:")
            appendLine("${expr(sx, "$sy ${addW.symbol} $sz")} = $sl")
            appendLine("${expr(sx, sy)} ${addX.symbol} ${expr(sx, sz)} = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    /**
     * Right homogeneity: `f(x, b·z) = b ⋅ f(x, z)`
     */
    suspend fun rightHomogeneityCheck() {
        checkAll(svwArb()) { (b, x, z) ->
            val left = f(x, scalarActionW(b, z))
            val right = scalarActionX(b, f(x, z))

            withClue(rightHomogeneityFailure(b, x, z, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun rightHomogeneityFailure(
        b: S, x: V, z: W,
        left: X, right: X
    ): () -> String = {
        buildString {
            val sb = prS(b)
            val sx = prV(x)
            val sz = prW(z)
            val sl = prX(left)
            val sr = prX(right)
            appendLine("Right homogeneity of $SYMBOL failed:")
            appendLine("${expr(sx, "$sb ${scalarActionW.symbol} $sz")} = $sl")
            appendLine("$sb ${scalarActionX.symbol} ${expr(sx, sz)} = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    companion object {
        const val SYMBOL = "⟨·,·⟩"
    }
}

class LeftBilinearityLaw<S : Any, V : Any, W : Any, X : Any>(
    override val f: (V, W) -> X,
    override val addX: BinOp<X>,
    override val addV: BinOp<V>,
    override val addW: BinOp<W>,
    override val scalarActionV: Action<S, V>,
    override val scalarActionW: Action<S, W>,
    override val scalarActionX: Action<S, X>,
    override val scalarArb: Arb<S>,
    override val arbV: Arb<V>,
    override val arbW: Arb<W>,
    override val eq: Eq<X> = Eq.default(),
    override val prS: Printable<S> = Printable.default(),
    override val prV: Printable<V> = Printable.default(),
    override val prW: Printable<W> = Printable.default(),
    override val prX: Printable<X> = Printable.default()
) : TestingLaw, BilinearityCore<S, V, W, X> {
    override val name = "left bilinearity ${BilinearityCore.SYMBOL}"
    override suspend fun test() {
        leftAdditivityCheck()
        leftHomogeneityCheck()
    }
}

class RightBilinearityLaw<S : Any, V : Any, W : Any, X : Any>(
    override val f: (V, W) -> X,
    override val addX: BinOp<X>,
    override val addV: BinOp<V>,
    override val addW: BinOp<W>,
    override val scalarActionV: Action<S, V>,
    override val scalarActionW: Action<S, W>,
    override val scalarActionX: Action<S, X>,
    override val scalarArb: Arb<S>,
    override val arbV: Arb<V>,
    override val arbW: Arb<W>,
    override val eq: Eq<X> = Eq.default(),
    override val prS: Printable<S> = Printable.default(),
    override val prV: Printable<V> = Printable.default(),
    override val prW: Printable<W> = Printable.default(),
    override val prX: Printable<X> = Printable.default()
) : TestingLaw, BilinearityCore<S, V, W, X> {
    override val name = "right bilinearity ${BilinearityCore.SYMBOL}"
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
    override val scalarActionV: Action<S, V>,
    override val scalarActionW: Action<S, W>,
    override val scalarActionX: Action<S, X>,
    override val scalarArb: Arb<S>,
    override val arbV: Arb<V>,
    override val arbW: Arb<W>,
    override val eq: Eq<X> = Eq.default(),
    override val prS: Printable<S> = Printable.default(),
    override val prV: Printable<V> = Printable.default(),
    override val prW: Printable<W> = Printable.default(),
    override val prX: Printable<X> = Printable.default()
) : TestingLaw, BilinearityCore<S, V, W, X> {
    override val name = "bilinearity ${BilinearityCore.SYMBOL}"
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
        scalarAction: Action<S, V>,
        scalarArb: Arb<S>,
        vectorArb: Arb<V>,
        eq: Eq<S> = Eq.default(),
        prS: Printable<S> = Printable.default(),
        prV: Printable<V> = Printable.default()
    ): TestingLaw = LeftBilinearityLaw(
        f, addS, addV, addV,
        scalarAction, scalarAction, mulAsScalarAction(mulS),
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
        scalarAction: Action<S, V>,
        scalarArb: Arb<S>,
        vectorArb: Arb<V>,
        eq: Eq<S> = Eq.default(),
        prS: Printable<S> = Printable.default(),
        prV: Printable<V> = Printable.default()
    ): TestingLaw = RightBilinearityLaw(
        f, addS, addV, addV,
        scalarAction, scalarAction, mulAsScalarAction(mulS),
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
        scalarAction: Action<S, V>,
        scalarArb: Arb<S>,
        vectorArb: Arb<V>,
        eq: Eq<S> = Eq.default(),
        prS: Printable<S> = Printable.default(),
        prV: Printable<V> = Printable.default()
    ): TestingLaw = BilinearityLaw(
        f, addS, addV, addV,
        scalarAction, scalarAction, mulAsScalarAction(mulS),
        scalarArb, vectorArb, vectorArb,
        eq, prS, prV, prV, prS
    )
}
