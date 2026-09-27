# Hypercomplex Number Systems

This package implements the classical tower of normed division algebras
built by iterated Cayley–Dickson doubling, together with their
integer- and rational-coefficient subrings:

$$
\mathbb{R} \;\subset\; \mathbb{C} \;\subset\; \mathbb{H} \;\subset\; \mathbb{O}
$$

At each level, the algebraic structure weakens:
$\mathbb{C}$ loses ordering,
$\mathbb{H}$ loses commutativity,
$\mathbb{O}$ loses associativity.
Every level retains a multiplicative norm ($|ab| = |a||b|$) and
hence a division algebra structure.

---

## One Construction, Many Algebras

Every algebra in this package is carried by the Cayley–Dickson pair `CD<A>` and gets its
arithmetic from `CayleyDickson` (in `algebra.structures`). Nothing re-implements a doubling
product by hand. The variations come from two choices: the involution on the base, and `σ`.

**Base with a nontrivial involution** (`CayleyDickson.usual` / `split`) gives the
noncommutative tower:

| Algebra | Carrier | Construction |
|---------|---------|--------------|
| ℍ, ℍ_ℚ, Lip | `Quaternion`, `RationalQuaternion`, `LipschitzQuaternion` (typealiases of `CD<…>`) | `CD⟨ℂ⟩`, `CD⟨ℚ(i)⟩`, `CD⟨ℤ[i]⟩` |
| 𝕆, 𝕆_ℚ, Gravesian | `Octonion`, `RationalOctonion`, `GravesianOctonion` | `CD⟨ℍ⟩`, `CD⟨ℍ_ℚ⟩`, `CD⟨Lip⟩` |

**Commutative base with the *trivial* involution** (`CayleyDickson.commutative(base, σ)`)
gives the commutative quadratic extension `R[x]/(x² + σ)`:

| σ | x² | Algebra | Carrier |
|---|----|---------|---------|
| 1  | −1 | ℂ = ℝ[i]/(i² + 1) | `Complex` (typealias of `CD<Real>`) |
| 1  | −1 | 𝔹 = ℂ[j]/(j² + 1) (bicomplex) | `Bicomplex` (value class over `CD<Complex>`) |
| −1 | +1 | split-complex `R[j]/(j² − 1)` | `SplitComplex<R>` (value class over `CD<R>`) |
| 0  | 0  | dual numbers `F[ε]/(ε²)` | `Dual<F>` (value class over `CD<F>`) |

`Bicomplex`, `Dual` and `SplitComplex` are value classes, not typealiases. `Bicomplex` and
`Quaternion` are both carried by `CD<Complex>`, and `Dual<Real>` and `SplitComplex<Real>` share
`CD<Real>` with `Complex`. The algebras differ, so the Kotlin types must too. Their ring
structures are transported from the Cayley–Dickson ring along the wrapper with
`CarrierTransport`.

**One construction per tier, three coefficient rings.** ℍ, ℍ_ℚ and the Lipschitz quaternions are
built by a single internal `QuaternionTier` over ℂ, ℚ(i) and ℤ[i]; 𝕆, 𝕆_ℚ and the Gravesian
octonions by a single `OctonionTier` over those. A tier supplies the ring, basis, coordinates,
scalar action, norm, embeddings and printable; the public `*Algebras` objects add only what
really differs per tier (reciprocals, vector spaces or ℤ-modules, conversions).

**What `CayleyDickson` promises.** `usual`, `split` and `commutative` are the safe factories.
`withSigma` accepts any `σ`, but the conjugation is an anti-automorphism only when `σ` is central
and fixed by the base involution (always true for the generalized quaternion algebras `(a, b)_F`,
where `σ` is a scalar). `associative` types the double of a *commutative* base as an associative
`InvolutiveRing`; this is the quaternion step. The kernel is tested directly, level by level, in
`CayleyDicksonSpec`.

**Orders are substructures, not new implementations.** A `HurwitzQuaternion` is an element of
the Hurwitz order `𝓗 ⊂ ℍ_ℚ`, represented by its underlying `RationalQuaternion`. Its
constructor is private: checked construction goes through `HurwitzQuaternion(q)`, and the ring
operations, which provably stay in `𝓗`, use a trusted internal `wrap`. The ring structure of `𝓗`
is the restriction of `ℍ_ℚ`, and its norm lands in ℤ. Only the 24 units are invertible *in* `𝓗`;
every other nonzero element is inverted in `ℍ_ℚ` after the inclusion
`HurwitzToRationalQuaternionMonomorphism`. The same holds for the Lipschitz quaternions (8 units)
and the Gravesian octonions (16 units): their `reciprocal` is defined on the units only.

