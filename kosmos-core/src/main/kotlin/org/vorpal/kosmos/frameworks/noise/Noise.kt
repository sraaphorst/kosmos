package org.vorpal.kosmos.frameworks.noise

import org.vorpal.kosmos.analysis.ScalarField
// TODO: SHOULD BE VEC3R.
import org.vorpal.kosmos.linear.Vec2R

typealias NoiseField = ScalarField<Double, Vec2R>
/**
 * General noise generator.
 * Implementations can be treated as instances of a functor from R^n -> R.
 */
fun interface Noise {
    operator fun invoke(x: Double, y: Double, z: Double): Double

    companion object {
        /**
         * Constructs a *fractal Brownian motion* (fBm) noise generator from a base [Noise] function.
         *
         * Fractal Brownian motion combines multiple octaves of coherent noise
         * (such as [PerlinNoise] or [SimplexNoise])
         * at increasing frequencies and decreasing amplitudes to create complex, natural-looking textures.
         *
         * Formally:
         * ```
         * fBm(x, y, z) = Σ_{i=0}^{octaves-1} (gain^i) * base(x * lacunarity^i, y * lacunarity^i, z * lacunarity^i)
         * ```
         *
         * Each successive octave increases spatial frequency by [lacunarity] and decreases amplitude by [gain].
         * The result is a fractal-like accumulation of noise layers that produces smooth,
         * self-similar detail.
         *
         * @param base the underlying [Noise] function used as the source for each octave (e.g. Perlin, Simplex).
         * @param octaves the number of noise layers (octaves) to sum. Higher values yield more detail.
         * @param lacunarity the frequency multiplier between successive octaves.
         *   Typical values range from `1.5` to `3.0`.
         *   Larger values increase the roughness of the resulting pattern.
         * @param gain the amplitude scaling factor between successive octaves.
         *   Typical values range from `0.3` to `0.7`.
         *   Smaller values reduce higher-octave influence.
         *
         * @return a new [Noise] function representing the fractal Brownian motion of the given parameters.
         *
         * * ### Example
         * ```kotlin
         * // Create a base noise generator (e.g., Perlin noise)
         * val perlin = PerlinNoise(Random(seed = 42))
         *
         * // Construct an fBm noise with 5 octaves, doubling frequency and halving amplitude each layer
         * val fbmNoise = Noise.fbm(
         *     base = perlin,
         *     octaves = 5,
         *     lacunarity = 2.0,
         *     gain = 0.5
         * )
         *
         * // Sample the fBm noise in 3D space
         * val height = fbmNoise(12.5, 8.3, 0.0)
         * println("fBm height = $height")
         * ```
         *
         * @see Noise for the general functional interface representing 3D noise mappings.
         */
        fun fbm(base: Noise, octaves: Int, lacunarity: Double, gain: Double): Noise =
            Noise { x, y, z ->
                tailrec fun aux(
                    o: Int = octaves,
                    freq: Double = 1.0,
                    amp: Double = 1.0,
                    acc: Double = 0.0): Double =
                    if (o == 0) acc
                    else aux(
                        o - 1,
                        freq * lacunarity,
                        amp * gain,
                        acc + amp * base(x * freq, y * freq, z * freq)
                    )
                aux()
            }
    }
}