**Reciprocals follow one contract** (documented on `HasReciprocal`): `hasReciprocal` is the single
source of truth, `reciprocal` throws `ArithmeticException` exactly where it returns `false`, and
`reciprocalOption` agrees with both. Exact algebras use exact nonzero-ness. Floating-point
algebras use *numerical* invertibility: the norm (or, for `Dual<Real>`, the computed inverse) is
finite and not approximately zero, so `NaN`, `±∞`, subnormal-scale values and the null cone are
rejected rather than inverted to garbage. `Bicomplex.isUnit()` stays the exact algebraic predicate.
This is tested uniformly in `ReciprocalPolicySpec`.

---

## Package Layout

```
hypercomplex/
├── complex/                  # ℂ = CD⟨ℝ⟩ (commutative doubling, σ = 1)
│   ├── Complex.kt
│   ├── ComplexAlgebras.kt
│   └── ComplexPrintable.kt
│
├── splitcomplex/             # SplitComplex<R> = CD⟨R⟩ (commutative doubling, σ = -1)
│   ├── SplitComplex.kt
│   └── SplitComplexAlgebras.kt
│
├── dual/                     # Dual<F>: value class over CD⟨F⟩ (commutative doubling, σ = 0)
│   ├── Dual.kt
│   ├── DualAlgebras.kt             ring, reciprocal, autodiff, matrix embedding
│   ├── DualModules.kt              scalar extension V ⊗ F[ε]
│   ├── DualRealFns.kt              elementary functions on Dual<Real>
│   └── DualRationalFns.kt          rational functions on Dual<Rational>
│
├── multi/                    # Bicomplex: value class over CD⟨ℂ⟩ (commutative doubling, σ = 1)
│   ├── Bicomplex.kt                standard basis z1 + z2·j; idempotent (𝛂, 𝜷) views
│   ├── BicomplexAlgebras.kt
│   (design notes for the general multicomplex case: docs/design/Multicomplex.md)
│
├── quaternion/               # ℍ-like rings
│   ├── Quaternion.kt               ℍ   = CD⟨ℂ⟩             (Real coefficients)
│   ├── QuaternionAlgebras.kt
│   ├── RationalQuaternion.kt       ℍ_ℚ = CD⟨ℚ(i)⟩          (Rational coefficients)
│   ├── RationalQuaternionAlgebras.kt
│   ├── LipschitzQuaternion.kt      Lip = CD⟨ℤ[i]⟩          (BigInteger coefficients)
│   ├── LipschitzQuaternionAlgebras.kt
│   ├── HurwitzQuaternion.kt        𝓗 ⊂ ℍ_ℚ, the Hurwitz order (value class over RationalQuaternion)
│   ├── HurwitzQuaternionAlgebras.kt
│   ├── QuaternionTier.kt           the one quaternion construction over ℂ, ℚ(i), ℤ[i]
│   ├── AxisSignEmbeddings.kt       the 6 specs for complex-like ↪ quaternion-like
│   ├── QuaternionEmbeddingKit.kt   generic complex-like → quaternion builder
│   └── QuaternionPrintable.kt
│
└── octonion/                 # 𝕆-like rings
    ├── Octonion.kt                 𝕆   = CD⟨ℍ⟩             (Real coefficients)
    ├── OctonionAlgebras.kt
    ├── RationalOctonion.kt         𝕆_ℚ = CD⟨ℍ_ℚ⟩           (Rational coefficients)
    ├── RationalOctonionAlgebras.kt
    ├── GravesianOctonion.kt        CD⟨Lip⟩ ≅ ℤ⁸            (BigInteger coefficients)
    ├── GravesianOctonionAlgebras.kt
    ├── OctonionTier.kt             the one octonion construction over ℍ, ℍ_ℚ, Lip
    ├── OctonionEmbeddingKit.kt     the 168 signed-basis-unit quaternion → octonion embeddings
    ├── OctonionFanoOrientation(s).kt, OrientedFanoLine.kt
    └── OctonionPrintable.kt
```

The Gaussian integers `ℤ[i]`, Gaussian rationals `ℚ(i)` and Eisenstein integers `ℤ[ω]` live in
`numberfields.quadratic`.

Bases are derived rather than written out: `CayleyDickson.doubledBasis(zero, baseBasis)` doubles
a basis `e₀, …, e_{n-1}` of `A` to `(e₀, 0), …, (0, e_{n-1})` in `CD<A>`, which reproduces
`1, i, j, k` for ℍ and `1, e₁, …, e₇` for 𝕆. Each quaternion and octonion ring exposes it as
`basis`.

---

## The Tower Diagram

Solid arrows are ring monomorphisms (structure-preserving injections).
Horizontal arrows stay within one "level" of the tower;
vertical arrows widen coefficients.

```
                        ℤ-coefficients          ℚ-coefficients          ℝ-coefficients
                        ──────────────          ──────────────          ──────────────

Level 0 (scalars)       ℤ ─────────────────────→ ℚ ─ ─ ─ ─ ─ ─ ─ ─ ─ → ℝ
                        │                         │           (floating pt)
                        ↓                         ↓
Level 1 (complex)       ℤ[i] ─────────────────→ ℚ(i) ─ ─ ─ ─ ─ ─ ─ ─→ ℂ
                        │  ╲  (×6)                │  ╲  (×6)              │  ╲  (×6)
                        │   ╲                     │   ╲                   │   ╲
                        ↓    ↘                    ↓    ↘                  ↓    ↘
Level 2 (quaternion)    Lip ──→ Hur ───────────→ ℍ_ℚ ─ ─ ─ ─ ─ ─ ─ ─ → ℍ
                        │  ╲  (×168)              │  ╲  (×168)            │  ╲  (×168)
                        │   ╲                     │   ╲                   │   ╲
                        ↓    ↘                    ↓    ↘                  ↓    ↘
Level 3 (octonion)      Grav ─────────────────→ 𝕆_ℚ ─ ─ ─ ─ ─ ─ ─ ─ → 𝕆
```

Dashed arrows (─ ─ →) are floating-point conversions (`toReal()`). They are **not**
monomorphisms, or even homomorphisms: distinct large integers and rationals collide in
`Double`, and products are preserved only up to rounding. They are therefore typed as plain
conversions, `UnaryOp`s named `…Approx` (e.g. `rationalQuaternionToQuaternionApprox`), and are
tested as homomorphisms *up to approximate equality* only.

The Eisenstein integers $\mathbb{Z}[\omega]$ sit beside $\mathbb{Z}[i]$ at Level 1
with a homomorphism $\mathbb{Z}[\omega] \to \mathbb{C}$. They embed into the Hurwitz order,
but not into the Lipschitz quaternions (see [below](#eisenstein-integers-and-the-quaternion-orders)).

---

## Embeddings: Complex → Quaternion (×6)

### The Idea

A complex number $a + bi$ lives in a 2D subalgebra of the quaternions.
The real part always maps to the scalar component, but the imaginary
unit $i_{\mathbb{C}}$ can map to any of $\pm i$, $\pm j$, $\pm k$ in $\mathbb{H}$:

$$
\varphi(a + bi_{\mathbb{C}}) \;=\; a \cdot 1 \;+\; b \cdot u, \qquad u \in \{+i,\, -i,\, +j,\, -j,\, +k,\, -k\}
$$

Each choice gives a unital ring monomorphism.
There are exactly **6 such embeddings**, parameterised by an
`AxisSignEmbedding(axis, sign)` where `axis ∈ {I, J, K}` and `sign ∈ {PLUS, MINUS}`.

### How to Create Them

Each complex-like Algebras object exposes a factory function that
takes an `AxisSignEmbedding` parameter (defaulting to canonical: $i_{\mathbb{C}} \mapsto +i$).

#### ℂ ↪ ℍ  (Real coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.*
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras

// Canonical: i_C ↦ +i
val canonical = ComplexAlgebras.complexToQuaternionEmbedding()

// Pick a specific one: i_C ↦ -k
val emb = AxisSignEmbedding(ImagAxis.K, Sign.MINUS)
val phiMinusK = ComplexAlgebras.complexToQuaternionEmbedding(emb)

// Use it:
val z = complex(3.0, 4.0)
val q = phiMinusK(z)   // quaternion(3.0, 0.0, 0.0, -4.0)
```

#### ℤ[i] ↪ Lip  (BigInteger coefficients)

```kotlin
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras

// Canonical: i ↦ +i
val phi = GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding()

// Along j-axis with positive sign: i ↦ +j
val phiJ = GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding(
    AxisSignEmbedding(ImagAxis.J, Sign.PLUS)
)
```

#### ℚ(i) ↪ ℍ_ℚ  (Rational coefficients)

```kotlin
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras

val phi = GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding()
```

#### ℤ[i] ↪ Hur  (via Lipschitz, then inclusion)

```kotlin
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras

// Composes: ℤ[i] ↪ Lip ↪ Hur
val phi = GaussianIntAlgebras.gaussianIntToHurwitzQuaternionEmbedding(
    AxisSignEmbedding(ImagAxis.I, Sign.PLUS)
)
```

#### Enumerating All 6

```kotlin
AxisSignEmbedding.all.forEach { emb ->
    val phi = ComplexAlgebras.complexToQuaternionEmbedding(emb)
    println("${emb.axis}${emb.sign}: phi(1+i) = ${phi(complex(1.0, 1.0))}")
}
```

The `AxisSignEmbedding.all` list contains all 6 combinations in a
deterministic order.

---

## Embeddings: Quaternion → Octonion (×168, or ×84 normalized)

### The Idea

A quaternion $w + xi + yj + zk$ can be embedded into the octonions by sending $i$ and $j$ to
*signed* imaginary basis units:

$$
\varphi(i) = \sigma_i\, e_p, \qquad \varphi(j) = \sigma_j\, e_q, \qquad
\varphi(k) = \varphi(i)\,\varphi(j) = \pm e_r, \qquad p \ne q,\ \sigma_i, \sigma_j \in \{\pm 1\}.
$$

Every pair of distinct points $p, q$ lies on exactly one **line of the Fano plane**
$\text{PG}(2,2)$ (the unique $(7,3,1)$-design), whose third point $r$ is forced. Two distinct
imaginary units anticommute and square to $-1$, so $(\varphi(i), \varphi(j))$ always generates a
copy of ℍ, and the quaternion multiplication table is respected.

What gets counted depends on what is allowed to vary:

| Count | What it counts | API |
|------:|----------------|-----|
| 1 | the Cayley–Dickson octonions themselves | `OctonionDivisionAlgebra` |
| 7 | coordinate quaternion subalgebras, one per Fano line | `FanoPlane.lines` |
| 84 | embeddings normalized so that $j \mapsto +e_q$ | `normalizedQuaternionEmbeddings()` |
| 168 | all signed-basis-unit embeddings ($\sigma_i$, $\sigma_j$ independent) | `allQuaternionEmbeddings()` |

$$
168 = \underbrace{7 \times 6}_{\text{ordered pairs } (p, q)} \times \underbrace{2 \times 2}_{(\sigma_i,\, \sigma_j)},
\qquad 84 = 7 \times 6 \times 2.
$$

The two families differ by an automorphism of ℍ: every embedding with $\sigma_j = -1$ is a
normalized one precomposed with $i \mapsto i,\ j \mapsto -j,\ k \mapsto -k$. So the 84 are a
set of representatives of the 168 modulo that automorphism (verified in `OctonionEmbeddingSpec`).

### How to Create Them

Each octonion-level Algebras object exposes an `embeddingKit` and both
`allQuaternionEmbeddings()` and `normalizedQuaternionEmbeddings()`.

#### ℍ ↪ 𝕆  (Real coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.Sign

// === Method 1: Create one embedding by spec ===

val (spec, phi) = OctonionAlgebras.embeddingKit.createEmbedding(
    iIndex = 1,            // i ↦ ±e₁
    jIndex = 2,            // j ↦ ±e₂
    iSign = Sign.PLUS,
    jSign = Sign.PLUS
)
// spec.k == 3, spec.kSign == +1   (forced: k ↦ +e₃)

val q = quaternion(1.0, 2.0, 3.0, 4.0)
val o = phi(q)  // 1 + 2e₁ + 3e₂ + 4e₃

// === Method 2: Enumerate ===

val all = OctonionAlgebras.allQuaternionEmbeddings()               // 168
val normalized = OctonionAlgebras.normalizedQuaternionEmbeddings() // 84

// Find a specific one by filtering:
val (_, phiCustom) = all.entries.first { (s, _) ->
    s.i == 3 && s.j == 5 && s.iSign == Sign.MINUS && s.jSign == Sign.PLUS
}
```

#### Lip ↪ Grav  (BigInteger coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.GravesianOctonionAlgebras

val all = GravesianOctonionAlgebras.allQuaternionEmbeddings()

// Or one at a time:
val (spec, phi) = GravesianOctonionAlgebras.embeddingKit.createEmbedding(
    iIndex = 4, jIndex = 6, iSign = Sign.PLUS, jSign = Sign.MINUS
)
```

#### ℍ_ℚ ↪ 𝕆_ℚ  (Rational coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.RationalOctonionAlgebras

val all = RationalOctonionAlgebras.allQuaternionEmbeddings()
```

### The `OctonionEmbeddingSpec`

Each embedding is identified by an `OctonionEmbeddingSpec`:

```kotlin
data class OctonionEmbeddingSpec(
    val i: Int,          // which e_p quaternion i maps to (up to sign)
    val j: Int,          // which e_q quaternion j maps to (up to sign)
    val k: Int,          // forced: third point on the Fano line through p and q
    val iSign: Sign,     // φ(i) = iSign · e_p
    val jSign: Sign,     // φ(j) = jSign · e_q
    val kSign: Int       // +1 or -1: φ(k) = kSign · e_r
)
```

The `kSign` is not a free parameter: it is *computed* from the octonion multiplication table,
then recorded in the spec for inspection.

### The Canonical Embedding

The canonical embedding has `iIndex = 1, jIndex = 2` and both signs `PLUS`, giving
$i \mapsto e_1,\; j \mapsto e_2,\; k \mapsto e_3$. This is `Quaternion.asOctonion()`, i.e. the
Cayley–Dickson embedding $q \mapsto (q, 0)$.

---

## Eisenstein Integers and the Quaternion Orders

The Eisenstein integers $\mathbb{Z}[\omega]$, $\omega = e^{2\pi i/3}$, satisfy
$\omega^2 + \omega + 1 = 0$. A unital ring monomorphism
$\varphi : \mathbb{Z}[\omega] \hookrightarrow \mathcal{O}$ into a quaternion order therefore needs
an element $u = \varphi(\omega)$ with $u^2 + u + 1 = 0$, i.e. a unit of order 3.

- In the **Lipschitz** quaternions there is none: the only units are $\pm 1, \pm i, \pm j, \pm k$,
  which have orders 1, 2 and 4.
- In the **Hurwitz** order there are eight:
  $$
  u = \tfrac{1}{2}(-1 \pm i \pm j \pm k)
  $$
  all have real part $-\tfrac{1}{2}$ and norm 1, hence $u^2 + u + 1 = 0$. For example
  $\rho = \omega_{\mathcal{H}} - 1 = \tfrac{1}{2}(-1 + i + j + k)$, where
  $\omega_{\mathcal{H}} = \tfrac{1}{2}(1 + i + j + k)$ is `HurwitzQuaternion.OMEGA`.
  So $\mathbb{Z}[\omega] \cong \mathbb{Z}[\rho] \subset \mathcal{H}$ (verified in
  `HurwitzQuaternionRingSpec`).

Kosmos does not yet provide the $\mathbb{Z}[\omega] \hookrightarrow \mathcal{H}$ monomorphisms.
Today the only path from the Eisenstein integers upward is the floating-point homomorphism

```
ℤ[ω] → ℂ → ℍ → 𝕆
```

via `EisensteinIntAlgebras.EisensteinIntToCHomomorphism`, which
passes through `Real` and is therefore not exact.

---

## Canonical (CD) Embeddings

At every level of the Cayley–Dickson tower, there is a *structural*
embedding given by the first-slot injection $a \mapsto (a, 0)$.
This is a ring monomorphism by the universal property of the construction,
and it is provided by:

```kotlin
CayleyDickson.canonicalEmbedding(base, doubled)
```

These canonical embeddings are used throughout the tower:

| Arrow                | Embedding                                                    |
|----------------------|--------------------------------------------------------------|
| $\mathbb{Z}[i] \hookrightarrow \text{Lip}$ | `GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding()` (canonical default) |
| $\text{Lip} \hookrightarrow \text{Grav}$    | `LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism` |
| $\mathbb{Q}(i) \hookrightarrow \mathbb{H}_\mathbb{Q}$ | `GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding()` (canonical default) |
| $\mathbb{C} \hookrightarrow \mathbb{B}$     | `BicomplexAlgebras.complexToBicomplexMonomorphism`           |
| $\mathbb{H}_\mathbb{Q} \hookrightarrow \mathbb{O}_\mathbb{Q}$ | `RationalOctonionAlgebras.RationalQuaternionToRationalOctonionMonomorphism` |
| $\mathbb{H} \hookrightarrow \mathbb{O}$     | `OctonionAlgebras.QuaternionToOctonionMonomorphism`          |

The ×6 and ×168 embedding families *include* the canonical embedding
as a special case (the one with axis=I, sign=PLUS for ×6;
iIndex=1, jIndex=2, both signs PLUS for ×168).

---

## Scalar Actions and Modules

Each type has an appropriate scalar action:

| Type | Scalar | Structure |
|------|--------|-----------|
| $\mathbb{Z}[i]$ | $\mathbb{Z}$ | `ZModule` |
| $\mathbb{Q}(i)$ | $\mathbb{Q}$ | `FiniteVectorSpace` (dim 2) |
| $\mathbb{C}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 2) |
| Lip  | $\mathbb{Z}$ | `ZModule` |
| Hur  | $\mathbb{Z}$ | `ZModule` |
| $\mathbb{H}_\mathbb{Q}$ | $\mathbb{Q}$ | `FiniteVectorSpace` (dim 4) |
| $\mathbb{H}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 4) |
| $\mathbb{H}$     | $\mathbb{C}$ | `FiniteVectorSpace` (dim 2, per embedding) |
| Grav | $\mathbb{Z}$ | `ZModule` |
| $\mathbb{O}_\mathbb{Q}$ | $\mathbb{Q}$ | `FiniteVectorSpace` (dim 8) |
| $\mathbb{O}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 8) |
| $\mathbb{B}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 4) |
| $\mathbb{B}$     | $\mathbb{C}$ | `FiniteVectorSpace` (dim 2) |

These scalar actions are required by the `OctonionEmbeddingKit`
to construct embeddings via linear combination of basis elements.

---

## Quick Reference: Monomorphisms and Conversions

Entries marked (≈) are **not** morphisms: they are `UnaryOp` conversions into floating point
(named `…Approx`), which round and so preserve the operations only approximately.

### Level 0 → Level 1

| Arrow | Location |
|-------|----------|
| `ℤ ↪ ℤ[i]` | `GaussianIntAlgebras.ZToGaussianIntMonomorphism` |
| `ℤ[i] ↪ ℂ` | `GaussianIntAlgebras.GaussianIntToComplexMonomorphism` |
| `ℚ ↪ ℚ(i)` | `GaussianRatAlgebras.QToGaussianRatMonomorphism` |
| `ℤ[i] ↪ ℚ(i)` | `GaussianIntAlgebras.GaussianIntToRatMonomorphism` |
| `ℤ ↪ ℚ(i)` | `GaussianRatAlgebras.ZToGaussianRatMonomorphism` |
| `ℚ(i) → ℂ` | `GaussianRatAlgebras.GaussianRatToComplexHomomorphism` |
| `ℝ ↪ ℂ` | `RealAlgebras.RealToComplexMonomorphism` |
| `ℤ[ω] → ℂ` | `EisensteinIntAlgebras.EisensteinIntToCHomomorphism` |

### Level 1 → Level 2 (×6 families)

| Arrow | Location |
|-------|----------|
| `ℂ ↪ ℍ` (×6) | `ComplexAlgebras.complexToQuaternionEmbedding(emb)` |
| `ℤ[i] ↪ Lip` (×6) | `GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding(emb)` |
| `ℚ(i) ↪ ℍ_ℚ` (×6) | `GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding(emb)` |
| `ℤ[i] ↪ 𝓗` (×6) | `GaussianIntAlgebras.gaussianIntToHurwitzQuaternionEmbedding(emb)` |
| `ℂ ↪ 𝔹` | `BicomplexAlgebras.complexToBicomplexMonomorphism` (`z ↦ z + 0j`) |
| `ℂ ↪ ℂ(j) ⊂ 𝔹` | `BicomplexAlgebras.complexToBicomplexAlongJ` |

### Within Level 2

| Arrow | Location |
|-------|----------|
| `Lip ↪ 𝓗` | `LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism` |
| `𝓗 ↪ ℍ_ℚ` | `HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism` |
| `Lip ↪ ℍ_ℚ` | `LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism` |
| `Lip → ℍ` (≈) | `LipschitzQuaternionAlgebras.lipschitzToQuaternionApprox` |
| `𝓗 → ℍ` (≈) | `HurwitzQuaternionAlgebras.hurwitzToQuaternionApprox` |
| `ℍ_ℚ → ℍ` (≈) | `RationalQuaternionAlgebras.rationalQuaternionToQuaternionApprox` |
| `𝔹 → ℂ` (×2) | `BicomplexAlgebras.BicomplexFirstProjectionHomomorphism`, `…SecondProjectionHomomorphism` |

### Level 2 → Level 3 (×168 families + canonical)

| Arrow | Location |
|-------|----------|
| `ℍ ↪ 𝕆` (×168) | `OctonionAlgebras.allQuaternionEmbeddings()` |
| `Lip ↪ Grav` (×168) | `GravesianOctonionAlgebras.allQuaternionEmbeddings()` |
| `ℍ_ℚ ↪ 𝕆_ℚ` (×168) | `RationalOctonionAlgebras.allQuaternionEmbeddings()` |
| `ℍ ↪ 𝕆` (canonical) | `OctonionAlgebras.QuaternionToOctonionMonomorphism` |
| `Lip ↪ Grav` (canonical) | `LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism` |
| `ℍ_ℚ ↪ 𝕆_ℚ` (canonical) | `RationalOctonionAlgebras.RationalQuaternionToRationalOctonionMonomorphism` |

### Within Level 3 and Cross-Level

| Arrow | Location |
|-------|----------|
| `Grav ↪ 𝕆_ℚ` | `GravesianOctonionAlgebras.GravesianToRationalOctonionMonomorphism` |
| `Lip ↪ 𝕆_ℚ` | `LipschitzQuaternionAlgebras.LipschitzToRationalOctonionMonomorphism` |
| `𝓗 ↪ 𝕆_ℚ` | `RationalOctonionAlgebras.HurwitzToRationalOctonionMonomorphism` |
| `Grav → 𝕆` (≈) | `GravesianOctonionAlgebras.gravesianToOctonionApprox` |
| `𝕆_ℚ → 𝕆` (≈) | `RationalOctonionAlgebras.rationalOctonionToOctonionApprox` |

### Matrix representations

| Arrow | Location |
|-------|----------|
| `ℂ ↪ M₂(ℝ)` | `ComplexAlgebras.ComplexToRank2RMatrixMonomorphism` |
| `SplitComplex<R> ↪ M₂(R)` | `SplitComplexAlgebras.splitComplexToRank2MatrixMonomorphism(base)` |
| `Dual<F> ↪ M₂(F)` | `DualAlgebras.dualToRank2MatrixMonomorphism(base)` |
| `𝔹 ↪ M₂(ℂ)` | `BicomplexAlgebras.BicomplexToComplexMatrixMonomorphism`, `…ToDiagonalMatrixMonomorphism` |
| `ℍ ↪ M₂(ℂ)` | `QuaternionAlgebras.QuaternionToComplexMatrixMonomorphism` |

The first four are all `quadraticRank2MatrixEmbedding`: the regular representation of
`R[x]/(x² − s − tx)` on the basis `{1, x}`.

---

## The Fano Plane

The 168 octonion embeddings are governed by the Fano plane, the unique
Steiner triple system $S(2,3,7)$ on points $\{1,\ldots,7\}$.
Its 7 lines (each a 3-element subset) are:

```
{1, 2, 3}    {1, 4, 5}    {1, 6, 7}
{2, 4, 6}    {2, 5, 7}    {3, 4, 7}    {3, 5, 6}
```

Every pair of distinct points lies on exactly one line.
Given a quaternion embedding choice $(i \mapsto e_p,\; j \mapsto e_q)$,
the third point $r$ on the unique line through $p$ and $q$
determines the image of $k$:

$$
\varphi(k) = \varphi(i) \cdot \varphi(j) = \pm\, e_r
$$

The sign depends on the orientation of the Fano line relative to
the octonion multiplication table and is computed (not chosen) by
the `OctonionEmbeddingKit`.

The Fano plane implementation lives in `org.vorpal.kosmos.combinatorics.FanoPlane`.